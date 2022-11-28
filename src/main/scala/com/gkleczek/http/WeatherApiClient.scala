package com.gkleczek
package http
import cats.effect.{ContextShift, IO, Resource}
import com.gkleczek.http.models.ApiResponses.{
  AstronomyResponse,
  WeatherResponse
}
import com.gkleczek.http.models.JsonCodecs
import org.http4s.Method.GET
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.io._
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.ExecutionContext

class WeatherApiClient(city: String, cache: WeatherApiCache)(implicit
    cs: ContextShift[IO]
) extends JsonCodecs {

  private val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private val baseQueryParams: Map[String, String] = {
    Map(
      "key" -> "e11d0c12dd0b4f7f935183400211807",
      "q" -> city,
      "aqi" -> "yes"
    )
  }
  private val baseUri: Uri = Uri
    .unsafeFromString("https://api.weatherapi.com")
    .withQueryParams(baseQueryParams)

  private val clientResource: Resource[IO, Client[IO]] =
    BlazeClientBuilder[IO](ExecutionContext.global).resource

  def getAstronomy: IO[AstronomyResponse] =
    cache.getAstronomy(() => fetchAstronomy)

  def getWeather: IO[WeatherResponse] =
    cache.getWeather(() => fetchWeather)

  def fetchAstronomy: IO[AstronomyResponse] =
    for {
      _ <- logger.info("Fetching astronomy")
      response <- clientResource.use { client =>
        val request = GET(
          baseUri / "v1" / "astronomy.json"
        )
        client.expect[AstronomyResponse](request)
      }
    } yield response

  def fetchWeather: IO[WeatherResponse] =
    for {
      _ <- logger.info("Fetching weather")
      response <- clientResource.use { client =>
        val request = GET(
          baseUri / "v1" / "current.json"
        )
        client.expect[WeatherResponse](request)
      }
    } yield response

}
