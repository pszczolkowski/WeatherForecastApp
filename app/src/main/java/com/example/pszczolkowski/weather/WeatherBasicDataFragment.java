package com.example.pszczolkowski.weather;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pszczolkowski.weather.util.LoadImageTask;
import com.example.pszczolkowski.weather.weather.Weather;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class WeatherBasicDataFragment extends Fragment{

	private Weather weather;
	private View view;
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState){
		view = inflater.inflate( R.layout.fragment_weather_basic_data, container, false );

		reload();

		return view;
	}

	public void setWeatherData( Weather weather ){
		this.weather = weather;
	}

	public void reload(){
		if( view == null || weather == null )
			return;

		((TextView) view.findViewById( R.id.weather_location_name )).setText( "" + weather.location.name );
		((TextView) view.findViewById( R.id.weather_time )).setText( "" + dateFormat.format( weather.lastUpdate ) );
		((TextView) view.findViewById( R.id.weather_temperature )).setText( "" + weather.condition.temp );
		((TextView) view.findViewById( R.id.weather_temperature_min )).setText( "" + weather.condition.tempMin );
		((TextView) view.findViewById( R.id.weather_temperature_max )).setText( "" + weather.condition.tempMax );
		((TextView) view.findViewById( R.id.weather_temperature_units )).setText( "" + weather.units.temperature );
		((TextView) view.findViewById( R.id.weather_description )).setText( "" + weather.condition.description);

		((TextView) view.findViewById( R.id.weather_geographic_coordinates )).setText( weather.location.latitude + ", " + weather.location.longitude );

		// WCZYTANIE OBRAZKA W ASYNCHRONICZNYM ZADANIU
		ImageView weatherImage = (ImageView) view.findViewById( R.id.weather_image );
		if( weather.imageUrl != null )
			new LoadImageTask( getActivity() , weatherImage ).execute( weather.imageUrl );
	}

}
