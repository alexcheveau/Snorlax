package com.alucas.snorlax.app.widget;

import java.util.Map;

import javax.inject.Inject;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.alucas.snorlax.BuildConfig;
import com.alucas.snorlax.R;
import com.alucas.snorlax.app.SnorlaxApp;
import com.alucas.snorlax.app.home.ListPokemonInGymActivity;
import com.alucas.snorlax.module.feature.gym.GymData;
import com.alucas.snorlax.module.feature.gym.GymManager;
import com.alucas.snorlax.module.feature.gym.GymPersistence;
import com.google.gson.Gson;

public class ListPokemonInGymPreference extends Preference {
	@Inject
	@SuppressWarnings("squid:S3306")
	Gson mGson;
	@Inject
	@SuppressWarnings("squid:S3306")
	GymManager mGymManager;

	private Context mContext;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@SuppressWarnings("unused")
	public ListPokemonInGymPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	@SuppressWarnings("unused")
	public ListPokemonInGymPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@SuppressWarnings("unused")
	public ListPokemonInGymPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@SuppressWarnings("unused")
	public ListPokemonInGymPreference(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		((SnorlaxApp) context.getApplicationContext()).getComponent().inject(this);

		this.mContext = context;

		setWidgetLayoutResource(R.layout.preference_button);
	}

	@Override
	public void onBindViewHolder(PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);

		final View button = holder.findViewById(R.id.button);
		if (button == null) {
			return;
		}

		button.setClickable(true);
		button.setOnClickListener(view -> {
			final Map<Long, GymData> pokemonsInGym = GymPersistence.loadPokemonInGym(mContext, mContext.getResources(), mGson);
			if (pokemonsInGym == null) {
				Toast.makeText(getContext(), "Failed to load pokemons in gym", Toast.LENGTH_SHORT).show();
				return;
			}

			mGymManager.initPokemonInGym(pokemonsInGym);
			mGymManager.getPokemonInGym();

			if (mGymManager.getPokemonInGym().length == 0) {
				Toast.makeText(getContext(), "You don't have any pokemon in gym", Toast.LENGTH_SHORT).show();
				return;
			}

			final Intent mapIntent = new Intent(mContext, ListPokemonInGymActivity.class);
			mapIntent.putExtra(BuildConfig.EXTRA_LIST_POKEMON_IN_GYM, mGymManager.getPokemonInGym());
			mContext.startActivity(mapIntent);
		});
	}
}
