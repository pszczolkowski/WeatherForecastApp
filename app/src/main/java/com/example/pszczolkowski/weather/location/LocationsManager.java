package com.example.pszczolkowski.weather.location;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class LocationsManager{

	private static final String FILE_NAME = "locations";
	private List< Location > locations;

	private Context context;

	public LocationsManager(Context context){
		this.context = context;
	}

	public boolean addLocation( Location location ){
		locations = getLocations();
		if( locations.contains( location ) )
			return false;

		locations.add( location );
		saveLocationsList( locations );

		return true;
	}

	public boolean removeLocation( Location location ){
		locations = getLocations();
		if( !locations.contains( location ) )
			return false;

		locations.remove( location );
		saveLocationsList( locations );

		return true;
	}

	public List< Location > getLocations(){
		if( locations != null )
			return locations;

		List< Location > readLocations = new ArrayList<>( );

		try( ObjectInputStream in = new ObjectInputStream( context.openFileInput( FILE_NAME ) ) ) {
			//noinspection unchecked
			readLocations = (List<Location>) in.readObject();
		}catch(FileNotFoundException ignored){}
		catch (Exception e) {
			e.printStackTrace();
		}

		locations = readLocations;

		return readLocations;
	}

	public Location getLocationByName( String name ){
		locations = getLocations();
		Location result = null;

		for(Location location : locations){
			if(location.getName().equals( name )){
				result = location;
				break;
			}
		}

		return result;
	}

	public boolean containsLocationWithName(String locationName){
		locations = getLocations();

		for( Location location : locations){
			if( location.getName().equalsIgnoreCase( locationName ) ){
				return true;
			}
		}

		return false;
	}


	private void saveLocationsList(List<Location> locations){
		try( ObjectOutputStream out = new ObjectOutputStream( context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE) ) ) {
			out.writeObject( locations );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
