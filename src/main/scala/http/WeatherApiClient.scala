package com.gkleczek
package http

import http.models.ApiResponses.{AstronomyResponse, WeatherResponse}
import http.models.JsonSupport

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import spray.json.JsValue

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class WeatherApiClient(city: String)(implicit
    system: ActorSystem,
    mat: Materializer,
    logger: LoggingAdapter
) extends SprayJsonSupport
    with JsonSupport {

  private implicit val ec: ExecutionContext = system.dispatcher
  private val baseUri: Uri = Uri("https://api.weatherapi.com")
  private val baseQueryParams: Query = {
    Query(
      "key" -> "e11d0c12dd0b4f7f935183400211807",
      "q" -> city,
      "aqi" -> "yes"
    )
  }
  private val astronomyCache: LoadingCache[Unit, Future[AstronomyResponse]] =
    Scaffeine()
      .expireAfterWrite(1.day)
      .maximumSize(1)
      .build(_ => fetchAstronomy)

  private val weatherCache: LoadingCache[Unit, Future[WeatherResponse]] =
    Scaffeine()
      .expireAfterWrite(AppConfig.WeatherCacheTtl)
      .maximumSize(1)
      .build(_ => fetchWeather)

  def getAstronomy: Future[AstronomyResponse] = {
    astronomyCache.get(())
  }

  def getWeather: Future[WeatherResponse] = {
    weatherCache.get(())
  }

  private def fetchAstronomy: Future[AstronomyResponse] = {
    val request = HttpRequest(
      uri = baseUri
        .withPath(baseUri.path / "v1" / "astronomy.json")
        .withQuery(baseQueryParams)
    )
    executeRequest(request).map(_.convertTo[AstronomyResponse])
  }

  private def fetchWeather: Future[WeatherResponse] = {
    val request = HttpRequest(
      uri = baseUri
        .withPath(baseUri.path / "v1" / "current.json")
        .withQuery(baseQueryParams)
    )
    executeRequest(request).map(_.convertTo[WeatherResponse])
  }

  private def executeRequest(request: HttpRequest): Future[JsValue] = {
    logger.info("Executing api request: {}", request)
    Http()
      .singleRequest(request)
      .flatMap(response => Unmarshal(response.entity).to[JsValue])
  }
}
