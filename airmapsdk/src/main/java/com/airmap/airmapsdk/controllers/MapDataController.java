package com.airmap.airmapsdk.controllers;

import android.graphics.RectF;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.status.AirMapAdvisory;
import com.airmap.airmapsdk.models.status.AirMapAirspaceStatus;
import com.airmap.airmapsdk.models.rules.AirMapJurisdiction;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.ui.views.AirMapMapView;
import com.airmap.airmapsdk.util.ThrottleablePublishSubject;
import com.google.gson.JsonObject;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.geometry.VisibleRegion;
import com.mapbox.services.commons.geojson.Feature;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;

/**
 * Created by collin@airmap.com on 9/26/17.
 */

public class MapDataController {

    private static final String TAG = "MapDataController";

    private AirMapMapView map;

    private ThrottleablePublishSubject<Coordinate> jurisdictionsPublishSubject;
    private PublishSubject<AirMapMapView.Configuration> configurationPublishSubject;
    private Subscription rulesetsSubscription;

    private List<AirMapRuleset> selectedRulesets;
    private List<AirMapRuleset> availableRulesets;
    private AirMapAirspaceStatus airspaceStatus;

    private Callback callback;

    public MapDataController(AirMapMapView map, AirMapMapView.Configuration configuration, Callback callback) {
        this.map = map;
        this.callback = callback;

        jurisdictionsPublishSubject = ThrottleablePublishSubject.create();
        configurationPublishSubject = PublishSubject.create();

        setupSubscriptions(configuration);
    }

    public void configure(AirMapMapView.Configuration configuration) {
        configurationPublishSubject.onNext(configuration);
    }

    private void setupSubscriptions(AirMapMapView.Configuration configuration) {
        // observes changes to jurisdictions (map bounds) to query rulesets for the region
        Observable<Map<String, AirMapRuleset>> jurisdictionsObservable = jurisdictionsPublishSubject.asObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<Coordinate>() {
                    @Override
                    public void call(Coordinate coordinate) {
                        if (callback != null) {
                            callback.onAdvisoryStatusLoading();
                        }
                    }
                })
                .map(new Func1<Coordinate, Pair<Map<String, AirMapRuleset>, List<AirMapJurisdiction>>>() {
                    @Override
                    public Pair<Map<String, AirMapRuleset>,List<AirMapJurisdiction>> call(Coordinate coordinate) {
                        // query map for jurisdictions
                        List<Feature> features = map.getMap().queryRenderedFeatures(new RectF(map.getLeft(),
                                map.getTop(), map.getRight(), map.getBottom()), "jurisdictions");

                        Map<String, AirMapRuleset> jurisdictionRulesets = new HashMap<>();
                        List<AirMapJurisdiction> jurisdictions = new ArrayList<>();
                        for (Feature feature : features) {
                            try {
                                JsonObject propertiesJSON = feature.getProperties();
                                JSONObject jurisdictionJSON = new JSONObject(propertiesJSON.get("jurisdiction").getAsString());

                                AirMapJurisdiction jurisdiction = new AirMapJurisdiction(jurisdictionJSON);
                                jurisdictions.add(jurisdiction);
                                for (AirMapRuleset ruleset : jurisdiction.getRulesets()) {
                                    jurisdictionRulesets.put(ruleset.getId(), ruleset);
                                }
                            } catch (JSONException e) {
                                AirMapLog.e(TAG, "Unable to get jurisdiction json", e);
                            }
                        }
                        AirMapLog.i(TAG, "Jurisdictions loaded: " + TextUtils.join(",", jurisdictionRulesets.keySet()));

                        return new Pair<>(jurisdictionRulesets,jurisdictions);
                    }
                })
                .map(new Func1<Pair<Map<String, AirMapRuleset>, List<AirMapJurisdiction>>, Map<String, AirMapRuleset>>() {
                    @Override
                    public Map<String, AirMapRuleset> call(Pair<Map<String, AirMapRuleset>, List<AirMapJurisdiction>> pair) {
                        return pair.first;
                    }
                });

