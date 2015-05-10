package com.example.pszczolkowski.weather.location;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

class LocationsDownloadTask extends AsyncTask< URL , Void , String >{

	private OnLocationsLoadedListener listener;

	private LocationsDownloadTask(OnLocationsLoadedListener listener){
		this.listener = listener;
	}

	public static LocationsDownloadTask subscribe( OnLocationsLoadedListener listener ){
		return new LocationsDownloadTask( listener );
	}

	@Override
	protected String doInBackground(URL... urls){
		URL url = urls[ 0 ];
		try( BufferedReader in = new BufferedReader( new InputStreamReader(url.openStream())) ){
			String inputLine;
			StringBuilder sb = new StringBuilder(  );

			while ((inputLine = in.readLine()) != null){
				sb.append( inputLine );
			}

			return sb.toString();
		}catch(IOException e){
			listener.onLocationsLoadError( e );

		}

		return null;
	}

	@Override
	protected void onPostExecute(String response){
		if( response != null )
			listener.onLocationsLoaded( response );
	}

	public interface OnLocationsLoadedListener{
		void onLocationsLoaded( String locations );
		void onLocationsLoadError(Exception e);
	}
}