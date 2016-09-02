##`1.0.2 (9/1/16)`
* Renamed `getAllPublicAndAuthenticatedPilotFlights` to `getAllPublicFlights`
* Getting flights now has a `startsAfterNow`, `startsBeforeNow`, `endsAfterNow`, and `endsBeforeNow`
* Renamed `isCurrent` to `isActive` in `AirMapFlight`
* Added 15 second timeout to requests
* Added ability to explicitly pass an `enhance` option when getting a flight
* Updated Gradle, Support Library, and Build Tools Versions
* Updated Properites Model Classes
* Added a constructor to `Coordinate` to create from a MapBox `LatLng`
* Create Flight UI updates
* Removed some unncessary Toasts
* Performance improvements
* Fixed bugs/crashes

##`1.0.1 (8/13/16)`

* Fixed a Manifest merger bug

##`1.0.0 (8/12/16)`
Initial release