# AirMap Android SDK
[![Bintray](https://img.shields.io/bintray/v/airmapio/maven/com.airmap.airmapsdk.svg)](http://jcenter.bintray.com/com/airmap/airmapsdk/airmapsdk/)
[![license](https://img.shields.io/github/license/airmap/AirMapSDK-Android.svg)](https://github.com/airmap/AirMapSDK-Android/blob/master/LICENSE)

Create Flights, Send Telemetry Data, Get Realtime Traffic Alerts.

##Requirements
* Minimum Andorid SDK Level 15 or higher

### Sign up for an [AirMap Developer Account.](https://dashboard.airmap.io/developer/)

 [https://dashboard.airmap.io/developer](https://dashboard.airmap.io/developer)
 
 
### Read Getting Started Guide
[https://developers.airmap.com/docs/android-getting-started](https://developers.airmap.com/docs/android-getting-started)

## Setup

Start by adding the Android SDK to your project:

* Add `compile('com.airmap.sdk:airmapsdk:1.0.0'){ exclude module: 'support-v4' }`
 to your module level `build.gradle` file
* Add `maven { url "https://repo.eclipse.org/content/repositories/paho-releases/" }` to your application-level `build.gradle` file in the 

Now, initialize the SDK in your Activity or Application

### Initalizing The SDK

You will need an API Key & Login Token (See Docs)

```java
AirMap.init(MainActivity.this);
```


#License
See [LICENSE](https://raw.githubusercontent.com/airmap/AirMapSDK-Android/master/LICENSE) for more details.