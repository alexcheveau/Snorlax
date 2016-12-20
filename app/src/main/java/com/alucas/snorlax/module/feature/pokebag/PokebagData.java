package com.alucas.snorlax.module.feature.pokebag;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

import POGOProtos.Data.PokemonDataOuterClass.PokemonData;

public class PokebagData implements Serializable {
	@SerializedName("pokemonId")
	public final long pokemonId;
	@SerializedName("pokemonPokedexId")
	public final int pokemonPokedexId;
	@SerializedName("pokemonHeight")
	public final float pokemonHeight;
	@SerializedName("pokemonWeight")
	public final float pokemonWeight;

	PokebagData(final PokemonData pokemonData) {
		this.pokemonId = pokemonData.getId();
		this.pokemonPokedexId = pokemonData.getPokemonIdValue();
		this.pokemonHeight = pokemonData.getHeightM();
		this.pokemonWeight = pokemonData.getWeightKg();
	}
}
