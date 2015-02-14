package com.cunoraz.locationapi;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cunoraz.locationapi.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends Activity implements ConnectionCallbacks,
		OnConnectionFailedListener {

	// LogCat tag
	private static final String TAG = MainActivity.class.getSimpleName();

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

	private Location mLastLocation;

	// Google API ile etkileþime geçicek olan Google client 
	private GoogleApiClient mGoogleApiClient;

	private LocationRequest mLocationRequest;

	// UI elemanlarý
	private TextView cordinatesText;
	private TextView addressText;
	private Button btnShowLocation;

	//enlem ve boylam
	double latitude;
	double longitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		cordinatesText = (TextView) findViewById(R.id.coordinatesText);
		btnShowLocation = (Button) findViewById(R.id.btnShowAddress);
		addressText = (TextView) findViewById(R.id.addressText);

		//Ýlk olarak play services hizmetini kontrol ediyoruz
		if (checkPlayServices()) {

			//GoogleApi client build ediliyor
			buildGoogleApiClient();

			createLocationRequest();
		}

		
		btnShowLocation.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				getAdress(latitude, longitude);
			}
		});

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mGoogleApiClient != null) {
			mGoogleApiClient.connect();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		checkPlayServices();

	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	/**
	 * Kordinatlarý ekranda gösteriyoruz
	 * */
	private void displayLocation() {

		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);

		if (mLastLocation != null) {
			latitude = mLastLocation.getLatitude();
			longitude = mLastLocation.getLongitude();

			cordinatesText.setText(latitude + ", " + longitude);

		} else {

			cordinatesText
					.setText("(Couldn't get the location. Make sure location is enabled on the device)");
		}
	}

	/*
	 * Enlem ve boylam bilgisinden açýk adres elde ediyoruz
	 */
	private void getAdress(double latitude, double longitude) {
		Geocoder geocoder = new Geocoder(this, Locale.getDefault());

		List<Address> addresses;
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 1);
			String address = addresses.get(0).getAddressLine(0);
			address += addresses.get(0).getAddressLine(1);
			address += addresses.get(0).getAddressLine(2);
			address += addresses.get(0).getAddressLine(3);
			addressText.setText(address);
			
			//burada cesitli ozel alanlar çekilebiliyor
			String il = addresses.get(0).getLocality();
			String ilce = addresses.get(0).getSubAdminArea();
			String ulke = addresses.get(0).getCountryName();
			String postaKodu = addresses.get(0).getPostalCode();
			String ulkeKodu = addresses.get(0).getCountryCode();
			Log.d(TAG, il + " " + ilce + " " + ulke + " " + postaKodu + " "
					+ ulkeKodu);
			
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this, "(Couldn't get the location. Make sure location is enabled on the device)", Toast.LENGTH_LONG).show();
		}

	}

	/**
	 *  google api client objesi oluþturuyoruz
	 * */
	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}

	/**
	 * Location request objesi  oluþturuyoruz
	 * */
	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	}

	/**
	 *  google play hizmetlerini kontrol ediyoruz 
	 * */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Toast.makeText(getApplicationContext(),
						"This device is not supported.", Toast.LENGTH_LONG)
						.show();
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Baðlantý kontrol fonksiyonu
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
				+ result.getErrorCode());
	}

	@Override
	public void onConnected(Bundle arg0) {

		// baðlandýðýmýzda kordinatlarý göstermesi icin cagýrýyoruz
		displayLocation();

	}

	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
	}

}
