package com.gkleczek.http

import cats.effect.{ContextShift, IO}
import com.github.benmanes.caffeine.cache.{Cache, Caffeine}
import com.gkleczek.http.models.ApiResponses.{
  AstronomyResponse,
  WeatherResponse
}
import scalacache.caffeine.CaffeineCache
import scalacache.memoization.memoizeF
import scalacache.{CacheConfig, Entry, Mode}

import java.time.Duration
import scala.concurrent.duration._

class WeatherApiCache()(implicit
    cs: ContextShift[IO]
) {

  private val underlyingAstronomyCache
      : Cache[String, Entry[AstronomyResponse]] = Caffeine
    .newBuilder()
    .expireAfterWrite(Duration.ofMinutes(5L))
    .build()

  private implicit val astronomyCache: CaffeineCache[AstronomyResponse] =
    CaffeineCache(underlyingAstronomyCache)(CacheConfig.defaultCacheConfig)

  private val underlyingWeatherCache: Cache[String, Entry[WeatherResponse]] =
    Caffeine
      .newBuilder()
      .expireAfterWrite(Duration.ofMinutes(5L))
      .build()

  private implicit val weatherCache: CaffeineCache[WeatherResponse] =
    CaffeineCache(underlyingWeatherCache)(CacheConfig.defaultCacheConfig)

  implicit val mode: Mode[IO] = scalacache.CatsEffect.modes.async

  def getAstronomy(fun: () => IO[AstronomyResponse]): IO[AstronomyResponse] = {
    memoizeF[IO, AstronomyResponse](Some(5.minutes)) {
      fun()
    }
  }

  def getWeather(fun: () => IO[WeatherResponse]): IO[WeatherResponse] = {
    memoizeF[IO, WeatherResponse](Some(5.minutes)) {
      fun()
    }
  }
}
