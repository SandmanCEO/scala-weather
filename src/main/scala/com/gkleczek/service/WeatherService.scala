package com.gkleczek.service

import akka.Done
import akka.event.LoggingAdapter
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.gkleczek.http.{ImageProvider, WeatherApiClient}
import com.gkleczek.panels.{
  AirQualityPanel,
  AstronomyPanel,
  MainWindow,
  WeatherPanel
}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.swing.GridBagPanel

class WeatherService(
    weatherPanel: WeatherPanel,
    astronomyPanel: AstronomyPanel,
    airQualityPanel: AirQualityPanel,
    weatherApiClient: WeatherApiClient,
    imageProvider: ImageProvider
)(implicit ex: ExecutionContext, mat: Materializer, logger: LoggingAdapter) {

  def run(mainWindow: MainWindow): Future[Done] = {
    logger.info("Starting main stream")
    Source
      .cycle(() => panelsWithUpdateFunctions.iterator)
      .throttle(1, 10.seconds)
      .mapAsyncUnordered(1) { case (panel, updateFunc) =>
        updateFunc().map(_ => mainWindow.showPanel(panel))
      }
      .runWith(Sink.ignore)
  }

  private val panelsWithUpdateFunctions
      : List[(GridBagPanel, () => Future[Done])] =
    weatherPanel.panel -> (() =>
      updateWeatherPanel()
    ) :: astronomyPanel.panel -> (() =>
      updateAstronomyPanel()
    ) :: airQualityPanel.panel -> (() => updateAirQualityPanel()) :: Nil

  private def updateWeatherPanel(): Future[Done] =
    for {
      weather <- weatherApiClient.getWeather
      image <- imageProvider.loadImage(weather.currentWeather.condition.icon)
      _ = weatherPanel.updateValues(weather, image)
    } yield Done

  private def updateAstronomyPanel(): Future[Done] =
    for {
      astronomy <- weatherApiClient.getAstronomy
      sunIcon <- imageProvider.loadImage(
        "http://cdn.weatherapi.com/weather/64x64/day/113.png"
      )
      moonIcon <- imageProvider.loadImage(
        "http://cdn.weatherapi.com/weather/64x64/night/113.png"
      )
      _ = astronomyPanel.updateValues(astronomy, sunIcon, moonIcon)
    } yield Done

  private def updateAirQualityPanel(): Future[Done] =
    for {
      weather <- weatherApiClient.getWeather
      indexImage = imageProvider.loadImageFromIndex(
        weather.currentWeather.airQuality.index
      )
      _ = airQualityPanel.updateValues(weather, indexImage)
    } yield Done
}
