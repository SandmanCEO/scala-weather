# scala-weather

Scala program for RaspberryPI showing current weather.
It uses akka-http and akka-streams for main functionalities.

Required configuration:

* `CITY_NAME` - City for fetching current weather. Example: `Warsaw`
* `WEATHER_CACHE_TTL` - Weather cache TTL. Example: `5m`
* `DISPLAY` - Required configuration when running on external device via SSH. Example: `:0.0`