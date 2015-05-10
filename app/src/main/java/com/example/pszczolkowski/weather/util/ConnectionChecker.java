package com.example.pszczolkowski.weather.util;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionChecker{

	private final Context context;

	private ConnectionChecker( Context context){
		this.context = context;
	}

	public static ConnectionChecker forActivity( Context context ){
		return new ConnectionChecker( context );
	}

	public boolean isAvailable(){
		ConnectivityManager connectivityManager	= (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
