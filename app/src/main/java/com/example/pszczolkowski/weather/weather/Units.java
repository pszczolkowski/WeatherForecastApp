package com.example.pszczolkowski.weather.weather;

public enum Units{
	FAHRENHEIT( "f" ), CELSIUS( "c" );

	private String unit;
	Units( String i ){
		unit = i;
	}

	public String getUnits(){
		return unit;
	}

}
