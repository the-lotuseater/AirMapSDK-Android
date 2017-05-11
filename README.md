![AirMap: The Airspace Platform for Developers](AirMap.png)
[![Bintray](https://img.shields.io/bintray/v/airmapio/maven/com.airmap.airmapsdk.svg)](http://jcenter.bintray.com/com/airmap/airmapsdk/airmapsdk/)
[![license](https://img.shields.io/github/license/airmap/AirMapSDK-Android.svg)](https://github.com/airmap/AirMapSDK-Android/blob/master/LICENSE)

Create Flights, Send Telemetry Data, Get Realtime Traffic Alerts.

## Requirements
* Minimum Andorid SDK Level 18 or higher

### Sign up for an [AirMap Developer Account.](https://dashboard.airmap.io/developer/)

 [https://dashboard.airmap.io/developer](https://dashboard.airmap.io/developer)
 
 
### Read Getting Started Guide
[https://developers.airmap.com/docs/android-getting-started](https://developers.airmap.com/docs/android-getting-started)

## Setup

Start by adding the Android SDK to your project:

* Add 
```groovy
compile('com.airmap.sdk:airmapsdk:1.0.8')
``` 

to your module level `build.gradle` file

* Add 
```groovy
maven { url "https://jitpack.io" }
``` 

to your application-level `build.gradle` file under the `allprojects.repositories` block


* If you were previously using version `1.0.2` or older, note that there have also been some package name changes, so some classes will need to be re-imported



### Initalizing The SDK

Simply add this line in your Application or Activity's `onCreate`

```java
AirMap.init(MainActivity.this);
```

### Documentation
Visit [https://developers.airmap.com/docs/android-getting-started](https://developers.airmap.com/docs/android-getting-started) for the full documentation

# License
See [LICENSE](https://raw.githubusercontent.com/airmap/AirMapSDK-Android/master/LICENSE) for details.
