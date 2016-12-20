package com.alucas.snorlax.module.feature.gym;

import java.io.Serializable;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class GymData implements Serializable {
	private static final String GYM_NAME_UNKNOWN = "?";

	@SerializedName("gymId")
	public final String id;
	@SerializedName("gymName")
	@Nullable
	public final String name;
	@SerializedName("gymLatitude")
	@Nullable
	public final Double latitude;
	@SerializedName("gymLongitude")
	@Nullable
	public final Double longitude;
	@SerializedName("pokemonNumber")
	@Nullable
	public final Integer pokemonNumber;

	GymData(final String gymId, @Nullable final String gymName, @Nullable final Double gymLatitude, @Nullable final Double gymLongitude, @Nullable final Integer pokemonNumber) {
		this.id = gymId;
		this.name = gymName;
		this.latitude = gymLatitude;
		this.longitude = gymLongitude;
		this.pokemonNumber = pokemonNumber;
	}

	GymData(final String gymId, final Integer pokemonNumber) {
		this(gymId, GYM_NAME_UNKNOWN, 0.0, 0.0, pokemonNumber);
	}
}
