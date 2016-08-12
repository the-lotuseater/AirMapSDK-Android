# AirMap Android SDK

Create Flights, Send Telemetry Data, Get Realtime Traffic Alerts.

[https://developers.airmap.com/docs/android-getting-started](https://developers.airmap.com/docs/android-getting-started)

#Overview
The AirMap Android SDK can be used to easily add AirMap integration to an Android app. Currently the AirMap Android SDK supports the following functions:

* Creating/managing flights
* Sending live aircraft telemetry
* Receiving traffic alerts
* Viewing/updating pilot profile
* Applying for permits
* Checking status for a point, path, or polygon

##Requirements
* Minimum Andorid SDK Level 15 or higher

## Setup

Start by adding the Android SDK to your project:

* Add 

`compile('com.airmap.sdk:airmapsdk:1.0.0') { exclude module: 'support-v4' }`
 to your `build.gradle` file

Now, initialize the SDK in your Activity or Application

### Initalizing The SDK

You will need an API Key & Login Token (See Docs)

```java
String auth = ...;
String apikey = ...;

AirMap.init(MainActivity.this, auth, apikey);
```

##User Verification

Before a user can use the DNAS system to send airport notifications and receive SMS reponses, the user must verify their phone number. The Android SDK provides the ability to verify a user's phone once the user is logged into an AirMap account.

By updating the phone number, AirMap will know to send a verification text to that number

```java
String phoneNumber = ...;
AirMap.updatePhoneNumber(phoneNumber, new AirMapCallback<AirMapPilot>() {
    @Override
    public void onSuccess(AirMapPilot response) {
        //At this point, the user will get a text sent to them with a verification token. This token will need to be verified
    }

    @Override
    public void onError(AirMapException e) {

    }
});
```
After updating the phone number, the number needs to be verified

```java
String token = ...;
AirMap.verifyPhoneToken(token, new AirMapCallback<Void>() {
    @Override
    public void onSuccess(Void response) {
        //Successfully verified
    }

    @Override
    public void onError(AirMapException e) {

    }
});
```

## Check Flight Status

Before you create a flight and takeoff, you will want to check the flight conditions. Flight conditions include everything an operator would want to know to determine whether its safe to fly in a given area. This includes:

* Traffic light (Red, Yellow, Green) on overall safety of flight in area
* Nearest flight advisories
* Current weather conditions

Here is an example of checking the flight conditions by providing a point and radius that bounds the flight. Note that by passing a `null` for both `types` and `ignoredTypes`, the function will return conditions for all types.

```java
Coordinate locationToCheck = new Coordinate(34.016779, -118.494698);
boolean showWeather = true;
Date date = new Date();
double radius = 100;

AirMap.checkCoordinate(locationToCheck, radius, null, null, showWeather, date, new AirMapCallback<AirMapStatus>() {
    @Override
  	public void onSuccess(AirMapStatus response) {
      
    }
    
    @Override
  	public void onError(AirMapException e) {
      
    }
});
```

You can also check status by providing a more detailed plan through a polygon area or a path and width. Check out the reference docs for more info.



## Flights

#### Create a Flight

Before you can use any other function, you must create a flight with a starting location.

There are 2 ways to create a flight. 

The first way is to start the built-in `Activity` that will handle the entire process of creating the flight, submitting digital notice, and obtaining the appropriate permits.

###### MapBox Api Key

The Create Flight UI uses the MapBox GL Native SDK.  Please request a MapBox Access Token: [https://www.mapbox.com/android-sdk/#access_tokens](https://www.mapbox.com/android-sdk/#access_tokens) 

```java
int requestCode = ...;
Coordinate flightLocation = ...;
String mapboxApiKey = ...;
AirMap.createFlight(MainActivity.this, requestCode, flightLocation, mapboxApiKey, null);
```
Once the user finishes creating the flight, you can obtain the flight they just created by overriding `onActivityResult` in your `Activity` or `Fragment`

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (requestCode == /*The request code you originally passed in*/) {
  	  if (resultCode == Activity.RESULT_OK) {
         	AirMapFlight flight = (AirMapFlight) data.getSerializableExtra(CreateFlightActivity.FLIGHT);
    }
  }
}
```
The second way is to simply pass in the flight you wish to create. Be wary of permits you may need to apply for and attach to the flight

```java
AirMapFlight flight = ...; //Create the AirMapFlight object
AirMap.createFlight(flight, new AirMapCallback<AirMapFlight>() {
    @Override
    public void onSuccess(AirMapFlight response) {
      //Flight was successfully created
    }
  
    @Override
    public void onError(AirMapException e) {
    	//Called when there was some type of error      
    }
});
```


## Sending Live Telemetry
Now that our flight has started, we should start sending telemetry. The recommended update frequency is 5 times per second.

#### Send Telemetry Data

```java
AirMapFlight flight = ...; //Obtain the flight you wish to send telemetry for
TelemetryService telemetryService = new TelemetryService(flight);
Coordinate updatedCoordinate = ...;
int updatedAltitude = ...;
int updatedGroundspeed = ...;
int updatedTrueHeading = ...;
int updatedPressure = Utils.hgToHpa(...); //This needs to be in hectoPascals, so there is a convenience method to convert from mm Hg to Hpa
telemetryService.sendMessage(updatedCoordinate, updatedAltitude, updatedGroundspeed, updatedTrueHeading, updatedPressure);
```


## Receiving Traffic Alerts

Now that we are sending live location data, we are ready to receive traffic alerts.

```java
AirMap.enableTrafficAlerts(new AirMapTrafficListener() {
  @Override
  public void onAddTraffic(List<AirMapTraffic> added) {

  }

  @Override
  public void onUpdateTraffic(List<AirMapTraffic> updated) {

  }

  @Override
  public void onRemoveTraffic(List<AirMapTraffic> removed) {

  }
});
```

There are three methods in this listener - all of which have a list of `AirMapTraffic` objects as a parameter. The first one sends new traffic that has entered the area. The next function updates traffic that has already been added with the latest position data. Finally, the last function removes traffic that has left the area and no longer needs tracked. 

The `AirMapTraffic` object gives properties about a certain flight. Most of these properties are straightforward, but the `TrafficType` requires an explanation. There are two types of traffic alerts. `SituationalAwareness` traffic is for other aircraft that are within 10 miles of our aircraft's position, while `Alert` traffic is other aircraft that are headed towards our aircraft and will be within 1000 meters of our aircraft in the next 30 seconds. Obviously the `Alert` traffic is especially important and should be prioritized.

##End a flight
Once a user has landed the aircraft, you should stop sending telemetry and end the flight:

```java
AirMapFlight flight = ...; 

telemetryService.disconnect();
AirMap.disableTrafficAlerts();
AirMap.endFlight(flight, new AirMapCallback<AirMapFlight>() {
  @Override
  public void onSuccess(AirMapFlight response) {
    
  }
  
  @Override
  public void onError(AirMapException e) {
    
  }
});
```
##Manage flights
There are some other useful functions for managing flights. We can get a list of the user's flights by calling `getFlights`. To permanently remove a saved flight, we can call `deleteFlight`
Warning: You cannot delete a flight that is in progress. Make sure to end the flight before deleting it!

```java
//Get all the user's flights
AirMap.getFlights(new AirMapCallback<List<AirMapFlight>>() {
  @Override
  public void onSuccess(List<AirMapFlight> response) {

  }

  @Override
  public void onError(final AirMapException e) {

  }
});

AirMapFlight flight = ...;
AirMap.deleteFlight(flight, new AirMapCallback<Void>() {
  @Override
  public void onSuccess(Void response) {
    
  }

  @Override
  public void onError(AirMapException e) {
    
  }
});
```

#License
See [LICENSE](https://raw.githubusercontent.com/airmap/airmap-android-sdk/master/LICENSE) for more details.