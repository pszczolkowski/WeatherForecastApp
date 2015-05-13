package com.example.pszczolkowski.weather.location;


import com.example.pszczolkowski.weather.util.FileDownloadTask;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kod stworzony na bazie https://github.com/survivingwithandroid/Swa-app/tree/master/AndroidYahooWeather
 */
public class LocationsLoader implements FileDownloadTask.OnFileDownloadedListener{

	private List< OnLocationsLoadedListener > listeners = new ArrayList<>();


	public void loadLocationsWithName( String locationName ){
		URL url = queryUrlFor( locationName );
		FileDownloadTask.subscribe( this ).execute( url );
	}

	public void addOnLocationsLoadedListener( OnLocationsLoadedListener listener ){
		listeners.add( listener );
	}

	private void notifyListenersAboutError(Exception error){
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
			Map< String , String > admin = new HashMap<>();
			Location location = new Location(  );
			String adminType = null;

			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					tagName = parser.getName();
					if( "admin1".equals( tagName ) ||
							"admin2".equals( tagName ) ||
							"admin3".equals( tagName )){
						adminType = parser.getAttributeValue( null , "type" );
					}
				}
				else if (eventType == XmlPullParser.TEXT) {
					switch( tagName ){
						case "woeid":
							location.setWoeid( parser.getText() );
							break;
						case "name":
							location.setName( parser.getText() );
							break;
						case "country":
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

	private static URL queryUrlFor(String locationName) {
		try{
			return new URL( "https://query.yahooapis.com/v1/public/yql?format=xml&q=" + URLEncoder.encode( "select * from geo.places(1) where text=\"" + locationName + "\"" , "UTF-8" ) );
		}catch(UnsupportedEncodingException | MalformedURLException e){
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void onFileDownloaded(String response){
		notifyListenersAboutSuccess( parse( response ) );
	}

	@Override
	public void onFileDownloadError(Exception e){
		notifyListenersAboutError( e );
	}


	public static interface OnLocationsLoadedListener{
		public void onLocationsLoaded( List< Location > locations );
		public void onError( Exception error );
	}

}