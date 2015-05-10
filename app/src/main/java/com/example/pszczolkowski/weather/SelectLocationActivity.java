package com.example.pszczolkowski.weather;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.pszczolkowski.weather.location.Location;
import com.example.pszczolkowski.weather.location.LocationsAdapter;
import com.example.pszczolkowski.weather.location.LocationsLoader;
import com.example.pszczolkowski.weather.location.LocationsManager;
import com.example.pszczolkowski.weather.util.ConnectionChecker;

import java.util.List;


public class SelectLocationActivity extends ActionBarActivity{

	private LocationsAdapter locationsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_select_location );

		initializeLocationsList();

		Button addLocationButton = (Button) findViewById( R.id.addLocationButton );
		addLocationButton.setOnClickListener( new View.OnClickListener(){
			@Override
			public void onClick(View v){
				onAddLocationButtonClick( v );
			}
		} );
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
		super.onCreateContextMenu( menu, v, menuInfo );

		MenuInflater inflater = getMenuInflater();
		inflater.inflate( R.menu.location_context_menu , menu );
	}

	@Override
	public boolean onContextItemSelected(MenuItem item){
		// REMOVE OPTION
		if( item.getItemId() == R.id.locationContextMenuRemove ){
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			LocationsManager locationsManager = new LocationsManager( this );

			Location location = locationsAdapter.getItem( info.position );
			if( locationsManager.removeLocation( location ) ){
				locationsAdapter.remove( location );
			}
		}

		return super.onContextItemSelected( item );
	}

	public void onAddLocationButtonClick(View view){
		EditText newLocationNameInput = (EditText) findViewById( R.id.new_location_name );
		String newLocationName = newLocationNameInput.getText().toString().trim();

		if( newLocationName.isEmpty() ){
			toast( R.string.location_name_empty );
			return;
		}

		tryToAddLocationWithName( newLocationName );
	}


	private void initializeLocationsList(){
		LocationsManager locationsManager = new LocationsManager( this );

		ListView locationsList = (ListView) findViewById( R.id.location_list );
		locationsAdapter = new LocationsAdapter( this, R.layout.location_item, locationsManager.getLocations() );
		locationsList.setAdapter( locationsAdapter );

		locationsList.setEmptyView( findViewById( R.id.empty_locations_list ) );

		locationsList.setOnItemClickListener( new AdapterView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				Location location = locationsAdapter.getItem( position );
				selectLocation( location );
			}
		} );
		registerForContextMenu( locationsList );
	}

	private void selectLocation(Location location){
		SharedPreferences preferences = getSharedPreferences( MainActivity.PREFERENCES_NAME , Activity.MODE_PRIVATE );
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString( MainActivity.SELECTED_LOCATION_NAME, location.getName() );
		editor.apply();

		displayWeatherFor( location );
	}

	private void displayWeatherFor(Location location){
		Intent intent = new Intent( this , MainActivity.class );

		Bundle bundle = new Bundle();
		bundle.putSerializable( "location" , location );
		intent.putExtras( bundle );

		startActivity( intent );
	}

	private void tryToAddLocationWithName( final String locationName ){
		if( !isNetworkConnectionAvailable()){
			toast( R.string.no_network_connection );
			return;
		}

		final LocationsManager locationsManager = new LocationsManager( this );

		if( locationsManager.containsLocationWithName( locationName ) )
			toast( R.string.location_exists );
		else{
			final AlertDialog dialog = showAlertDialog( R.string.loading );

			LocationsLoader loader = new LocationsLoader( this );
			loader.addOnLocationsLoadedListener( new LocationsLoader.OnLocationsLoadedListener(){
				@Override
				public void onLocationsLoaded(List<Location> foundLocations){
					dialog.dismiss();

					if( foundLocations.size() == 0 ){
						new AlertDialog.Builder( SelectLocationActivity.this )
								.setMessage( R.string.location_doesnt_exist )
								.setCancelable( true )
								.setPositiveButton( "OK", null )
								.create()
								.show();
					}else{
						final Location foundLocation = foundLocations.get( 0 );

						StringBuilder sb = new StringBuilder();
						sb.append( getResources().getString( R.string.did_you_mean ) );
						sb.append( foundLocation.getName() );
						sb.append( "\n" );

						for(String admin : foundLocation.getAdmin().keySet()){
							sb.append( admin );
							sb.append( ": " );
							sb.append( foundLocation.getAdmin().get( admin ) );
							sb.append( "\n" );
						}

						new AlertDialog.Builder( SelectLocationActivity.this )
								.setMessage( sb.toString() )
								.setCancelable( false )
								.setPositiveButton( "yes", new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog, int which){
										locationsAdapter.add( foundLocation );
										locationsManager.addLocation( foundLocation );

										( (EditText) findViewById( R.id.new_location_name ) ).setText( "" );
										toast( R.string.location_added );
									}
								} ).setNegativeButton( "no", null )
								.create()
								.show();
					}


					/*Location matchingLocation = findMatchingLocation( foundLocations, locationName );

					Log.e( "testowanie" , matchingLocation.getAdmin().size() + " " + matchingLocation.getAdmin() );

					if( matchingLocation == null ){
						String text = getResources().getString( R.string.location_doesnt_exist );
						if( foundLocations.size() > 0 ){
							text += getResources().getString( R.string.did_you_mean_one_of_the_following ) +
							foundLocations.toString();
						}

						new AlertDialog.Builder( SelectLocationActivity.this )
							.setMessage( text )
							.setCancelable( true )
							.setPositiveButton( "OK" , null )
							.create()
							.show();
					}
					else{
						locationsAdapter.add( matchingLocation );
						locationsManager.addLocation( matchingLocation );

						((EditText) findViewById( R.id.new_location_name )).setText( "" );
						toast( R.string.location_added );
					}*/
				}

				@Override
				public void onError(Exception error){
					Log.e( "error", error.toString() );
				}
			} );

			loader.loadLocationsWithName( locationName );
		}
	}

	private boolean isNetworkConnectionAvailable(){
		return ConnectionChecker.forActivity( this ).isAvailable();
	}

	private AlertDialog showAlertDialog( int text ){
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder( this );
		dlgAlert.setMessage( text );
		dlgAlert.setCancelable( false );
		AlertDialog dialog = dlgAlert.create();
		dialog.show();

		return dialog;
	}

	private Location findMatchingLocation(List<Location> locations, String locationName){
		Location matchingLocation = null;
		for( Location location : locations ){
			if( location.getName().equalsIgnoreCase( locationName ) ){
				matchingLocation = location;
				break;
			}
		}
		return matchingLocation;
	}

	private void toast(int string){
		Toast.makeText( this , string, Toast.LENGTH_SHORT ).show();
	}

}
