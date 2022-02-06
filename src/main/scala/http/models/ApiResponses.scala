package com.gkleczek
package http.models

import java.time.{LocalDateTime, LocalTime}

object ApiResponses {

  final case class WeatherCondition(condition: String, icon: String)

  final case class AirQuality(
      carbonOxide: Double,
      nitrousDioxide: Double,
      ozone: Double,
      sulphurDioxide: Double,
      pm25: Double,
      pm10: Double,
      index: Int
  )

  final case class CurrentWeather(
      lastUpdated: LocalDateTime,
      temperature: Double,
      feelsLikeTemperature: Double,
      windSpeed: Double,
      pressure: Double,
      humidity: Int,
      condition: WeatherCondition,
      airQuality: AirQuality
  )

  final case class WeatherLocation(name: String, country: String)

  final case class WeatherResponse(
      location: WeatherLocation,
      currentWeather: CurrentWeather
  )

  final case class AstronomyData(
      sunrise: LocalTime,
      sunset: LocalTime,
      moonrise: LocalTime,
      moonSet: LocalTime
  )

  final case class AstronomyResponse(
      location: WeatherLocation,
      astronomy: AstronomyData
  )
}
