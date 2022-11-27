package com.gkleczek
import http.{ImageProvider, WeatherApiAkkaClient}
import panels._
import service.WeatherService

import akka.Done
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.Materializer
import com.gkleczek.http.{ImageProvider, WeatherApiClient}
import com.gkleczek.panels._
import com.gkleczek.service.WeatherService

import scala.concurrent.{ExecutionContext, Future}

object Main extends App {
  private implicit val system: ActorSystem = ActorSystem("scala-weather")
  private implicit val ex: ExecutionContext = system.dispatcher
  private implicit val mat: Materializer = Materializer(system)
  private implicit val logger: LoggingAdapter =
    Logging(system.eventStream, this.getClass)

  private val weatherProvider = new WeatherApiAkkaClient(AppConfig.City)
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
