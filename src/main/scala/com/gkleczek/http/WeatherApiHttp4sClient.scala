package com.gkleczek
package http
import akka.event.LoggingAdapter
import com.gkleczek.http.models.JsonCodecs

import scala.concurrent.ExecutionContext

class WeatherApiHttp4sClient(city: String)(implicit
    logger: LoggingAdapter
) extends JsonCodecs {

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

  private val client: Resource[IO, Client[IO]] =
    BlazeClientBuilder[IO](ExecutionContext.global).resource

  def getAstronomy = {
    client.map(_.expect[String](baseUri))
  }

}
