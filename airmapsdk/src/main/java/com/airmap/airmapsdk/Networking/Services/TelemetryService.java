package com.airmap.airmapsdk.Networking.Services;

import com.google.protobuf.ByteString;

import android.support.annotation.Nullable;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.Models.Comm.AirMapComm;
import com.airmap.airmapsdk.Models.Coordinate;
import com.airmap.airmapsdk.Models.Flight.AirMapFlight;
import com.airmap.airmapsdk.Models.Telemetry;
import com.airmap.airmapsdk.Networking.Callbacks.AirMapCallback;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Vansh Gandhi on 7/1/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class TelemetryService extends BaseService {

    AirMapComm comm;
    DatagramSocket socket;
    AirMapFlight flight;

    /**
     * Initializes service to send Telemetry data
     *
     * @param flight The flight to send telemetry for
     */
    public TelemetryService(AirMapFlight flight) {
        this.flight = flight;
        connect(); //Initializes socket
    }

    /**
     * Sends a telemetry update to AirMap
     *
     * @param coordinate  The location of the aircraft
     * @param altitude    The altitude of the aircraft AGL in meters
     * @param groundSpeed The speed of the aircraft in m/s
     * @param trueHeading The heading of the aircraft, measured in degrees relative to true north
     * @param pressure    The barometric pressure around the aircraft, measured in hectoPascals
     */
    public void sendMessage(final Coordinate coordinate, final int altitude, @Nullable final Integer groundSpeed, @Nullable final Integer trueHeading, @Nullable final Float pressure) {
        if (!socket.isConnected()) {
            connect();
        }

        if (comm == null || comm.isExpired()) {
            FlightService.getCommKey(flight, new AirMapCallback<AirMapComm>() {
                @Override
                public void onSuccess(AirMapComm response) {
                    comm = response;
                    doSendMessage(coordinate, altitude, groundSpeed, trueHeading, pressure);
                }

                @Override
                public void onError(AirMapException e) {
                    onException(e);
                }
            });
        } else {
            doSendMessage(coordinate, altitude, groundSpeed, trueHeading, pressure);
        }
    }

    /**
     * Gets the flight associated with this telemetry service
     *
     * @return the flight associated with this telemetry service
     */
    public AirMapFlight getFlight() {
        return flight;
    }

    //Connects to the UDP server
    private void connect() {
        AirMapLog.v("TelemetryService", "Connecting to Telemetry");
        try {
            InetAddress address = InetAddress.getByName(telemetryBaseUrl);
            socket = new DatagramSocket();
            socket.connect(address, telemetryPort);
        } catch (Exception e) {
            onException(e);
        }
    }

    /**
     * Disconnects from the telemetry service
     */
    public void disconnect() {
        socket.disconnect();
    }

    //Sends the encrypted, encoded message
    private void doSendMessage(Coordinate coordinate, int altitude, Integer groundSpeed, Integer trueHeading, Float baro) {
        byte[] message = encodeData(comm.getKey(), flight, coordinate, altitude, groundSpeed, trueHeading, baro);
        if (message != null) {
            DatagramPacket packet = new DatagramPacket(message, message.length);
            try {
                socket.send(packet);
            } catch (Exception e) {
                onException(e);
            }
        }
    }

    //Gets the OpenMessage ready to send
    private byte[] encodeData(int[] key, AirMapFlight flight, Coordinate coord, int altitude, Integer groundSpeed, Integer trueHeading, Float baro) {
        IvParameterSpec iv = generateIv();
        byte[] encryptedMessage = encrypt(key, iv, buildSecretMessage(coord, altitude, groundSpeed, trueHeading, baro));
        if (encryptedMessage != null) {
            Telemetry.OpenMessage openMessage = Telemetry.OpenMessage.newBuilder()
                    .setFlightId(flight.getFlightId())
                    .setIv(ByteString.copyFrom(iv.getIV()))
                    .setPayload(ByteString.copyFrom(encryptedMessage))
                    .build();
            return openMessage.toByteArray();
        }
        return null;
    }

    //Encrypts a SecretMessage with the given key and iv
    private byte[] encrypt(int[] key, IvParameterSpec iv, Telemetry.SecretMessage secretMessage) {
        try {
            SecretKey secretKey = new SecretKeySpec(integersToBytes(key), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            return cipher.doFinal(secretMessage.toByteArray());
        } catch (Exception e) {
            onException(e);
        }
        return null;
    }

    //Builds the protobuf secret message
    private Telemetry.SecretMessage buildSecretMessage(Coordinate coordinate, int altitude, Integer groundSpeed, Integer trueHeading, Float pressure) {
        Telemetry.SecretMessage.Builder builder = Telemetry.SecretMessage.newBuilder();
        builder.setAltitude(altitude)
                .setTimestamp(new Date().getTime() / 1000) //timestamp should be sent as seconds
                .setLatitude((float) coordinate.getLatitude())
                .setLongitude((float) coordinate.getLongitude());
        //Optional parameter
        if (pressure != null) {
            builder.setBaro(pressure);
        }
        //Optional parameter
        if (groundSpeed != null) {
            builder.setGroundSpeedMs(groundSpeed);
        }
        //Optional parameter
        if (trueHeading != null) {
            builder.setTrueHeading(trueHeading);
        }
        return builder.build();
    }

    //Generates a random IV to be used for the AES encryption
    private IvParameterSpec generateIv() {
        int blockSize = 16;
        byte[] iv = new byte[blockSize];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    //Converts an array of 8 bit integers to a byte array
    private byte[] integersToBytes(int[] values) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for (int value : values) {
            dos.writeByte(value); //Only writes the least significant byte of the int
        }
        return baos.toByteArray();
    }

    private void onException(Exception e) {
        AirMapLog.e("TelemetryService", e.getMessage());
        e.printStackTrace();
    }
}