package com.gkleczek
import cats.effect._
import com.gkleczek.http.{ImageProvider, WeatherApiClient}
import com.gkleczek.panels._
import com.gkleczek.service.WeatherService
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._

object Main extends IOApp {

  private val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private def createApp: Either[
    ConfigReaderFailures,
    WeatherService
  ] =
    ConfigSource.default.load[AppConfig].map { config =>
      val weatherProvider =
        new WeatherApiClient(config.city, config.weatherCacheTtl)
      val imageProvider   = new ImageProvider
      val weatherPanel    = new WeatherPanel(weatherProvider, imageProvider)
      val astronomyPanel  =
        new AstronomyPanel(weatherProvider, imageProvider)
      val airQualityPanel =
        new AirQualityPanel(weatherProvider, imageProvider)
      val initPanel       = new InitPanel
      val mainFrame       = new MainWindow
      val weatherService  =
        new WeatherService(
          mainFrame,
          weatherPanel,
          astronomyPanel,
          airQualityPanel
        )
      mainFrame.showPanel(initPanel.panel)

      weatherService
    }

  private def program(
      weatherService: WeatherService
  ): IO[ExitCode] =
    weatherService.run
      .flatMap { result =>
        result.fold(
          error =>
            for {
              _      <- logger.error(s"Error while running program! $error")
              result <- weatherService.close
            } yield result,
          _ => logger.info("Program finished with success!")
        )
      }
      .handleErrorWith { error =>
        for {
          _ <- logger.error(error)("Fatal error while running program!")
          _ <- weatherService.close
        } yield ExitCode.Error
      }
      .as(ExitCode.Success)

  override def run(args: List[String]): IO[ExitCode] =
    createApp match {
      case Right(weatherService) => program(weatherService)
      case Left(failures)        =>
        logger
          .error(s"${failures.prettyPrint()}")
          .as(ExitCode.Error)
    }
}
