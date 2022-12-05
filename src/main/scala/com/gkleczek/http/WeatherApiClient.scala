package com.gkleczek
package http
import cats.data.EitherT
import cats.effect.{ContextShift, IO, Resource}
import com.github.benmanes.caffeine.cache.{Cache, Caffeine}
import com.gkleczek.http.models.ApiResponses.{
  AstronomyResponse,
  WeatherResponse
}
import com.gkleczek.http.models.AppErrors.{AppError, JsonParsingError}
import com.gkleczek.http.models.JsonCodecs
import io.circe.{Decoder, Json}
import org.http4s.Method.GET
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.io._
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scalacache.caffeine.CaffeineCache
import scalacache.{CacheConfig, Entry, Mode, cachingF}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class WeatherApiClient(city: String, cacheTTL: FiniteDuration)(implicit
    cs: ContextShift[IO]
) extends JsonCodecs {

  private val underlyingCache: Cache[String, Entry[Either[AppError, Json]]] =
    Caffeine
      .newBuilder()
      .build()

  private implicit val cache: CaffeineCache[Either[AppError, Json]] =
    CaffeineCache(underlyingCache)(CacheConfig.defaultCacheConfig)

  implicit val mode: Mode[IO] = scalacache.CatsEffect.modes.async

  private val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private val baseQueryParams: Map[String, String] = {
    Map(
      "key" -> "e11d0c12dd0b4f7f935183400211807",
      "q"   -> city,
      "aqi" -> "yes"
    )
  }
  private val baseUri: Uri = Uri
    .unsafeFromString("https://api.weatherapi.com")
    .withQueryParams(baseQueryParams)

  private val clientResource: Resource[IO, Client[IO]] =
    BlazeClientBuilder[IO](ExecutionContext.global).resource

  def getAstronomy: EitherT[IO, AppError, AstronomyResponse] =
    for {
      astronomyJson <- getFromCache("astronomy", () => fetchAstronomy)
      astronomy     <- parse[AstronomyResponse](astronomyJson)
    } yield astronomy

  def getWeather: EitherT[IO, AppError, WeatherResponse] =
    for {
      weatherJson <- getFromCache("weather", () => fetchWeather)
      weather     <- parse[WeatherResponse](weatherJson)
    } yield weather

  def fetchAstronomy: EitherT[IO, AppError, Json] = {
    val result = for {
      _        <- logger.info("Fetching astronomy")
      response <- clientResource.use { client =>
                    val request = GET(
                      baseUri / "v1" / "astronomy.json"
                    )
                    client.expect[Json](request)
                  }
    } yield response
    EitherT.right(result)
  }

  def fetchWeather: EitherT[IO, AppError, Json] = {
    val result = for {
      _        <- logger.info("Fetching weather")
      response <- clientResource.use { client =>
                    val request = GET(
                      baseUri / "v1" / "current.json"
                    )
                    client.expect[Json](request)
                  }
    } yield response
    EitherT.right(result)
  }

  private def getFromCache(
      key: String,
      fun: () => EitherT[IO, AppError, Json]
  ): EitherT[IO, AppError, Json] = {
    val result = cachingF(key)(Some(cacheTTL)) {
      fun().value
    }
    EitherT(result)
  }

  private def parse[T](
      json: Json
  )(implicit decoder: Decoder[T]): EitherT[IO, AppError, T] = {
    val result: Either[JsonParsingError, T] = json
      .as[T]
      .fold(failure => Left(JsonParsingError(failure)), r => Right(r))
    EitherT.fromEither[IO](result)
  }

}
