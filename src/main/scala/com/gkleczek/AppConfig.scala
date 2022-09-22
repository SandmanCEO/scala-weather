package com.gkleczek

import com.typesafe.config.{Config, ConfigFactory}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object AppConfig {

  private val appConfig: Config = ConfigFactory.load()

  val City: String = appConfig.getString("city")
  val WeatherCacheTtl: FiniteDuration = FiniteDuration(
    appConfig.getDuration("weather-cache-ttl").toMillis,
    TimeUnit.MILLISECONDS
  )
}
