package com.gkleczek
package service

import http.{ImageProvider, WeatherApiClient}
import panels.{AirQualityPanel, AstronomyPanel, MainWindow, WeatherPanel}

import akka.Done
import akka.event.LoggingAdapter
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

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
      .fromIterator(() => (0 until Int.MaxValue).iterator)
      .throttle(1, 10.seconds)
      .map(_ % 3)
      .mapAsyncUnordered(1) {
        case 0 =>
          updateWeatherPanel().map(_ =>
            mainWindow.showPanel(weatherPanel.panel)
          )
        case 1 =>
          updateAstronomyPanel().map(_ =>
            mainWindow.showPanel(astronomyPanel.panel)
          )
        case 2 =>
          updateAirQualityPanel().map(_ =>
            mainWindow.showPanel(airQualityPanel.panel)
          )
      }
      .runWith(Sink.ignore)
  }

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
