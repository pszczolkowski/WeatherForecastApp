package com.example.pszczolkowski.weather.location;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Location implements Serializable{

	private static final long serialVersionUID = 3;

	private String woeid;
	private String name;
	private String country;
	Map< String , String > admin = new HashMap<>();

	Location(){}

	public Location(String name){
		if( name == null )
			throw new IllegalArgumentException( "name cannot be null" );
		if( name.trim().isEmpty() )
			throw new IllegalArgumentException( "name cannot be empty" );

		this.name = name;
	}

	void setName( String name ){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public String getWoeid(){
		return woeid;
	}

	public void setWoeid(String woeid){
		this.woeid = woeid;
	}

	public String getCountry(){
		return country;
	}

	public void putAdmin( String type , String value ){
		admin.put( type , value );
	}

	public Map< String , String > getAdmin(){
		return new HashMap<>( admin );
	}

	public void setCountry(String country){
		this.country = country;
	}

	@Override
	public boolean equals(Object o){
		if(this == o)
			return true;

		if( o instanceof Location ){
			Location location = (Location) o;

			return Objects.equals( name , location.name ) && Objects.equals( country , location.country );
		}else
			return false;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode( name );
	}

	@Override
	public String toString(){
		return name;
	}
}
