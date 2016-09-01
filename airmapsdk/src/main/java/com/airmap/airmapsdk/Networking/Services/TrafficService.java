package com.airmap.airmapsdk.Networking.Services;

import android.content.Context;
import android.util.Log;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.Models.Coordinate;
import com.airmap.airmapsdk.Models.Flight.AirMapFlight;
import com.airmap.airmapsdk.Models.Traffic.AirMapTraffic;
import com.airmap.airmapsdk.Networking.Callbacks.AirMapCallback;
import com.airmap.airmapsdk.Networking.Callbacks.AirMapTrafficListener;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Vansh Gandhi on 6/27/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class TrafficService extends BaseService {
    //The current state of the connection
    private enum ConnectionState {
        Connecting, Connected, Disconnected
    }

    private MqttAndroidClient client;
    private MqttConnectOptions options;
    private List<AirMapTrafficListener> listeners;
    private List<AirMapTraffic> allTraffic;
    private List<String> currentChannels; //Current subscribed channels
    private ConnectionState connectionState;
    private CurrentFlightAirMapCallback currentFlightCallback;
    private IMqttActionListener actionListener;
    private String flightId;
    private boolean timerPaused;
    private boolean checkForUpdatedFlight;

    /**
     * Initialize an TrafficService to receive traffic alerts and situational awareness
     *
     * @param context An Android Context
     */
    public TrafficService(Context context) {
        String clientId = UUID.randomUUID().toString();
        client = new MqttAndroidClient(context, mqttBaseUrl, clientId);
        client.setCallback(new MqttEventCallback());
        options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setKeepAliveInterval(15);
        options.setPassword(AirMap.getInstance().getAuthToken().toCharArray());
        connectionState = ConnectionState.Disconnected;
        currentChannels = new ArrayList<>();
        allTraffic = new CopyOnWriteArrayList<>(); //Thread safe list
        listeners = new ArrayList<>();
        timerPaused = true;
        checkForUpdatedFlight = false;
        currentFlightCallback = new CurrentFlightAirMapCallback();
        actionListener = new MqttActionCallback();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!timerPaused) { //Don't clear if the timer is "paused"
                    clearOldTraffic();
                    updateTrafficProjections();
                }
            }
        }, 0, 1000); //Clear old traffic every second

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (checkForUpdatedFlight) {
                    AirMap.getCurrentFlight(new AirMapCallback<AirMapFlight>() {
                        @Override
                        public void onSuccess(AirMapFlight response) {
                            if (response != null && !response.getFlightId().equals(flightId)) {
                                connect();
                            }
                        }

                        @Override
                        public void onError(AirMapException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }, 0, 1000 * 60); //Update current flight every minute
    }

    /**
     * Connect to the server to receive updates
     */
    public void connect() {
        if (listeners.isEmpty()) {
            AirMapLog.i("TrafficService", "No listeners, not connecting");
            return;
        }
        AirMapLog.i("TrafficService", "Connecting to Traffic Service");
        if (connectionState == ConnectionState.Connecting) { //Don't connect if already connecting
            return;
        }
        connectionState = ConnectionState.Connecting;
        allTraffic.clear();
        unsubscribeFromAllChannels();
        AirMap.getCurrentFlight(currentFlightCallback);
    }

    /**
     * Disconnect from the server and stop receiving updates
     */
    public void disconnect() {
        if (connectionState == ConnectionState.Disconnected || connectionState == ConnectionState.Connecting || !client.isConnected()) {
            return;
        }
        AirMapLog.i("TrafficService", "Disconnecting from alerts");
        removeAllTraffic();
        try {
            client.disconnect(connectionState, actionListener);
            checkForUpdatedFlight = false;
            timerPaused = true;
        } catch (MqttException e) {
            AirMapLog.e("TrafficService", "Error disconnecting");
        } finally {
            onDisconnect(false);
        }
    }

    /**
     * Add a listener to be notified of traffic events
     *
     * @param listener A AirMapTrafficListener which will be notified when there is a traffic alert
     */
    public void addListener(AirMapTrafficListener listener) {
        if (listeners.isEmpty()) {
            listeners.add(listener);
            connect();
        } else {
            listeners.add(listener);
        }
    }

    /**
     * Clear all the listeners
     */
    public void removeAllListeners() {
        listeners.clear();
    }

    /**
     * Determine whether the client is connected
     *
     * @return whether the client is connected
     */
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    /**
     * When connected, subscribe to the necessary channels to get properly notified
     */
    private void onConnect() {
        connectionState = ConnectionState.Connected;
        checkForUpdatedFlight = true;
        startTimer();
        subscribe(String.format(trafficAlertChannel, flightId));
        subscribe(String.format(situationalAwarenessChannel, flightId));
    }

    /**
     * This will take care of all work that needs to be done when disconnected
     */
    private void onDisconnect() {
        onDisconnect(true);
    }

    private void onDisconnect(boolean retry) {
        connectionState = ConnectionState.Disconnected;
        checkForUpdatedFlight = false;
        pauseTimer();
        if (retry) {
            connect(); //Reconnect
        }
    }

    /**
     * Update all the traffic projections based on their heading and ground speed
     */
    private void updateTrafficProjections() {
        List<AirMapTraffic> updated = new ArrayList<>();
        for (AirMapTraffic traffic : allTraffic) {
            if (traffic.getGroundSpeedKt() > -1 && traffic.getTrueHeading() > -1) {
                allTraffic.remove(traffic);
                Coordinate projected = projectedCoordinate(traffic);
                traffic.setCoordinate(projected);
                traffic.setShowAlert(false);
                allTraffic.add(traffic);
                updated.add(traffic);
            }
        }
        notifyUpdated(updated);
    }

    /**
     * Get a projected coordinate from an AirMapTraffic's bearing and ground speed
     *
     * @param traffic The traffic whose coordinate to update
     * @return the projected location of the traffic
     */
    private Coordinate projectedCoordinate(AirMapTraffic traffic) {
        long elapsedTime = (new Date().getTime() - traffic.getRecordedTime().getTime()) / 1000; //elapsed time between now and traffic's time
        double metersPerSecond = traffic.getGroundSpeedKt() * 0.514444;
        double distanceTraveled = metersPerSecond * elapsedTime;
        //Use initial coordinate for calculation to avoid improper calculation
        return getCoordinateFromBearingAndDistance(traffic.getInitialCoordinate(), traffic.getTrueHeading(), distanceTraveled);
    }

    /**
     * Get a projected coordinate from a starting Coordinate, a bearing, and a distance traveled
     *
     * @param c        The starting coordinate
     * @param bearing  The bearing of the traffic
     * @param distance The distance the traffic has traveled
     * @return the projected location of the traffic
     */
    private Coordinate getCoordinateFromBearingAndDistance(Coordinate c, double bearing, double distance) {
        final int earthRadius = 6371000;
        double angularDistance = distance / earthRadius;
        double brng = Math.toRadians(bearing);
        double lat1 = Math.toRadians(c.getLatitude());
        double lng1 = Math.toRadians(c.getLongitude());
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(angularDistance) + Math.cos(lat1) * Math.sin(angularDistance) * Math.cos(brng));

        double lng2 = lng1 + Math.atan2(Math.sin(brng) * Math.sin(angularDistance) * Math.cos(lat1), Math.cos(angularDistance) - Math.sin(lat1) * Math.sin(lat2));

        lng2 = (lng2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;
        return new Coordinate(Math.toDegrees(lat2), Math.toDegrees(lng2));
    }

    /**
     * Called when a traffic alert is received
     *
     * @param json        The JSON representation of an array of AirMapTraffic
     * @param trafficType The type of traffic (Alert or Situational Awareness)
     */
    private void receivedTraffic(String json, AirMapTraffic.TrafficType trafficType) {
        List<AirMapTraffic> updated = new ArrayList<>();
        List<AirMapTraffic> added = new ArrayList<>();
        JSONArray trafficJsonArray;
        try {
            trafficJsonArray = new JSONObject(json).getJSONArray("traffic");
        } catch (JSONException e) {
            e.printStackTrace();
            AirMapLog.e("TrafficService", e.getMessage());
            return;
        }
        for (int i = 0; i < trafficJsonArray.length(); i++) {
            AirMapTraffic temp = new AirMapTraffic(trafficJsonArray.optJSONObject(i));
            temp.setTrafficType(trafficType);
            Coordinate projected = projectedCoordinate(temp);
            temp.setCoordinate(projected);
            int index = allTraffic.indexOf(temp);
            if (index != -1) {
                allTraffic.set(index, temp);
                updated.add(temp);
            } else {
                allTraffic.add(temp);
                added.add(temp);
            }
        }
        notifyUpdated(updated);
        notifyAdded(added);
    }

    /**
     * Get rid of traffic that is no longer valid (the traffic is expired)
     */
    private void clearOldTraffic() {
        List<AirMapTraffic> oldAllTraffic = new ArrayList<>();
        for (AirMapTraffic traffic : allTraffic) {
            if (trafficExpired(traffic)) {
                oldAllTraffic.add(traffic);
                allTraffic.remove(traffic);
            }
        }
        notifyRemoved(oldAllTraffic);
        if (allTraffic.isEmpty() && connectionState == ConnectionState.Disconnected) {
            pauseTimer();
        }
    }

    /**
     * Subscribe to the specified channel
     *
     * @param channel The channel to subscribe to
     */
    private void subscribe(final String channel) {
        try {
            client.subscribe(channel, 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.v("TrafficService", "Success subscribing");
                    Log.v("TrafficService", channel);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("TrafficService", exception.getMessage());
                    exception.printStackTrace();
                }
            });
            currentChannels.add(channel);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unsubscribe from all channels currently subscribed to
     */
    private void unsubscribeFromAllChannels() {
        try {
            for (String channel : currentChannels) {
                if (client != null && channel != null)
                    client.unsubscribe(channel);
            }
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e("TrafficService", "Error unsubscribing: " + e.getMessage());
        } finally {
            currentChannels.clear();
        }
    }

    /**
     * Checks if the traffic is older than the validity interval
     *
     * @param traffic the traffic to check the validity of
     * @return whether the traffic is expired (no longer valid)
     */
    private boolean trafficExpired(AirMapTraffic traffic) {
        int timeInterval = 30;
        return new Date(traffic.getIncomingTime().getTime() + timeInterval * 1000).before(new Date());
    }

    /**
     * Removes all traffic from the list and notifies the listener
     */
    private void removeAllTraffic() {
        List<AirMapTraffic> removed = new ArrayList<>(allTraffic);
        allTraffic.clear();
        notifyRemoved(removed);
    }

    /**
     * Notify the listeners that traffic has been removed
     *
     * @param removed a list of all traffic that was removed
     */
    private void notifyRemoved(List<AirMapTraffic> removed) {
        if (removed == null || removed.isEmpty()) {
            return;
        }

        for (AirMapTrafficListener listener : listeners) {
            listener.onRemoveTraffic(removed);
        }
    }

    /**
     * Notify the listeners that traffic has been added
     *
     * @param added a list of all traffic that was added
     */
    private void notifyAdded(List<AirMapTraffic> added) {
        if (added == null || added.isEmpty()) {
            return;
        }

        for (AirMapTrafficListener listener : listeners) {
            listener.onAddTraffic(added);
        }
    }

    /**
     * Notify the listeners that traffic has been updated
     *
     * @param updated a list of all traffic that was updated
     */
    private void notifyUpdated(List<AirMapTraffic> updated) {
        if (updated == null || updated.isEmpty()) {
            return;
        }
        for (AirMapTrafficListener listener : listeners) {
            listener.onUpdateTraffic(updated);
        }
    }

    /**
     * Pauses the timer
     */
    private void pauseTimer() {
        timerPaused = true;
    }

    /**
     * Starts the timer
     */
    private void startTimer() {
        timerPaused = false;
    }


    private class CurrentFlightAirMapCallback implements AirMapCallback<AirMapFlight> {

        /**
         * Called when the current flight was successfully received
         *
         * @param response The object response
         */
        @Override
        public void onSuccess(AirMapFlight response) {
            if (response != null) {
                flightId = response.getFlightId();
                options.setUserName(flightId);
                AirMapLog.v("TrafficService", options.toString());
                AirMapLog.i("TrafficService", "Got flight with id: " + flightId);
                AirMapLog.i("TrafficService", "Connecting to MQTT server");
                try {
                    client.connect(options, ConnectionState.Connecting, actionListener);
                } catch (MqttException e) {
                    onDisconnect(false);
                }
            }
        }

        /**
         * Called when there was an error retreiving the current flight
         *
         * @param e Specifics of the error
         */
        @Override
        public void onError(AirMapException e) {
            onDisconnect(false);
        }
    }

    private class MqttActionCallback implements IMqttActionListener {

        /**
         * Called when there was a successful MQTT connection
         */
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            ConnectionState state = (ConnectionState) asyncActionToken.getUserContext();
            if (state == ConnectionState.Connecting) {
                AirMapLog.i("TrafficService", "Successfully connected");
                onConnect();
            }
        }

        /**
         * Called when there was an error connecting
         */
        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            exception.printStackTrace();
            Log.e("TrafficService", "Error connecting: " + exception.getMessage());
            onDisconnect(false);
        }
    }

    private class MqttEventCallback implements MqttCallback {

        /**
         * Called when a message is received from the server
         *
         * @param topic   The topic the message was sent to
         * @param message The message itself
         */
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            String messageString = message.toString();
            System.out.println("TrafficService: " + messageString);
            if (topic.contains("/alert/")) {
                receivedTraffic(messageString, AirMapTraffic.TrafficType.Alert);
            } else if (topic.contains("/sa/")) {
                receivedTraffic(messageString, AirMapTraffic.TrafficType.SituationalAwareness);
            }
        }

        /**
         * Called when the connection to the server was lost
         */
        @Override
        public void connectionLost(Throwable cause) {
            onDisconnect(false);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            //We're not publishing anything as of right now
        }
    }

    protected void setAuthToken(String auth) {
        options.setPassword(auth != null ? auth.toCharArray() : new char[]{}); //Auth might be null
    }
}