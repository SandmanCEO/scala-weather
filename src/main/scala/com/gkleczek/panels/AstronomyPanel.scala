package com.gkleczek.panels

import cats.data.EitherT
import cats.effect.IO
import com.gkleczek.http.models.ApiResponses.AstronomyResponse
import com.gkleczek.http.models.AppErrors.AppError
import com.gkleczek.http.{ImageProvider, WeatherApiClient}

import java.awt.Color
import javax.swing.ImageIcon
import javax.swing.border.EmptyBorder
import scala.swing.{GridBagPanel, Label}

class AstronomyPanel(service: WeatherApiClient, imageProvider: ImageProvider)
    extends Panel {

  val panel = new GridBagPanel

  private val sunriseLabel  = new Label()
  private val sunsetLabel   = new Label()
  private val moonriseLabel = new Label()
  private val moonSetLabel  = new Label()
  private val sunIcon       = new Label()
  private val moonIcon      = new Label()

  private val allLabels: Set[Label] = Set(
    sunriseLabel,
    sunsetLabel,
    moonriseLabel,
    moonSetLabel,
    sunIcon,
    moonIcon
  )

  buildPanel()

  override def update(): EitherT[IO, AppError, Unit] =
    for {
      astronomy <- service.getAstronomy
      sunIcon   <- imageProvider.loadImage(
                     "http://cdn.weatherapi.com/weather/64x64/day/113.png"
                   )
      moonIcon  <- imageProvider.loadImage(
                     "http://cdn.weatherapi.com/weather/64x64/night/113.png"
                   )
    } yield updateValues(astronomy, sunIcon, moonIcon)

  def updateValues(
      astronomy: AstronomyResponse,
      sunIconData: Array[Byte],
      moonIconData: Array[Byte]
  ): Unit = {
    val sunriseHour: String  =
      astronomy.astronomy.sunrise.fold(s => s, d => d.toString)
    val sunsetHour: String   =
      astronomy.astronomy.sunset.fold(s => s, d => d.toString)
    val moonriseHour: String =
      astronomy.astronomy.moonrise.fold(s => s, d => d.toString)
    val moonSetHour: String  =
      astronomy.astronomy.moonSet.fold(s => s, d => d.toString)
    sunriseLabel.text = s"Sunrise: $sunriseHour"
    sunsetLabel.text = s"Sunset: $sunsetHour"
    moonriseLabel.text = s"Moonrise: $moonriseHour"
    moonSetLabel.text = s"Moon set: $moonSetHour"
    sunIcon.icon = new ImageIcon(sunIconData)
    moonIcon.icon = new ImageIcon(moonIconData)
  }

  private def buildPanel(): Unit = {
    panel.background = Color.DARK_GRAY

    allLabels.foreach { label =>
      label.foreground = Color.WHITE
      label.border = new EmptyBorder(10, 10, 10, 10)
    }

    val sunriseConstraint = new panel.Constraints()
    sunriseConstraint.grid = (0, 0)
    panel.layout += sunriseLabel -> sunriseConstraint

    val sunIconConstraint = new panel.Constraints()
    sunIconConstraint.grid = (1, 0)
    panel.layout += sunIcon -> sunIconConstraint

    val sunsetConstraint = new panel.Constraints()
    sunsetConstraint.grid = (2, 0)
    panel.layout += sunsetLabel -> sunsetConstraint

    val moonriseConstraint = new panel.Constraints()
    moonriseConstraint.grid = (0, 1)
    panel.layout += moonriseLabel -> moonriseConstraint

    val moonIconConstraint = new panel.Constraints()
    moonIconConstraint.grid = (1, 1)
    panel.layout += moonIcon -> moonIconConstraint

    val moonSetConstraint = new panel.Constraints()
    moonSetConstraint.grid = (2, 1)
    panel.layout += moonSetLabel -> moonSetConstraint
    ()
  }
}
