package com.alucas.snorlax.module.feature.gym;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GymManager {
	private Map<Long, GymData> mPokemonsInGym = new HashMap<>();

	@Inject
	public GymManager() {
		// Must be present because dependency injection
	}

	public void initPokemonInGym(final Map<Long, GymData> pokemons) {
		mPokemonsInGym = pokemons;
	}

	public void removePokemonInGym(final long pokemonUID) {
		mPokemonsInGym.remove(pokemonUID);
	}

	public void addPokemonInGym(final long pokemonUID, GymData gymData) {
		mPokemonsInGym.put(pokemonUID, gymData);
	}

	public boolean wasPokemonInGym(final Long pokemonUID) {
		return mPokemonsInGym.get(pokemonUID) != null;
	}

	public GymData getPokemonInGym(final Long pokemonUID) {
		return mPokemonsInGym.get(pokemonUID);
	}

	public GymData[] getPokemonInGym() {
		final int nbGym = mPokemonsInGym.values().size();
		final GymData[] gyms = new GymData[nbGym];

		return mPokemonsInGym.values().toArray(gyms);
	}
}
