package com.example.pszczolkowski.weather.util;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class FileDownloadTask extends AsyncTask< URL , Void , String >{

	private OnFileDownloadedListener listener;

	private FileDownloadTask(OnFileDownloadedListener listener){
		this.listener = listener;
	}

	public static FileDownloadTask subscribe( OnFileDownloadedListener listener ){
		return new FileDownloadTask( listener );
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
			listener.onFileDownloadError( e );

		}

		return null;
	}

	@Override
	protected void onPostExecute(String fileContent){
		if( fileContent != null )
			listener.onFileDownloaded( fileContent );
	}

	public interface OnFileDownloadedListener{
		void onFileDownloaded(String fileContent);
		void onFileDownloadError(Exception e);
	}
}