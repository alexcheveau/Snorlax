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

import javax.inject.Inject;

import android.app.Application;

import com.alucas.snorlax.BuildConfig;
import com.alucas.snorlax.app.SnorlaxApp;
import com.alucas.snorlax.module.feature.FeatureHelper;
import com.alucas.snorlax.module.feature.broadcast.Broadcast;
import com.alucas.snorlax.module.feature.capture.Capture;
import com.alucas.snorlax.module.feature.collect.CollectDefender;
import com.alucas.snorlax.module.feature.encounter.Encounter;
import com.alucas.snorlax.module.feature.gamemaster.GameMaster;
import com.alucas.snorlax.module.feature.gym.Eject;
import com.alucas.snorlax.module.feature.gym.Gym;
import com.alucas.snorlax.module.feature.gym.GymPersistence;
import com.alucas.snorlax.module.feature.lure.Lure;
import com.alucas.snorlax.module.feature.mitm.Mitm;
import com.alucas.snorlax.module.feature.mock.Mock;
import com.alucas.snorlax.module.feature.pokebag.Pokebag;
import com.alucas.snorlax.module.feature.rename.Rename;
import com.alucas.snorlax.module.feature.safetynet.SafetyNet;
import com.alucas.snorlax.module.feature.traffic.Traffic;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Snorlax implements IXposedHookLoadPackage, IXposedHookZygoteInit {

	@Inject
	SafetyNet mSafetyNet;
	@Inject
	Mitm mMitm;
	@Inject
	GameMaster mGameMaster;
	@Inject
	Mock mMock;
	@Inject
	Capture mCapture;
	@Inject
	Encounter mEncounter;
	@Inject
	Rename mRename;
	@Inject
	Broadcast mBroadcast;
	@Inject
	Lure mLure;
	@Inject
	Traffic mTraffic;
	@Inject
	Gym mGym;
	@Inject
	GymPersistence mGymPersistence;
	@Inject
	Eject mEject;
	@Inject
	CollectDefender mCollectDefender;
	@Inject
	Pokebag mPokebag;

	private XSharedPreferences mXSharedPreferences;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mXSharedPreferences = new XSharedPreferences(BuildConfig.SNORLAX_ID);
		mXSharedPreferences.makeWorldReadable();
	}

	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
		final ClassLoader classLoader = lpparam.classLoader;

		if (lpparam.packageName.equals(BuildConfig.POKEMON_GO_ID)) {
			handlePokemonGoLoadPackage(classLoader);
		}

		if (lpparam.packageName.equals(BuildConfig.SNORLAX_ID)) {
			handleSnorlaxLoadPackage(classLoader);
		}
	}

	private void handlePokemonGoLoadPackage(final ClassLoader classLoader) {
		XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				getComponent((Application) param.thisObject, classLoader, mXSharedPreferences).inject(Snorlax.this);

				FeatureHelper.subscribe(
					mSafetyNet,
					mMitm,
					mGameMaster,
					mMock,
					mCapture,
					mEncounter,
					mRename,
					mBroadcast,
					mLure,
					mTraffic,
					mGym,
					mGymPersistence,
					mEject,
					mCollectDefender,
					mPokebag
				);
			}
		});
		XposedHelpers.findAndHookMethod(Application.class, "onTerminate", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				FeatureHelper.unsubscribe(
					mSafetyNet,
					mMitm,
					mGameMaster,
					mMock,
					mCapture,
					mEncounter,
					mRename,
					mBroadcast,
					mLure,
					mTraffic,
					mGym,
					mEject,
					mCollectDefender,
					mPokebag
				);
			}
		});
	}

	private void handleSnorlaxLoadPackage(final ClassLoader classLoader) {
		XposedHelpers.findAndHookMethod(SnorlaxApp.class.getName(), classLoader, "isEnabled", XC_MethodReplacement.returnConstant(true));
	}

	private SnorlaxComponent getComponent(Application application, ClassLoader classLoader, XSharedPreferences preferences) {
		return DaggerSnorlaxComponent.builder()
			.snorlaxModule(new SnorlaxModule(application, classLoader, preferences))
			.build();
	}
}
