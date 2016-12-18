package com.alucas.snorlax.app.widget;

import java.util.Map;

import javax.inject.Inject;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.alucas.snorlax.R;
import com.alucas.snorlax.app.SnorlaxApp;
import com.alucas.snorlax.module.feature.gym.GymData;
import com.alucas.snorlax.module.feature.gym.GymManager;
import com.alucas.snorlax.module.feature.gym.GymPersistence;
import com.google.gson.Gson;

public class ListPokemonInGymPreference extends Preference {
	@Inject
	Gson mGson;
	@Inject
	GymManager mGymManager;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ListPokemonInGymPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	public ListPokemonInGymPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public ListPokemonInGymPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ListPokemonInGymPreference(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		((SnorlaxApp) context.getApplicationContext()).getComponent().inject(this);

		final Map<Long, GymData> pokemonsInGym = GymPersistence.loadPokemonInGym(context, context.getResources(), mGson);
		if (pokemonsInGym != null) {
			mGymManager.initPokemonInGym(pokemonsInGym);
		}

		setWidgetLayoutResource(R.layout.preference_button);
	}

	@Override
	public void onBindViewHolder(PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);

		final View button = holder.findViewById(R.id.button);
		if (button == null) {
			return;
		}

//		final Uri posURI = Uri.parse("geo:" + gymLatitude + "," + gymLongitude + "?q=" + gymLatitude + "," + gymLongitude + "(" + gymName + ")");
//		final Intent posIntent = new Intent(Intent.ACTION_VIEW, posURI).setPackage("com.google.android.apps.maps");
//		final PendingIntent posPendingIntent = PendingIntent.getActivity(mPokemonGoContext, 0, posIntent, 0);

		button.setClickable(true);
		button.setOnClickListener(view -> Toast.makeText(getContext(), "Nb pokemon in gym : " + mGymManager.getPokemonInGymUID().size(), Toast.LENGTH_SHORT).show());
	}
}
