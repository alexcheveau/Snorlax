package com.alucas.snorlax.module.feature.gym;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Pair;

import com.alucas.snorlax.common.rx.RxFuncitons;
import com.alucas.snorlax.module.context.snorlax.Snorlax;
import com.alucas.snorlax.module.feature.Feature;
import com.alucas.snorlax.module.util.Log;
import com.alucas.snorlax.module.util.Storage;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import POGOProtos.Data.PokemonDataOuterClass.PokemonData;
import rx.Subscription;
import timber.log.Timber;

@Singleton
public class GymPersistence implements Feature {
	private static final String LOG_PREFIX = "[" + GymPersistence.class.getSimpleName() + "] ";

	private static final String PREF_POKEMON_IN_GYM = "pokemonInGym";
	private static final String GYM_DIRECTORY_NAME = "gym";
	private static final String POKEMON_IN_GYM_FILE_NAME = "pokemonInGym.json";
	private static final Type TYPE_POKEMON_IN_GYM = new TypeToken<Map<Long, GymData>>() {
	}.getType();

	private final Context mContext;
	private final Resources mResouces;
	private final Gson mGson;
	private final Gym mGym;
	private final GymManager mGymManager;

	private Subscription mSubscription;

	@Inject
	public GymPersistence(@Snorlax final Context context, @Snorlax Resources resources, final Gson gson, final Gym gym, final GymManager gymManager) {
		this.mContext = context;
		this.mResouces = resources;
		this.mGson = gson;
		this.mGym = gym;
		this.mGymManager = gymManager;

		final Map<Long, GymData> pokemonsInGym = loadPokemonInGym(mContext, mResouces, mGson);
		if (pokemonsInGym != null) {
			this.mGymManager.initPokemonInGym(pokemonsInGym);
		}
	}

	@Override
	public void subscribe() {
		mSubscription = mGym
			.getObservable()
			.subscribe(pair -> {
				final Pair<PokemonData, GymData> pokemonInfo = pair.second;
				final PokemonData pokemon = pokemonInfo.first;
				final GymData gymData = pokemonInfo.second;
				switch (pair.first) {
					case POKEMON_ADD:
						addPokemonInGym(pokemon.getId(), gymData);
						break;
					case POKEMON_REMOVE:
						removePokemonInGym(pokemon.getId());
						break;
					default:
						break;
				}
			});
	}

	@Override
	public void unsubscribe() {
		RxFuncitons.unsubscribe(mSubscription);
	}

	public static Map<Long, GymData> loadPokemonInGym(final Context context, final Resources resources, final Gson gson) {
		final File pokemonInGymFile = getPokemonInGymFile(resources);
		if (pokemonInGymFile == null || !Storage.isExternalStorageReadable(context)) {
			return null;
		}

		final Map<Long, GymData> pokemonsInGym = loadPokemonInGymFile(context, gson, pokemonInGymFile);

		// TODO : old storage system, delete in next version
		final SharedPreferences settings = context.getSharedPreferences(PREF_POKEMON_IN_GYM, 0);
		final Map<String, ?> pokemonsInGymRaw = settings.getAll();
		for (final Map.Entry<String, ?> pokemonEntry : pokemonsInGymRaw.entrySet()) {
			final String gymIdString = pokemonEntry.getKey();
			Long gymId;
			try {
				gymId = Long.decode(gymIdString);
			} catch (NumberFormatException e) {
				Log.e(e);
				continue;
			}

			final GymData gymData = jsonToGymData(gson, (String) pokemonEntry.getValue());

			pokemonsInGym.put(gymId, gymData);
		}

		return pokemonsInGym;
	}

	private static Map<Long, GymData> loadPokemonInGymFile(final Context context, final Gson gson, final File pokemonInGymFile) {
		try (final Reader reader = new FileReader(pokemonInGymFile)) {
			return gson.fromJson(reader, TYPE_POKEMON_IN_GYM);
		} catch (IOException | JsonIOException | JsonSyntaxException e) {
			e.printStackTrace();
		}

		return new Hashtable<>();
	}

