package com.gkleczek.service

import cats.effect.{IO, Timer}
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

  def run(mainWindow: MainWindow): IO[Unit] =
    for {
      _ <- logger.info("Starting main stream")
      result <- Stream
        .emits(weatherPanel :: astronomyPanel :: airQualityPanel :: Nil)
        .repeat
        .evalMap { panel =>
          for {
            _ <- panel.update()
            _ <- IO(mainWindow.showPanel(panel.panel))
            done <- IO.sleep(10.seconds)
          } yield done
        }
        .compile
        .drain
    } yield result
}
