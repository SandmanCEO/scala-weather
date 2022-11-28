package com.gkleczek.http.models

import java.time.{LocalDateTime, LocalTime}

object ApiResponses {

  type MaybeLocalTime = Either[String, LocalTime]

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
      sunrise: MaybeLocalTime,
      sunset: MaybeLocalTime,
      moonrise: MaybeLocalTime,
      moonSet: MaybeLocalTime
  )

  final case class AstronomyResponse(
      location: WeatherLocation,
      astronomy: AstronomyData
  )
}
