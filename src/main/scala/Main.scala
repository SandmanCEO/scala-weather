package com.gkleczek
import http.{ImageProvider, WeatherApiClient}
import panels._
import service.WeatherService

import akka.Done
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}

object Main extends App {
  private implicit val system: ActorSystem = ActorSystem("scala-weather")
  private implicit val ex: ExecutionContext = system.dispatcher
  private implicit val mat: Materializer = Materializer(system)
  private implicit val logger: LoggingAdapter =
    Logging.getLogger(system, this.getClass)

  private val weatherProvider = new WeatherApiClient(AppConfig.City)
  private val imageProvider = new ImageProvider
  private val weatherPanel = new WeatherPanel
  private val astronomyPanel = new AstronomyPanel
  private val airQualityPanel = new AirQualityPanel
  private val initPanel = new InitPanel
  private val mainFrame = new MainWindow

  private val weatherService =
    new WeatherService(
      weatherPanel,
      astronomyPanel,
      airQualityPanel,
      weatherProvider,
      imageProvider
    )

  mainFrame.showPanel(initPanel.panel)

  weatherService
    .run(mainFrame)
    .recoverWith { ex =>
      logger.error(ex, "Fatal error encountered")
      Future.successful(Done)
    }

}
