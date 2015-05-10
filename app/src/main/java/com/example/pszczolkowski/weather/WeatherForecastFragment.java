package com.example.pszczolkowski.weather;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pszczolkowski.weather.weather.Weather;

import java.util.List;

public class WeatherForecastFragment extends Fragment{

	private View view;
	//private List< Weather.Forecast > forecast;
	private Weather weather;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState){
		view = inflater.inflate( R.layout.fragment_weather_forecast, container, false );

		reload();

		return view;
	}

	public void reload(){
		if( getActivity() == null || weather == null )
			return;

		LayoutInflater layoutInflater = getActivity().getLayoutInflater();
		LinearLayout forecastContainer = (LinearLayout) view.findViewById( R.id.weather_forecast_items );

		forecastContainer.removeAllViews();

		for(Weather.Forecast item : weather.forecast){
			LinearLayout layoutItem = (LinearLayout) layoutInflater.inflate( R.layout.forecast_item, null );

			( (TextView) layoutItem.findViewById( R.id.forecast_item_day ) ).setText( item.day );
			( (TextView) layoutItem.findViewById( R.id.forecast_item_date ) ).setText( item.date );
			( (TextView) layoutItem.findViewById( R.id.forecast_item_description ) ).setText( item.description );
			( (TextView) layoutItem.findViewById( R.id.forecast_item_low ) ).setText( "" + item.tempMin );
			( (TextView) layoutItem.findViewById( R.id.forecast_item_high ) ).setText( "" + item.tempMax );
			( (TextView) layoutItem.findViewById( R.id.forecast_item_units ) ).setText( "" + weather.units.temperature );

			forecastContainer.addView( layoutItem );
		}
	}

	//public void setForecast( List< Weather.Forecast > forecast ){
	public void setForecast( Weather weather ){
		this.weather = weather;
	}

}
