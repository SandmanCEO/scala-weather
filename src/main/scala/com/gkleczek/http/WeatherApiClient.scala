package com.gkleczek.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.gkleczek.AppConfig
import com.gkleczek.http.models.ApiResponses.{
  AstronomyResponse,
  WeatherResponse
}
import com.gkleczek.http.models.JsonSupport
import spray.json.JsValue

import scala.concurrent.{ExecutionContext, Future}

trait WeatherApiClient {
  def getAstronomy: Future[AstronomyResponse]
  def getWeather: Future[WeatherResponse]
}

class WeatherApiAkkaClient(city: String)(implicit
    system: ActorSystem,
    mat: Materializer,
    logger: LoggingAdapter
) extends SprayJsonSupport
    with AkkaJsonFormatters
    with WeatherApiClient {

  private implicit val ec: ExecutionContext = system.dispatcher
  private val baseUri: Uri = Uri("https://api.weatherapi.com")
  private val baseQueryParams: Query = {
    Query(
      "key" -> "e11d0c12dd0b4f7f935183400211807",
      "q" -> city,
      "aqi" -> "yes"
    )
  }

  def getAstronomy: Future[AstronomyResponse] = {
    val request = HttpRequest(
      uri = baseUri
        .withPath(baseUri.path / "v1" / "astronomy.json")
        .withQuery(baseQueryParams)
    )
    executeRequest(request).map(_.convertTo[AstronomyResponse])
  }

  def getWeather: Future[WeatherResponse] = {
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
      .recoverWith { ex =>
        logger.error(ex, "Recovering on exception for http request")
        executeRequest(request)
      }
  }
}
