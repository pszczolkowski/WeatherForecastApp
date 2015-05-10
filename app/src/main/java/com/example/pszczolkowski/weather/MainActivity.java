package com.example.pszczolkowski.weather;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.pszczolkowski.weather.location.Location;
import com.example.pszczolkowski.weather.location.LocationsManager;
import com.example.pszczolkowski.weather.util.ConnectionChecker;
import com.example.pszczolkowski.weather.weather.Units;
import com.example.pszczolkowski.weather.weather.Weather;
import com.example.pszczolkowski.weather.weather.WeatherLoader;
import com.example.pszczolkowski.weather.weather.WeatherStore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements WeatherLoader.OnWeatherLoadedListener {

	public static final String SELECTED_LOCATION_NAME = "selectedLocationName";
	public static final String PREFERENCES_NAME = "sharedPreferenes";
	private static final String SELECTED_UNITS_NAME = "selectedUnitsName";
	private String WEATHER_PREFERENCES = "weatherStore";
	private Location selectedLocation;
	private WeatherBasicDataFragment weatherBasicDataFragment;
	private WeatherAdditionalDataFragment weatherAdditionalDataFragment;
	private WeatherForecastFragment weatherForecastFragment;
	private PageAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate( savedInstanceState );


		selectedLocation = readSelectedLocation();
		if( selectedLocation == null )
			displayLocationSelection();
		else{
			setContentView( R.layout.activity_main );

			if (savedInstanceState != null) {
				weatherBasicDataFragment = (WeatherBasicDataFragment) getSupportFragmentManager().getFragment( savedInstanceState , "weatherBasicData" );
				weatherAdditionalDataFragment= (WeatherAdditionalDataFragment) getSupportFragmentManager().getFragment( savedInstanceState , "weatherAdditionalData" );
				weatherForecastFragment = (WeatherForecastFragment) getSupportFragmentManager().getFragment( savedInstanceState , "weatherForecast" );

				if( weatherBasicDataFragment == null )
					weatherBasicDataFragment = new WeatherBasicDataFragment();
				if( weatherAdditionalDataFragment == null )
					weatherAdditionalDataFragment = new WeatherAdditionalDataFragment();
				if( weatherForecastFragment == null )
					weatherForecastFragment = new WeatherForecastFragment();
			} else {
				weatherBasicDataFragment = new WeatherBasicDataFragment();
				weatherAdditionalDataFragment = new WeatherAdditionalDataFragment();
				weatherForecastFragment = new WeatherForecastFragment();
			}

			Weather savedWeather = readWeatherFor( selectedLocation );
			display( savedWeather );

			ViewPager pager = (ViewPager) findViewById( R.id.pager );
			adapter = new PageAdapter( getSupportFragmentManager() );
			pager.setAdapter( adapter );

			if( isOutdated( savedWeather ) )
				loadWeatherFor( selectedLocation );
		}
	}

	private boolean isOutdated(Weather weather){
		if( weather == null )
			return true;

		Calendar lastUpdateExpiration = Calendar.getInstance();
		lastUpdateExpiration.setTime( weather.lastUpdate );
		lastUpdateExpiration.add( Calendar.MINUTE , weather.timeToLive );
		Calendar now = Calendar.getInstance();

		return now.after( lastUpdateExpiration );
	}

	private Weather readWeatherFor( Location location ){
		WeatherStore weatherStore = new WeatherStore( this );
		return weatherStore.get( location );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate( R.menu.menu_main, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if(id == R.id.action_select_location){
			//resetSelectedLocation();
			displayLocationSelection();
			return true;
		}else if( id == R.id.action_refresh ){
			loadWeatherFor( selectedLocation );
		}else if( id == R.id.action_select_celsius ){
			setUnits( Units.CELSIUS );
			loadWeatherFor( selectedLocation );
		}else if( id == R.id.action_select_fahrenheit ){
			setUnits( Units.FAHRENHEIT );
			loadWeatherFor( selectedLocation );
		}

		return super.onOptionsItemSelected( item );
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
		super.onCreateContextMenu( menu, v, menuInfo );

		MenuInflater inflater = getMenuInflater();
		inflater.inflate( R.menu.location_context_menu , menu );
	}

	@Override
	public void onWeatherLoaded(Location location , Weather weather){
		display( weather );
		weatherBasicDataFragment.reload();
		weatherAdditionalDataFragment.reload();
		weatherForecastFragment.reload();

		WeatherStore weatherStore = new WeatherStore( MainActivity.this );
		weatherStore.put( location , weather );
	}

	@Override
	public void onError(VolleyError error){
		Toast.makeText( MainActivity.this , R.string.cannot_load_weather , Toast.LENGTH_SHORT ).show();
	}


	private void resetSelectedLocation(){
		SharedPreferences preferences = getSharedPreferences( MainActivity.PREFERENCES_NAME , Activity.MODE_PRIVATE );
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove( SELECTED_LOCATION_NAME );
		editor.apply();
	}

	private Location readSelectedLocation(){
		SharedPreferences preferences = getSharedPreferences( MainActivity.PREFERENCES_NAME , Activity.MODE_PRIVATE );
		String selectedLocationName = preferences.getString( SELECTED_LOCATION_NAME, null );

		Location selectedLocation = null;
		if(selectedLocationName != null)
			selectedLocation = new LocationsManager( this ).getLocationByName( selectedLocationName );

		return selectedLocation;
	}

	private void display( Weather weather ){
		if( weather == null )
			return;

		weatherBasicDataFragment.setWeatherData( weather );
		weatherAdditionalDataFragment.setWeatherData( weather );
		weatherForecastFragment.setForecast( weather );
		/*WeatherBasicDataFragment weatherDataFragment = (WeatherBasicDataFragment) getFragmentManager().findFragmentById( R.id.fragment_weather_basic_data );
		weatherDataFragment.setWeatherData( weather );

		WeatherForecastFragment weatherForecastFragment = (WeatherForecastFragment) getFragmentManager().findFragmentById( R.id.fragment_weather_forecast );
		weatherForecastFragment.setForecast( weather.forecast );*/
	}

	private void displayLocationSelection(){
		Intent intent = new Intent( this , SelectLocationActivity.class );
		startActivity( intent );
		//finish();
	}

	private void loadWeatherFor(final Location location){
		if( !isNetworkConnectionAvailable() ){
			Toast.makeText( this, R.string.no_network_connection, Toast.LENGTH_SHORT ).show();
			return;
		}

		WeatherLoader loader = new WeatherLoader( this );
		loader.addOnWeatherLoadedListener( this );

		loader.loadWeather( location, getUnits() );
	}

	private boolean isNetworkConnectionAvailable(){
		return ConnectionChecker.forActivity( this ).isAvailable();
	}

	private void setUnits(Units units){
		SharedPreferences preferences = getSharedPreferences( PREFERENCES_NAME , Context.MODE_PRIVATE );
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString( SELECTED_UNITS_NAME , units.getUnits() );
		editor.commit();
	}

	private String getUnits(){
		SharedPreferences preferences = getSharedPreferences( PREFERENCES_NAME , Context.MODE_PRIVATE );
		return preferences.getString( SELECTED_UNITS_NAME , Units.CELSIUS.getUnits() );
	}

	private class PageAdapter extends FragmentPagerAdapter{

		public PageAdapter(FragmentManager fm){
			super( fm );
		}

		@Override
		public Fragment getItem(int position){
			if( position == 0 ){
				return weatherBasicDataFragment;
			}else if( position == 1 ){
				return weatherAdditionalDataFragment;
			}else if( position == 2 ){
				return weatherForecastFragment;
			}else
				return null;
		}

		@Override
		public int getCount(){
			return 3;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState( outState );
		if( weatherBasicDataFragment != null )
			getSupportFragmentManager().putFragment( outState , "weatherBasicData" , weatherBasicDataFragment );
		if(weatherAdditionalDataFragment != null )
			getSupportFragmentManager().putFragment( outState , "weatherAdditionalData" , weatherAdditionalDataFragment);
		if( weatherForecastFragment != null )
			getSupportFragmentManager().putFragment( outState , "weatherForecast" , weatherForecastFragment );
	}
}
