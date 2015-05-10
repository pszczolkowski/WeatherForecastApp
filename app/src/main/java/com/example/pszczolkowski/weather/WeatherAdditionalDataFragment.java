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

public class WeatherAdditionalDataFragment extends Fragment{

	private Weather weather;
	private View view;
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState){
		view = inflater.inflate( R.layout.fragment_weather_additional_data, container, false );

		reload();

		return view;
	}

	public void setWeatherData( Weather weather ){
		this.weather = weather;
	}

	public void reload(){
		if( view == null || weather == null )
			return;

		((TextView) view.findViewById( R.id.weather_pressure )).setText( "" + weather.atmosphere.pressure );
		((TextView) view.findViewById( R.id.weather_pressure_units )).setText( "" + weather.units.pressure );

		((TextView) view.findViewById( R.id.weather_humidity )).setText( "" + weather.atmosphere.humidity);

		((TextView) view.findViewById( R.id.weather_visibility )).setText( "" + weather.atmosphere.visibility);

		((TextView) view.findViewById( R.id.weather_sunrise )).setText( "" + weather.astronomy.sunRise);
		((TextView) view.findViewById( R.id.weather_sunset)).setText( "" + weather.astronomy.sunSet );
	}

}
