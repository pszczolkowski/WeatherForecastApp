package com.example.pszczolkowski.weather.location;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.pszczolkowski.weather.R;

import java.util.List;

public class LocationsAdapter extends ArrayAdapter< Location >{
	private final Context context;

	public LocationsAdapter(Context context, int resource, List<Location> objects){
		super( context, resource, objects );
		this.context = context;
	}

	@Override
	public View getView(int position, View row, ViewGroup parent){
		if( row == null ){
			LayoutInflater inflater = LayoutInflater.from( context );
			row = inflater.inflate( R.layout.location_item, parent, false );
		}

		TextView locationName = (TextView) row.findViewById( R.id.location_item_name );
		locationName.setText( getItem( position ).getName() );

		return row;
	}
}
