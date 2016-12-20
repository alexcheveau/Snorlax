package com.alucas.snorlax.module.feature.pokebag;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import android.content.Context;
import android.content.res.Resources;

import com.alucas.snorlax.module.context.pokemongo.PokemonGo;
import com.alucas.snorlax.module.context.snorlax.Snorlax;
import com.alucas.snorlax.module.util.Log;
import com.alucas.snorlax.module.util.Storage;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import timber.log.Timber;

@Singleton
class PokebagPersistence {
	private static final String LOG_PREFIX = "[" + PokebagPersistence.class.getSimpleName() + "] ";

	private static final String INVENTORY_DIRECTORY_NAME = "inventory";
	private static final String POKEMON_FILE_NAME = "pokebag.json";
	private static final Type TYPE_POKEMON = new TypeToken<Set<PokebagData>>() {
	}.getType();

	private final Context mContext;
	private final Resources mResouces;
	private final Gson mGson;

	@Inject
	public PokebagPersistence(@PokemonGo final Context context, @Snorlax Resources resources, final Gson gson) {
		this.mContext = context;
		this.mResouces = resources;
		this.mGson = gson;
	}

	private static Set<PokebagData> loadInventoryPokemon(final Context context, final Resources resources, final Gson gson) {
		final File inventoryPokemonFile = getInventoryPokemonFile(resources);
		if (inventoryPokemonFile == null || !Storage.isExternalStorageReadable(context)) {
			return new HashSet<>();
		}

		return loadInventoryPokemonFile(gson, inventoryPokemonFile);
	}

	private static Set<PokebagData> loadInventoryPokemonFile(final Gson gson, final File inventoryPokemonFile) {
		try (final Reader reader = new FileReader(inventoryPokemonFile)) {
			final Set<PokebagData> inventoryPokemon = gson.fromJson(reader, TYPE_POKEMON);
			if (inventoryPokemon != null) {
				return inventoryPokemon;
			}
		} catch (IOException | JsonIOException | JsonSyntaxException e) {
			Timber.d(e);
		}

		return new HashSet<>();
	}

	private static void saveInventoryPokemon(final Context context, final Resources resources, final Gson gson, final Set<PokebagData> inventoryPokemon) {
		final File inventoryPokemonFile = getInventoryPokemonFile(resources);
		if (inventoryPokemonFile == null || !Storage.isExternalStorageWritable(context)) {
			return;
		}

		saveInventoryPokemonFile(gson, inventoryPokemonFile, inventoryPokemon);
	}

	private static void saveInventoryPokemonFile(final Gson gson, final File inventoryPokemonFile, final Set<PokebagData> inventoryPokemon) {
		try (final Writer writer = new FileWriter(inventoryPokemonFile)) {
			gson.toJson(inventoryPokemon, TYPE_POKEMON, writer);
		} catch (IOException | JsonIOException | JsonSyntaxException e) {
			Timber.e(e);
		}
	}

	private static File getInventoryPokemonFile(final Resources resources) {
		final File inventoryDirectory = new File(Storage.getPublicDirectory(resources), INVENTORY_DIRECTORY_NAME);
		if (!inventoryDirectory.exists()) {
			final boolean mkdirResult = inventoryDirectory.mkdirs();
			if (!mkdirResult) {
				Timber.d("Failed to create directory : %s", inventoryDirectory.getAbsolutePath());
				return null;
			}
		}

		final File pokemonFile = new File(inventoryDirectory, POKEMON_FILE_NAME);
		if (!pokemonFile.exists()) {
			final boolean createResult;
			try {
				createResult = pokemonFile.createNewFile();
			} catch (IOException e) {
				Timber.d(e);
				return null;
			}

			if (createResult) {
				Timber.d("Create file : " + pokemonFile.getAbsolutePath());
			}
		}

		if (!pokemonFile.setReadable(true, false)) {
			Timber.d("Failed to set read permission");
		}

		if (!pokemonFile.setWritable(true, false)) {
			Timber.d("Failed to set write permission");
		}

		return pokemonFile;
	}

	void addPokemon(final PokebagData pokemonData) {
		Log.d(LOG_PREFIX + "Add inventory pokemon : " + pokemonData.pokemonPokedexId);

		final Set<PokebagData> inventoryPokemon = loadInventoryPokemon(mContext, mResouces, mGson);
		inventoryPokemon.add(pokemonData);
		saveInventoryPokemon(mContext, mResouces, mGson, inventoryPokemon);
	}
}
