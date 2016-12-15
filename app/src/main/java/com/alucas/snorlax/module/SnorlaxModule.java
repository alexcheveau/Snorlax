/*
 * Copyright (c) 2016. Pedro Diaz <igoticecream@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alucas.snorlax.module;

import java.util.List;

import javax.inject.Singleton;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.util.LongSparseArray;

import com.alucas.snorlax.common.rx.RxBus;
import com.alucas.snorlax.module.feature.mitm.MitmRelay;
import com.alucas.snorlax.module.util.GsonAdapterFactory;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

import dagger.Module;
import dagger.Provides;
import de.robv.android.xposed.XSharedPreferences;

import static POGOProtos.Networking.Requests.RequestOuterClass.Request;

@Module
final class SnorlaxModule {

	private final Application mApplication;
	private final ClassLoader mClassLoader;
	private final XSharedPreferences mXSharedPreferences;

	SnorlaxModule(Application application, ClassLoader classLoader, XSharedPreferences xSharedPreferences) {
		mApplication = application;
		mClassLoader = classLoader;
		mXSharedPreferences = xSharedPreferences;
	}

	@Provides
	ClassLoader provideClassLoader() {
		return mClassLoader;
	}

	@Provides
	XSharedPreferences provideXSharedPreferences() {
		return mXSharedPreferences;
	}

	@Provides
	Application provideAppliction() {
		return mApplication;
	}

	@Provides
	@Singleton
	MitmRelay provideMitmRelay() {
		return MitmRelay.getInstance();
	}

	@Provides
	@Singleton
	LongSparseArray<List<Request>> provideLongSparseArray() {
		return new LongSparseArray<>();
	}

	@Provides
	RxBus provideRxBus() {
		return RxBus.getInstance();
	}

	@Provides
	@Singleton
	AlarmManager provideAlarmManager() {
		return (AlarmManager) mApplication.getSystemService(Context.ALARM_SERVICE);
	}
}
