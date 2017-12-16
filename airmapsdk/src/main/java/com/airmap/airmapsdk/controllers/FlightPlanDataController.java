package com.airmap.airmapsdk.controllers;

import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan;
import com.airmap.airmapsdk.models.rules.AirMapJurisdiction;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.models.shapes.AirMapGeometry;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.airmapsdk.models.status.AirMapAirspaceStatus;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.ui.views.AirMapMapView;
import com.airmap.airmapsdk.util.ThrottleablePublishSubject;

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
 * Created by collin@airmap.com on 12/14/17.
 */

public class FlightPlanDataController extends MapDataController {

    private static final String TAG = "FlightPlanDataController";

    private AirMapPolygon polygon;
    private Coordinate takeoffCoordinate;
    private float buffer;

    public FlightPlanDataController(AirMapMapView map, AirMapMapView.Configuration configuration) {
        super(map, configuration);
    }

    @Override
    protected Func1<AirMapPolygon, Observable<List<AirMapJurisdiction>>> getJurisdictions() {
        return new Func1<AirMapPolygon, Observable<List<AirMapJurisdiction>>>() {
            @Override
            public Observable<List<AirMapJurisdiction>> call(final AirMapPolygon polygon) {
                return Observable.create(new Observable.OnSubscribe<List<AirMapJurisdiction>>() {
                    @Override
                    public void call(final Subscriber<? super List<AirMapJurisdiction>> subscriber) {
                        final Call statusCall = AirMap.getJurisdictions(polygon, new AirMapCallback<List<AirMapJurisdiction>>() {
                            @Override
                            public void onSuccess(final List<AirMapJurisdiction> response) {
                                subscriber.onNext(response);
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onError(AirMapException e) {
                                if (polygon == null) {
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
                })
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends List<AirMapJurisdiction>>>() {
                    @Override
                    public Observable<? extends List<AirMapJurisdiction>> call(Throwable throwable) {
                        return Observable.just(null);
                    }
                });
            }
        };
    }

    @Override
    public void onMapRegionChanged() {
        // map region does not affect jurisdictions, rulesets or advisories in this mode
    }

    public void onPolygonChanged(AirMapPolygon polygon, Coordinate takeoffCoordinate, float buffer) {
        this.polygon = polygon;
        this.takeoffCoordinate = takeoffCoordinate;
        this.buffer = buffer;

        jurisdictionsPublishSubject.onNextThrottled(polygon);
    }

    public AirMapPolygon getPolygon() {
        return polygon;
    }

    public Coordinate getTakeoffCoordinate() {
        return takeoffCoordinate;
    }

    public float getBuffer() {
        return buffer;
    }
}
