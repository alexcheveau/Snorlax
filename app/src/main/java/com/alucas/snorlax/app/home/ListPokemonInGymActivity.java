package com.alucas.snorlax.app.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.alucas.snorlax.BuildConfig;
import com.alucas.snorlax.R;
import com.alucas.snorlax.module.feature.gym.GymData;
import com.alucas.snorlax.module.util.Resource;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class ListPokemonInGymActivity extends FragmentActivity implements OnMapReadyCallback {

	private GoogleMap mMap;
	private GymData[] mGyms;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		mGyms = (GymData[]) getIntent().getExtras().get(BuildConfig.EXTRA_LIST_POKEMON_IN_GYM);
	}


	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		if (mGyms == null || mGyms.length == 0) {
			return;
		}

		final LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
		for (int i = 0; i < mGyms.length; i++) {
			final GymData gymData = mGyms[i];
			if (gymData.latitude == 0 && gymData.longitude == 0) {
				continue;
			}

			final LatLng gymPos = new LatLng(gymData.latitude, gymData.longitude);
			final BitmapDescriptor pokemonIcon = (gymData.pokemonNumber == null)
				? BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
				: BitmapDescriptorFactory.fromBitmap(
				Bitmap.createScaledBitmap(
					BitmapFactory.decodeResource(
						getResources(),
						Resource.getPokemonResourceId(this, getResources(), gymData.pokemonNumber)
					),
					Resource.getLargeIconWidth(getResources()),
					Resource.getLargeIconHeight(getResources()),
					false
				));

			boundsBuilder.include(gymPos);

			mMap.addMarker(
				new MarkerOptions()
					.position(gymPos)
					.title(gymData.name)
					.icon(pokemonIcon));
		}

		mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 2));
	}
}