	private static void savePokemonInGym(final Context context, final Resources resources, final Gson gson, final Map<Long, GymData> pokemonsInGym) {
		final File pokemonInGymFile = getPokemonInGymFile(resources);
		if (pokemonInGymFile == null || !Storage.isExternalStorageWritable(context)) {
			return;
		}

		savePokemonInGymFile(gson, pokemonInGymFile, pokemonsInGym);
	}

	private static void savePokemonInGymFile(final Gson gson, final File pokemonInGymFile, final Map<Long, GymData> pokemonsInGym) {
		try (final Writer writer = new FileWriter(pokemonInGymFile)) {
			gson.toJson(pokemonsInGym, TYPE_POKEMON_IN_GYM, writer);
		} catch (IOException | JsonIOException | JsonSyntaxException e) {
			Log.e(e);
		}
	}

	private static File getPokemonInGymFile(final Resources resources) {
		final File gymDirectory = new File(Storage.getPublicDirectory(resources), GYM_DIRECTORY_NAME);
		if (!gymDirectory.exists()) {
			final boolean mkdirResult = gymDirectory.mkdirs();
			if (!mkdirResult) {
				Timber.d("Failed to create directory : " + gymDirectory.getAbsolutePath());
				return null;
			}
		}

		final File pokemonInGymFile = new File(gymDirectory, POKEMON_IN_GYM_FILE_NAME);
		if (!pokemonInGymFile.exists()) {
			final boolean createResult;
			try {
				createResult = pokemonInGymFile.createNewFile();
			} catch (IOException e) {
				Timber.d(e);
				return null;
			}

			if (createResult) {
				Timber.d("Create file : " + pokemonInGymFile.getAbsolutePath());
			}
		}

		if (!pokemonInGymFile.setReadable(true, false)) {
			Timber.d("Failed to set read permission");
		}
		if (!pokemonInGymFile.setWritable(true, false)) {
			Timber.d("Failed to set write permission");
		}

		return pokemonInGymFile;
	}

	private static GymData jsonToGymData(final Gson gson, final String gymDataJson) {
		try {
			return gson.fromJson((String) gymDataJson, GymData.class);
		} catch (JsonSyntaxException e) {
			Log.e(e);
		}

		return new GymData(gymDataJson);
	}

	@SuppressWarnings("unused")
	private void addPokemonInGym(final long pokemonUID, final GymData gymData) {
		Log.d(LOG_PREFIX + "addPokemonInGym { pokemonUID : '" + pokemonUID + "', gymUID : '" + gymData.id + "'");

		final Map<Long, GymData> pokemonsInGym = loadPokemonInGym(mContext, mResouces, mGson);
		if (pokemonsInGym == null) {
			return;
		}

		pokemonsInGym.put(pokemonUID, gymData);
		savePokemonInGym(mContext, mResouces, mGson, pokemonsInGym);

		mGymManager.addPokemonInGym(pokemonUID, gymData);
	}

	@SuppressWarnings("unused")
	private void removePokemonInGym(final long pokemonUID) {
		Log.d(LOG_PREFIX + "removePokemonInGym : " + pokemonUID);

		final Map<Long, GymData> pokemonsInGym = loadPokemonInGym(mContext, mResouces, mGson);
		if (pokemonsInGym == null) {
			return;
		}

		pokemonsInGym.remove(pokemonUID);
		savePokemonInGym(mContext, mResouces, mGson, pokemonsInGym);

		// TODO : old storage system, delete in next version
		final SharedPreferences settings = mContext.getSharedPreferences(PREF_POKEMON_IN_GYM, 0);
		final SharedPreferences.Editor editor = settings.edit();
		editor.remove(String.valueOf(pokemonUID));
		editor.apply();

		mGymManager.removePokemonInGym(pokemonUID);
	}
}