        // observes changes to preferred rulesets to trigger advisories fetch
        Observable<AirMapMapView.Configuration> preferredRulesetsObservable = configurationPublishSubject
                .startWith(configuration)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Action1<AirMapMapView.Configuration>() {
                    @Override
                    public void call(AirMapMapView.Configuration configuration) {
                        if (callback != null) {
                            callback.onAdvisoryStatusLoading();
                        }

                        switch (configuration.type) {
                            case AUTOMATIC:
                                AirMapLog.i(TAG, "AirMapMapView updated to automatic configuration");
                                break;
                            case DYNAMIC:
                                AirMapLog.i(TAG, "AirMapMapView updated to dynamic configuration w/ preferred rulesets: " +
                                        TextUtils.join(",", ((AirMapMapView.DynamicConfiguration) configuration).preferredRulesetIds));
                                break;
                            case MANUAL:
                                AirMapLog.i(TAG, "AirMapMapView updated to manual configuration w/ preferred rulesets: " +
                                        TextUtils.join(",", ((AirMapMapView.ManualConfiguration) configuration).selectedRulesets));
                                break;
                        }
                    }
                });

        // combines preferred rulesets and available rulesets changes
        // to calculate selected rulesets and advisories
        rulesetsSubscription = Observable.combineLatest(jurisdictionsObservable, preferredRulesetsObservable,
                new Func2<Map<String, AirMapRuleset>, AirMapMapView.Configuration, Pair<List<AirMapRuleset>,List<AirMapRuleset>>>() {
                    @Override
                    public Pair<List<AirMapRuleset>,List<AirMapRuleset>> call(Map<String, AirMapRuleset> availableRulesetsMap, AirMapMapView.Configuration configuration) {
                        AirMapLog.i(TAG, "combine available rulesets & configuration");
                        List<AirMapRuleset> availableRulesets = new ArrayList<>(availableRulesetsMap.values());
                        List<AirMapRuleset> selectedRulesets = RulesetsEvaluator.computeSelectedRulesets(availableRulesets, configuration);

                        return new Pair<>(availableRulesets, selectedRulesets);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<Pair<List<AirMapRuleset>,List<AirMapRuleset>>, Boolean>() {
                    @Override
                    public Boolean call(Pair<List<AirMapRuleset>,List<AirMapRuleset>> rulesets) {
                        return rulesets != null;
                    }
                })
                .doOnNext(new Action1<Pair<List<AirMapRuleset>,List<AirMapRuleset>>>() {
                    @Override
                    public void call(Pair<List<AirMapRuleset>, List<AirMapRuleset>> pair) {
                        AirMapLog.i(TAG, "Computed rulesets: " + TextUtils.join(",", pair.second));
                        List<AirMapRuleset> availableRulesetsList = pair.first != null ? new ArrayList<>(pair.first) : null;
                        List<AirMapRuleset> selectedRulesetsList = pair.second != null ? new ArrayList<>(pair.second) : null;
                        List<AirMapRuleset> previouslySelectedRulesetsList = selectedRulesets != null ? new ArrayList<>(selectedRulesets) : null;

                        callback.onRulesetsUpdated(availableRulesetsList, selectedRulesetsList, previouslySelectedRulesetsList);
                        availableRulesets = pair.first;
                        selectedRulesets = pair.second;
                    }
                })
                .map(new Func1<Pair<List<AirMapRuleset>,List<AirMapRuleset>>, List<AirMapRuleset>>() {
                    @Override
                    public List<AirMapRuleset> call(Pair<List<AirMapRuleset>, List<AirMapRuleset>> pair) {
                        return pair.second;
                    }
                })
                .flatMap(convertRulesetsToAdvisories())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Func1<Throwable, AirMapAirspaceStatus>() {
                    @Override
                    public AirMapAirspaceStatus call(Throwable throwable) {
                        AirMapLog.e(TAG, "onErrorReturn", throwable);
                        return null;
                    }
                })
                .subscribe(new Action1<AirMapAirspaceStatus>() {
                    @Override
                    public void call(AirMapAirspaceStatus advisoryStatus) {
                        airspaceStatus = advisoryStatus;
                        callback.onAdvisoryStatusUpdated(advisoryStatus);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AirMapLog.e(TAG, "Unknown error on jurisdictions observable", throwable);
                    }
                });
    }

    /**
     *  Fetches advisories based on map bounds and selected rulesets
     *
     *  @return
     */
    private Func1<List<AirMapRuleset>, Observable<AirMapAirspaceStatus>> convertRulesetsToAdvisories() {
        return new Func1<List<AirMapRuleset>, Observable<AirMapAirspaceStatus>>() {
            @Override
            public Observable<AirMapAirspaceStatus> call(List<AirMapRuleset> selectedRulesets) {
                VisibleRegion region = map.getMap().getProjection().getVisibleRegion();
                LatLngBounds bounds = region.latLngBounds;
                List<Coordinate> coordinates = new ArrayList<>();
                coordinates.add(new Coordinate(bounds.getLatNorth(), bounds.getLonWest()));
                coordinates.add(new Coordinate(bounds.getLatNorth(), bounds.getLonEast()));
                coordinates.add(new Coordinate(bounds.getLatSouth(), bounds.getLonEast()));
                coordinates.add(new Coordinate(bounds.getLatSouth(), bounds.getLonWest()));
                coordinates.add(new Coordinate(bounds.getLatNorth(), bounds.getLonWest()));

                AirMapPolygon polygon = new AirMapPolygon(coordinates);

                return getAdvisories(selectedRulesets, polygon)
                        .onErrorResumeNext(new Func1<Throwable, Observable<? extends AirMapAirspaceStatus>>() {
                            @Override
                            public Observable<? extends AirMapAirspaceStatus> call(Throwable throwable) {
                                return Observable.just(null);
                            }
                        });
            }
        };
    }

    private Observable<AirMapAirspaceStatus> getAdvisories(final List<AirMapRuleset> rulesets, final AirMapPolygon polygon) {
        return Observable.create(new Observable.OnSubscribe<AirMapAirspaceStatus>() {
            @Override
            public void call(final Subscriber<? super AirMapAirspaceStatus> subscriber) {
                Date start = new Date();
                Date end = new Date(start.getTime() + (4 * 60 * 60 * 1000));

                List<String> rulesetIds = new ArrayList<>();
                for (AirMapRuleset ruleset : rulesets) {
                    rulesetIds.add(ruleset.getId());
                }

                final Call statusCall = AirMap.getAirspaceStatus(polygon, rulesetIds, start, end, new AirMapCallback<AirMapAirspaceStatus>() {
                    @Override
                    public void onSuccess(final AirMapAirspaceStatus response) {
                        subscriber.onNext(response);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(AirMapException e) {
                        if (rulesets == null || rulesets.isEmpty()) {
                            subscriber.onNext(null);
                            subscriber.onCompleted();
                        } else {
                            subscriber.onError(e);
                        }
                    }
                });

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        statusCall.cancel();
                    }
                }));
            }
        });
    }

    public List<AirMapAdvisory> getCurrentAdvisories() {
        return airspaceStatus.getAdvisories();
    }

    public AirMapAirspaceStatus getAirspaceStatus() {
        return airspaceStatus;
    }

    public List<AirMapRuleset> getAvailableRulesets() {
        return availableRulesets;
    }

    public List<AirMapRuleset> getSelectedRulesets() {
        return selectedRulesets;
    }

    public void onMapReset() {
        availableRulesets = new ArrayList<>();
        selectedRulesets = new ArrayList<>();
    }

    public void onMapLoaded(LatLng latLng) {
        jurisdictionsPublishSubject.onNext(new Coordinate(latLng));
    }

    public void onMapMoved(LatLng latLng) {
        jurisdictionsPublishSubject.onNextThrottled(new Coordinate(latLng));
    }

    public void onDestroy() {
        rulesetsSubscription.unsubscribe();
    }

    public interface Callback {
        void onRulesetsUpdated(List<AirMapRuleset> availableRulesets, List<AirMapRuleset> selectedRulesets, List<AirMapRuleset> previouslySelectedRulesets);
        void onAdvisoryStatusUpdated(AirMapAirspaceStatus advisoryStatus);
        void onAdvisoryStatusLoading();
    }
}
