package com.example.pszczolkowski.weather.location;


import android.content.Context;
import android.util.Log;

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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kod stworzony na bazie https://github.com/survivingwithandroid/Swa-app/tree/master/AndroidYahooWeather
 */
public class LocationsLoader implements Response.Listener<String> , Response.ErrorListener{

	private static final int MAX_CACHE_SIZE_BYTES = 1024 * 1024;
	private Context context;
	private List< OnLocationsLoadedListener > listeners = new ArrayList<>();

	public LocationsLoader(Context context){
		this.context = context;
	}

	public void loadLocationsWithName( String locationName ){
		Cache cache = new DiskBasedCache( context.getCacheDir() , MAX_CACHE_SIZE_BYTES );
		Network network = new BasicNetwork( new HurlStack() );
		RequestQueue requestQueue = new RequestQueue( cache , network );

		String url = queryUrlFor( locationName );

		requestQueue.add( requestFor( url ) );
		requestQueue.start();
	}

	public void addOnLocationsLoadedListener( OnLocationsLoadedListener listener ){
		listeners.add( listener );
	}

	@Override
	public void onResponse(String response){
		notifyListenersAboutSuccess( parse( response ) );
	}

	@Override
	public void onErrorResponse(VolleyError error){
		notifyListenersAboutError( error );
	}


	private StringRequest requestFor(String url){
		return new StringRequest( Request.Method.GET, url, this , this );
	}

	private void notifyListenersAboutError(VolleyError error){
		for( OnLocationsLoadedListener listener : listeners )
			listener.onError( error );
	}

	private void notifyListenersAboutSuccess(List<Location> locations){
		for( OnLocationsLoadedListener listener : listeners )
			listener.onLocationsLoaded( locations );
	}

	private static List<Location> parse(String response){
		List< Location > locations = new ArrayList<>(  );

		try{
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput( new StringReader( response ) );
			int eventType = parser.getEventType();

			String tagName = null;
			String locationWoeid = null;
			String locationName = null;
			String locationCountry = null;
			Map< String , String > admin = new HashMap<>();
			Location location = new Location(  );
			String adminType = null;

			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					tagName = parser.getName();
					if ( "place".equals( tagName ) ) {
						locationWoeid = null;
						locationName = null;
						locationCountry = null;
					}else if( "admin1".equals( tagName ) ||
							"admin2".equals( tagName ) ||
							"admin3".equals( tagName )){
						adminType = parser.getAttributeValue( null , "type" );
					}
				}
				else if (eventType == XmlPullParser.TEXT) {
					switch( tagName ){
						case "woeid":
							//locationWoeid = parser.getText();
							location.setWoeid( parser.getText() );
							break;
						case "name":
							//locationName = parser.getText();
							location.setName( parser.getText() );
							break;
						case "country":
							//locationCountry = parser.getText();
							location.setCountry( parser.getText() );
							break;
						case "admin1":
						case "admin2":
						case "admin3":
							admin.put( adminType , parser.getText() );
							break;
					}
				}
				else if (eventType == XmlPullParser.END_TAG) {
					tagName = parser.getName();
					if ( "place".equals( tagName ) ){
						//Location location = new Location( locationName );
						//location.setWoeid( locationWoeid );
						//location.setCountry( locationCountry );

						for( String type : admin.keySet() ){
							location.putAdmin( type , admin.get( type ) );
						}

						locations.add( location );
					}

					tagName = null;
				}

				eventType = parser.next();
			}
		}catch(XmlPullParserException | IOException e){
			e.printStackTrace();
		}

		return locations;
	}

	private static String queryUrlFor(String locationName) {
		try{
			return "https://query.yahooapis.com/v1/public/yql?format=xml&q=" + URLEncoder.encode( "select * from geo.places(1) where text=\"" + locationName + "\"" , "UTF-8" );
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}

		return null;
	}


	public static interface OnLocationsLoadedListener{
		public void onLocationsLoaded( List< Location > locations );
		public void onError( VolleyError error );
	}
}
