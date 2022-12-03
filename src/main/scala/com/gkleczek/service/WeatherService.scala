package com.gkleczek.service

import cats.data.EitherT
import cats.effect.{IO, Timer}
import com.gkleczek.http.models.AppErrors.AppError
import com.gkleczek.panels.{
  AirQualityPanel,
  AstronomyPanel,
  MainWindow,
  WeatherPanel
}
import fs2.Stream
import org.typelevel.log4cats._
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration._

class WeatherService(
    weatherPanel: WeatherPanel,
    astronomyPanel: AstronomyPanel,
    airQualityPanel: AirQualityPanel
)(implicit timer: Timer[IO]) {

  private val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def run(mainWindow: MainWindow): IO[Either[AppError, Unit]] =
    for {
      _ <- logger.info("Starting main stream")
      result <- Stream
        .emits(weatherPanel :: astronomyPanel :: airQualityPanel :: Nil)
        .repeat
        .evalMap { panel =>
          for {
            _ <- panel.update()
            _ <- mainWindow.showPanel(panel.panel)
            done <- EitherT.right[AppError](IO.sleep(10.seconds))
          } yield done
        }
        .compile
        .drain
        .value
    } yield result
}
