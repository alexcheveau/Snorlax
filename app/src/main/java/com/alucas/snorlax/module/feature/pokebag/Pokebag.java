package com.alucas.snorlax.module.feature.pokebag;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.alucas.snorlax.common.rx.RxFuncitons;
import com.alucas.snorlax.module.feature.Feature;
import com.alucas.snorlax.module.feature.mitm.MitmEnvelope;
import com.alucas.snorlax.module.feature.mitm.MitmRelay;
import com.alucas.snorlax.module.feature.mitm.MitmUtil;
import com.alucas.snorlax.module.util.Log;

import POGOProtos.Inventory.InventoryItemDataOuterClass.InventoryItemData;
import POGOProtos.Inventory.InventoryItemOuterClass.InventoryItem;
import POGOProtos.Networking.Requests.RequestTypeOuterClass.RequestType;
import POGOProtos.Networking.Responses.GetInventoryResponseOuterClass.GetInventoryResponse;
import rx.Observable;
import rx.Subscription;

@Singleton
public class Pokebag implements Feature {
	private final MitmRelay mMitmRelay;
	private final PokebagPersistence mPokebagPersistence;

	private Subscription mSubscription;

	@Inject
	public Pokebag(final MitmRelay mitmRelay, final PokebagPersistence pokebagPersistence) {
		this.mMitmRelay = mitmRelay;
		this.mPokebagPersistence = pokebagPersistence;
	}

	@Override
	public void subscribe() throws Exception {
		unsubscribe();

		mSubscription = mMitmRelay
			.getObservable()
			.compose(getInventoryResponse())
			.compose(onInventoryResponse())
			.subscribe(mPokebagPersistence::addPokemon)
		;
	}

	@Override
	public void unsubscribe() throws Exception {
		RxFuncitons.unsubscribe(mSubscription);
	}

	private Observable.Transformer<MitmEnvelope, GetInventoryResponse> getInventoryResponse() {
		return observable -> observable
			.flatMap(MitmUtil.filterResponse(RequestType.GET_INVENTORY))
			.flatMap(messages -> Observable.fromCallable(() -> GetInventoryResponse.parseFrom(messages.response)))
			.doOnError(Log::e)
			.onErrorResumeNext(Observable.empty())
			;
	}

	private Observable.Transformer<GetInventoryResponse, PokebagData> onInventoryResponse() {
		return observable -> observable
			.filter(response -> response.getSuccess() && response.hasInventoryDelta())
			.map(GetInventoryResponse::getInventoryDelta)
			.flatMap(inventoryDelta -> Observable.from(inventoryDelta.getInventoryItemsList()))
			.map(InventoryItem::getInventoryItemData)
			.map(InventoryItemData::getPokemonData)
			.map(PokebagData::new)
			;
	}
}
