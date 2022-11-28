package com.gkleczek
import cats.effect.{ExitCode, IO, IOApp}
import com.gkleczek.http.{ImageProvider, WeatherApiCache, WeatherApiClient}
import com.gkleczek.panels._
import com.gkleczek.service.WeatherService
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {

  private val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]
  private val cache: WeatherApiCache = new WeatherApiCache()
  private val weatherProvider = new WeatherApiClient(AppConfig.City, cache)
  private val imageProvider = new ImageProvider
  private val weatherPanel = new WeatherPanel(weatherProvider, imageProvider)
  private val astronomyPanel =
    new AstronomyPanel(weatherProvider, imageProvider)
  private val airQualityPanel =
    new AirQualityPanel(weatherProvider, imageProvider)
  private val initPanel = new InitPanel
  private val mainFrame = new MainWindow

  private val weatherService =
    new WeatherService(
      weatherPanel,
      astronomyPanel,
      airQualityPanel
    )

  mainFrame.showPanel(initPanel.panel)

  override def run(args: List[String]): IO[ExitCode] = {
    weatherService
      .run(mainFrame)
      .handleErrorWith { error =>
        logger
          .error(error)("Error while running program!")
          .map(_ => ExitCode.Error)
      }
      .as(ExitCode.Success)
  }
}
