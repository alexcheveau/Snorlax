package com.alucas.snorlax.module.feature.gym;

import java.io.Serializable;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class GymData implements Serializable {
	private static final String GYM_NAME_UNKNOWN = "?";

	public final transient String id;
	@SerializedName("gymName")
	@Nullable
	public final String name;
	@SerializedName("gymLatitude")
	@Nullable
	public final Double latitude;
	@SerializedName("gymLongitude")
	@Nullable
	public final Double longitude;

	GymData(final String gymId, @Nullable final String gymName, @Nullable final Double gymLatitude, @Nullable final Double gymLongitude) {
		this.id = gymId;
		this.name = gymName;
		this.latitude = gymLatitude;
		this.longitude = gymLongitude;
	}

	GymData(final String gymId) {
		this(gymId, GYM_NAME_UNKNOWN, 0.0, 0.0);
	}
}
