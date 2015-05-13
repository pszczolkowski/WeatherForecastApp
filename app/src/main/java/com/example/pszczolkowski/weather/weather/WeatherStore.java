package com.example.pszczolkowski.weather.weather;

import android.content.Context;

import com.example.pszczolkowski.weather.location.Location;

import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class WeatherStore{

	private static final String FILE_NAME = "weathers";
	private Context context;
	private Map< String , Weather > weathers = null;

	public WeatherStore(Context context){
		this.context = context;
	}

	public void put( Location location , Weather weather ){
		if( weathers == null )
			readFromFile();

		weathers.put( location.getName() , weather );
		saveToFile();
	}

	public Weather get( Location location ){
		if( weathers == null )
			readFromFile();

		return weathers.get( location.getName() );
	}

	private void saveToFile(){
		try( ObjectOutputStream out = new ObjectOutputStream( context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE) ) ) {
			out.writeObject( weathers );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readFromFile(){
		weathers = new HashMap<>();

		try( ObjectInputStream in = new ObjectInputStream( context.openFileInput( FILE_NAME ) ) ) {
			//noinspection unchecked
			weathers = (Map< String , Weather >) in.readObject();
		}catch(FileNotFoundException ignored){}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
