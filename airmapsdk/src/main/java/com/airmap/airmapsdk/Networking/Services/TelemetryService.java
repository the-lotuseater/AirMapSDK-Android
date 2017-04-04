package com.airmap.airmapsdk.networking.services;

import com.google.protobuf.Message;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.models.Telemetry;
import com.airmap.airmapsdk.models.comm.AirMapComm;
import com.airmap.airmapsdk.models.flight.AirMapFlight;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Vansh Gandhi on 7/1/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class TelemetryService extends BaseService {

    private static final String TAG = "Telemetry Service";

    // frequencies in milliseconds
    private static final long POSITION_FREQUENCY = 200;
    private static final long ATTITUDE_FREQUENCY = 200;
    private static final long SPEED_FREQUENCY = 200;
    private static final long BAROMETER_FREQUENCY = 5000;

    private PublishSubject<Pair<AirMapFlight, Message>> telemetry;

    public TelemetryService() {
        telemetry = PublishSubject.create();
        setupBindings();
    }

    public void sendPositionMessage(AirMapFlight flight, double latitude, double longitude, @Nullable float altitudeAGL, @Nullable float altitudeMSL, @Nullable float horizontalAccuracy) {
        long timestamp = System.currentTimeMillis();

        Telemetry.Position positionMessage = Telemetry.Position.newBuilder()
                .setTimestamp(timestamp)
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setAltitudeAgl(altitudeAGL)
                .setAltitudeMsl(altitudeMSL)
                .setHorizontalAccuracy(horizontalAccuracy)
                .build();

        sendTelemetry(flight, positionMessage);
    }

    public void sendAttitudeMessage(AirMapFlight flight, float yaw, float pitch, float roll) {
        long timestamp = System.currentTimeMillis();

        Telemetry.Attitude attitudeMessage = Telemetry.Attitude.newBuilder()
                .setTimestamp(timestamp)
                .setYaw(yaw)
                .setPitch(pitch)
                .setRoll(roll)
                .build();

        sendTelemetry(flight, attitudeMessage);
    }

    public void sendSpeedMessage(AirMapFlight flight, float velocityX, float velocityY, float velocityZ) {
        long timestamp = System.currentTimeMillis();

        Telemetry.Speed speedMessage = Telemetry.Speed.newBuilder()
                .setTimestamp(timestamp)
                .setVelocityX(velocityX)
                .setVelocityY(velocityY)
                .setVelocityZ(velocityZ)
                .build();

        sendTelemetry(flight, speedMessage);
    }

    public void setBarometerMessage(AirMapFlight flight, float pressure) {
        long timestamp = System.currentTimeMillis();

        Telemetry.Barometer barometerMessage = Telemetry.Barometer.newBuilder()
                .setTimestamp(timestamp)
                .setPressure(pressure)
                .build();

        sendTelemetry(flight, barometerMessage);
    }

    private void sendTelemetry(AirMapFlight flight, Message message) {
        telemetry.onNext(new Pair<>(flight, message));
    }

    private void setupBindings() {
        Observable<Session> session = telemetry
                .map(new Func1<Pair<AirMapFlight, Message>, AirMapFlight>() {
                    @Override
                    public AirMapFlight call(Pair<AirMapFlight, Message> pair) {
                        return pair.first;
                    }
                })
                .distinctUntilChanged()
                .flatMap(new Func1<AirMapFlight, Observable<Session>>() {
                    @Override
                    public Observable<Session> call(final AirMapFlight flight) {
                        return FlightService.getCommKey(flight)
                                .doOnError(new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        Log.e(TAG, "getCommKey failed", throwable);
                                    }
                                })
                                .onErrorResumeNext(Observable.<AirMapComm>empty())
                                .map(new Func1<AirMapComm, Session>() {
                                    @Override
                                    public Session call(AirMapComm airMapComm) {
                                        return new Session(flight, airMapComm, null);
                                    }
                                })
                                .subscribeOn(Schedulers.io());
                    }
                });

        Observable<Pair<Session, Message>> flightMessages = Observable
                .combineLatest(session, telemetry, new Func2<Session, Pair<AirMapFlight, Message>, Pair<Session, Pair<AirMapFlight, Message>>>() {
                    @Override
                    public Pair<Session,Pair<AirMapFlight, Message>> call(Session s, Pair<AirMapFlight, Message> p) {
                        return new Pair<>(s, p);
                    }
                })
                .filter(new Func1<Pair<Session, Pair<AirMapFlight,Message>>, Boolean>() {
                    @Override
                    public Boolean call(Pair<Session, Pair<AirMapFlight, Message>> p) {
                        AirMapFlight sessionFlight = p.first.flight;
                        AirMapFlight telemetryFlight = p.second.first;
                        return sessionFlight.equals(telemetryFlight);
                    }
                })
                .map(new Func1<Pair<Session, Pair<AirMapFlight, Message>>, Pair<Session, Message>>() {
                    @Override
                    public Pair<Session,Message> call(Pair<Session, Pair<AirMapFlight, Message>> pair) {
                        return new Pair<>(pair.first, pair.second.second);
                    }
                });

        Observable<Pair<Session,Message>> position = flightMessages
                .filter(new Func1<Pair<Session,Message>,Boolean>() {
                    @Override
                    public Boolean call(Pair<Session, Message> p) {
                        return p.second instanceof Telemetry.Position;
                    }
                })
                .sample(Observable.interval(POSITION_FREQUENCY, TimeUnit.MILLISECONDS));

        Observable<Pair<Session,Message>> attitude = flightMessages
                .filter(new Func1<Pair<Session,Message>,Boolean>() {
                    @Override
                    public Boolean call(Pair<Session, Message> p) {
                        return p.second instanceof Telemetry.Attitude;
                    }
                })
                .sample(Observable.interval(ATTITUDE_FREQUENCY, TimeUnit.MILLISECONDS));

        Observable<Pair<Session,Message>> speed = flightMessages
                .filter(new Func1<Pair<Session,Message>,Boolean>() {
                    @Override
                    public Boolean call(Pair<Session, Message> p) {
                        return p.second instanceof Telemetry.Speed;
                    }
                })
                .sample(Observable.interval(SPEED_FREQUENCY, TimeUnit.MILLISECONDS));

        Observable<Pair<Session,Message>> barometer = flightMessages
                .filter(new Func1<Pair<Session,Message>,Boolean>() {
                    @Override
                    public Boolean call(Pair<Session, Message> p) {
                        return p.second instanceof Telemetry.Barometer;
                    }
                })
                .sample(Observable.interval(BAROMETER_FREQUENCY, TimeUnit.MILLISECONDS));

        Subscription latestMessages = Observable.merge(position, attitude, speed, barometer)
                .buffer(1, TimeUnit.SECONDS, 20)
                .doOnNext(new Action1<List<Pair<Session,Message>>>() {
                    @Override
                    public void call(List<Pair<Session,Message>> sessionMessages) {
                        sendMessages(sessionMessages);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Actions.empty(), new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "latestMessages Error", throwable);
                    }
                });

    }

    private void sendMessages(List<Pair<Session,Message>> sessionMessages) {
        if (sessionMessages == null || sessionMessages.isEmpty()) return;

        Pair<Session,Message> pair = sessionMessages.get(0);
        Session session = pair.first;

        List<Message> messages = new ArrayList<>();
        for (Pair<Session, Message> p : sessionMessages) {
            messages.add(p.second);
        }
        session.send(messages);
    }

    private void onException(Exception e) {
        AirMapLog.e("TelemetryService", e.getMessage());
        e.printStackTrace();
    }

    private enum Encryption {
        NONE,
        AES256CBC
    }

    private enum MessageType {
        POSITION(1),
        SPEED(2),
        ATTITUDE(3),
        BAROMETER(4);

        public final int value;

        MessageType(int value) {
            this.value = value;
        }
    }

    private class Session {
        private AirMapComm comm;
        private DatagramSocket socket;
        private AirMapFlight flight;

        private int packetNumber;

        Session(AirMapFlight flight, AirMapComm comm, DatagramSocket socket) {
            this.flight = flight;
            this.comm = comm;

            this.packetNumber = 1;

            this.socket = socket;
            if (this.socket == null || !this.socket.isConnected()) {
                try {
                    connect();
                } catch (UnknownHostException | SocketException e) {
                    Log.e(TAG, "Unable to connect to telemetry socket", e);
                }
            }
        }

        private void connect() throws UnknownHostException, SocketException {
            InetAddress address = InetAddress.getByName(telemetryBaseUrl);
            socket = new DatagramSocket();
            socket.connect(address, telemetryPort);
        }

        //Sends the encrypted, encoded message
        private void send(List<Message> messageList) {
            try {
                byte[] message = buildPacketData(comm.getKey(), flight, Encryption.AES256CBC, messageList);
                DatagramPacket packet = new DatagramPacket(message, message.length);
                socket.send(packet);
                packetNumber++;
            } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
                Log.e(TAG, "Unable to build or send packet", e);
            }
        }

        private byte[] buildPacketData(byte[] key, AirMapFlight flight, Encryption encryption, List<Message> messageList) throws IOException, NoSuchPaddingException,
                InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

            int serialNumber = packetNumber;

            String flightId = flight.getFlightId();

            byte flightIdLength = (byte) flightId.length();

            IvParameterSpec iv = generateIv();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);

            daos.writeInt(serialNumber);
            daos.writeByte(flightIdLength);
            daos.writeBytes(flightId);

            switch(encryption) {
                case AES256CBC:
                    daos.writeByte(1);
                    daos.write(iv.getIV());
                    break;
                case NONE:
                default:
                    daos.writeByte(0);
                    break;
            }

            ByteArrayOutputStream payloadBaos = new ByteArrayOutputStream();
            for (Message message : messageList) {
                MessageType messageType;
                if (message instanceof Telemetry.Position) {
                    messageType = MessageType.POSITION;
                } else if (message instanceof Telemetry.Speed) {
                    messageType = MessageType.SPEED;
                } else if (message instanceof Telemetry.Attitude) {
                    messageType = MessageType.ATTITUDE;
                } else if (message instanceof Telemetry.Barometer) {
                    messageType = MessageType.BAROMETER;
                } else {
                    messageType = null;
                }

                payloadBaos.write(buildMessage(messageType, message));
            }

            byte[] encryptedPayload = encryption == Encryption.AES256CBC ? encrypt(key, iv, payloadBaos.toByteArray()) : payloadBaos.toByteArray();
            payloadBaos.close();

            daos.write(encryptedPayload);
            daos.close();

            byte[] result = baos.toByteArray();
            baos.close();

            return result;
        }

        private byte[] buildMessage(MessageType messageType, Message message) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeShort(messageType.value);
            daos.writeShort(message.getSerializedSize());
            daos.write(message.toByteArray());

            daos.close();

            byte[] result = baos.toByteArray();
            baos.close();

            return result;
        }

        //Encrypts a SecretMessage with the given key and iv
        private byte[] encrypt(byte[] key, IvParameterSpec iv, byte[] payload) throws InvalidAlgorithmParameterException, InvalidKeyException,
                IOException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {

            SecretKey secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            return cipher.doFinal(payload);
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
    }
}