package com.example.pszczolkowski.weather.weather;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Kod stworzony na bazie https://github.com/survivingwithandroid/Swa-app/tree/master/AndroidYahooWeather
 */
public class Weather implements Serializable{

	private final static long serialVersionUID = 6;

	public String imageUrl;

	public Condition condition = new Condition();
	public Wind wind = new Wind();
	public Atmosphere atmosphere = new Atmosphere();
	public List< Forecast > forecast = new ArrayList<>();
	public Location location = new Location();
	public Astronomy astronomy = new Astronomy();
	public Units units = new Units();

	public String day;
	public Date lastUpdate;
	public int timeToLive;

	public class Condition implements Serializable{
		public String description;
		public int code;
		public String date;
		public int temp;
		public int tempMax;
		public int tempMin;
	}

	public class Forecast implements Serializable{
		public int tempMin;
		public int tempMax;
		public String description;
		public String day;
		public String date;
		public int code;
	}

	public static class Atmosphere implements Serializable{
		public int humidity;
		public float visibility;
		public float pressure;
		public int rising;
	}

	public class Wind implements Serializable{
		public int chill;
		public int direction;
		public int speed;
	}

	public class Units implements Serializable{
		public String speed;
		public String distance;
		public String pressure;
		public String temperature;
	}

	public class Location implements Serializable{
		public String name;
		public String region;
		public String country;
		public double latitude;
		public double longitude;
	}

	public class Astronomy implements Serializable{
		public String sunRise;
		public String sunSet;
	}
}

