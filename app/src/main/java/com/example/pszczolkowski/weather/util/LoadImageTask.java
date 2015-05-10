package com.example.pszczolkowski.weather.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LoadImageTask extends AsyncTask< String , Void , Bitmap >{
	private ImageView imageView;
	private Context context;

	public LoadImageTask( Context context , ImageView imageView) {
		this.context = context;
		this.imageView = imageView;
	}


	@Override
	protected Bitmap doInBackground(String... urls) {
		String url = urls[0];
		String fileName = url.substring( 7 ).replaceAll( "/" , "_" );
		Bitmap bitmap = readBitmapFromFile( fileName );

		if( bitmap == null ){
			try( InputStream in = new java.net.URL( url ).openStream() ) {
				bitmap = BitmapFactory.decodeStream( in );

				saveBitmapToFile( fileName, bitmap );
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return bitmap;
	}

	protected void onPostExecute(Bitmap bitmap) {
		imageView.setImageBitmap( bitmap );
	}


	private void saveBitmapToFile(String fileName, Bitmap bitmap){
		try( FileOutputStream stream = context.openFileOutput( fileName , Context.MODE_PRIVATE ) ){
			ByteArrayOutputStream outstream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 85, outstream);
			byte[] byteArray = outstream.toByteArray();

			stream.write( byteArray );
		}catch(IOException ignored){}
	}

	private Bitmap readBitmapFromFile( String fileName ){
		File file = new File( context.getFilesDir() , fileName );
		return BitmapFactory.decodeFile( file.getAbsolutePath() );

	}
}