package com.example.pszczolkowski.weather.weather;


import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.example.pszczolkowski.weather.location.Location;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Kod stworzony na bazie https://github.com/survivingwithandroid/Swa-app/tree/master/AndroidYahooWeather
 */
public class WeatherLoader{
	private static final int MAX_CACHE_SIZE_BYTES = 1024 * 1024;
	private Context context;
	private List<OnWeatherLoadedListener> listeners = new ArrayList<>();

	private final static String YAHOO_WEATHER_URL = "http://weather.yahooapis.com/forecastrss";

	public WeatherLoader(Context context){
		this.context = context;
	}

	public void addOnWeatherLoadedListener( OnWeatherLoadedListener listener ){
		listeners.add( listener );
	}

	public void loadWeather( final Location location , String unit ){
			Cache cache = new DiskBasedCache( context.getCacheDir() , MAX_CACHE_SIZE_BYTES );
			Network network = new BasicNetwork( new HurlStack() );
			RequestQueue requestQueue = new RequestQueue( cache , network );

			String url = queryUrlForLocation( location.getWoeid(), unit );


			StringRequest req = new StringRequest( Request.Method.GET, url, new Response.Listener<String>() {
				@Override
				public void onResponse(String s) {
					Weather weather = parseResponse( s );
					for( OnWeatherLoadedListener listener : listeners )
						listener.onWeatherLoaded( location , weather );
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse( VolleyError error ) {
					for( OnWeatherLoadedListener listener : listeners )
						listener.onError( error );
				}
			});

			requestQueue.add(req);
			requestQueue.start();
		}

	private static Weather parseResponse (String resp) {
		final Weather result = new Weather();

		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(new StringReader(resp));

			String tagName;
			String currentTag = null;

			int event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				tagName = parser.getName();

				if (event == XmlPullParser.START_TAG) {
					switch(tagName){
						case "yweather:wind":
							result.wind.chill = Integer.parseInt( parser.getAttributeValue( null, "chill" ) );
							result.wind.direction = Integer.parseInt( parser.getAttributeValue( null, "direction" ) );
							result.wind.speed = (int) Float.parseFloat( parser.getAttributeValue( null, "speed" ) );
							break;
						case "yweather:atmosphere":
							result.atmosphere.humidity = Integer.parseInt( parser.getAttributeValue( null, "humidity" ) );
							if( !parser.getAttributeValue( null, "visibility" ).isEmpty() )
								result.atmosphere.visibility = Float.parseFloat( parser.getAttributeValue( null, "visibility" ) );
							result.atmosphere.pressure = Float.parseFloat( parser.getAttributeValue( null, "pressure" ) );
							result.atmosphere.rising = Integer.parseInt( parser.getAttributeValue( null, "rising" ) );
							break;
						case "yweather:forecast":
							if( parser.getAttributeValue( null , "day" ).equals( result.day ) ){
								result.condition.tempMax = Integer.parseInt( parser.getAttributeValue( null, "high" ) );
								result.condition.tempMin = Integer.parseInt( parser.getAttributeValue( null, "low" ) );
								break;
							}

							Weather.Forecast forecast = result.new Forecast();
							forecast.code = Integer.parseInt( parser.getAttributeValue( null, "code" ) );
							forecast.tempMin = Integer.parseInt( parser.getAttributeValue( null, "low" ) );
							forecast.tempMax = Integer.parseInt( parser.getAttributeValue( null, "high" ) );
							forecast.description = parser.getAttributeValue( null , "text" );
							forecast.day = parser.getAttributeValue( null , "day" );
							forecast.date = parser.getAttributeValue( null , "date" );

							result.forecast.add( forecast );
							break;
						case "yweather:condition":
							result.condition.code = Integer.parseInt( parser.getAttributeValue( null, "code" ) );
							result.condition.description = parser.getAttributeValue( null, "text" );
							result.condition.temp = Integer.parseInt( parser.getAttributeValue( null, "temp" ) );
							result.condition.date = parser.getAttributeValue( null, "date" );
							break;
						case "yweather:units":
							result.units.temperature = "°" + parser.getAttributeValue( null, "temperature" );
							result.units.pressure = parser.getAttributeValue( null, "pressure" );
							result.units.distance = parser.getAttributeValue( null, "distance" );
							result.units.speed = parser.getAttributeValue( null, "speed" );
							break;
						case "yweather:location":
							result.location.name = parser.getAttributeValue( null, "city" );
							result.location.region = parser.getAttributeValue( null, "region" );
							result.location.country = parser.getAttributeValue( null, "country" );
							break;
						case "image":
							currentTag = "image";
							break;
						case "img":
							result.imageUrl = parser.getAttributeValue( null, "src" );
							break;
						case "lastBuildDate":
							currentTag = "update";
							break;
						case "ttl":
							currentTag = "ttl";
							break;
						case "yweather:astronomy":
							result.astronomy.sunRise = parser.getAttributeValue( null, "sunrise" );
							result.astronomy.sunSet = parser.getAttributeValue( null, "sunset" );
							break;
						case "geo:lat":
							currentTag = "lat";
							break;
						case "geo:long":
							currentTag = "long";
							break;
						case "description":
							currentTag = "description";
							break;
					}

				}else if (event == XmlPullParser.END_TAG) {
					currentTag = null;
				}
				else if (event == XmlPullParser.TEXT && currentTag != null) {
					switch(currentTag){
						case "update":
							result.day = parser.getText().substring( 0, 3 );

							result.lastUpdate = new Date();
							SimpleDateFormat sdf = new SimpleDateFormat( "EEE, dd MMM yyyy KK:mm a z", Locale.US );

							String value = parser.getText();
							try{
								result.lastUpdate = sdf.parse( value );
							}catch(ParseException ignored){}
							break;
						case "ttl":
							result.timeToLive = Integer.parseInt( parser.getText() );
							break;
						case "lat":
							result.location.latitude = Double.parseDouble( parser.getText() );
							break;
						case "long":
							result.location.longitude = Double.parseDouble( parser.getText() );
							break;
						case "description":
							String text = parser.getText();
							int pos = text.indexOf( "src=\"" );
							if(pos > -1){
								pos += 5;
								int pos2 = text.indexOf( "\"", pos );
								result.imageUrl = parser.getText().substring( pos, pos2 );
							}
							break;
					}
				}
				event = parser.next();
			}
		}
		catch( Exception e ) {
			e.printStackTrace();
		}

		return result;
	}

	private static String queryUrlForLocation(String woeid, String unit) {
		return  YAHOO_WEATHER_URL + "?w=" + woeid + "&u=" + unit;
	}

	public static interface OnWeatherLoadedListener{
		public void onWeatherLoaded( Location location , Weather weather );
		public void onError( VolleyError error );
	}
}
