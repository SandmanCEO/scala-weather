package com.gkleczek.panels

import cats.effect.IO
import com.gkleczek.http.models.ApiResponses.WeatherResponse
import com.gkleczek.http.{ImageProvider, WeatherApiClient}

import java.awt.Color
import javax.swing.ImageIcon
import javax.swing.border.EmptyBorder
import scala.swing.{GridBagPanel, Label}

class AirQualityPanel(service: WeatherApiClient, imageProvider: ImageProvider)
    extends Panel {

  val panel = new GridBagPanel

  private val carbonOxideLabel = new Label()
  private val nitrousDioxideLabel = new Label()
  private val ozoneLabel = new Label()
  private val sulphurDioxideLabel = new Label()
  private val pm25Label = new Label()
  private val pm10Label = new Label()
  private val indexIcon = new Label()

  private val allLabels: Set[Label] = Set(
    carbonOxideLabel,
    nitrousDioxideLabel,
    ozoneLabel,
    sulphurDioxideLabel,
    pm25Label,
    pm10Label,
    indexIcon
  )

  buildPanel()

  override def update(): IO[Unit] =
    for {
      weather <- service.getWeather
      indexImage <- imageProvider.loadImageFromIndex(
        weather.currentWeather.airQuality.index
      )
    } yield updateValues(weather, indexImage)

  def updateValues(
      weather: WeatherResponse,
      indexImageData: Array[Byte]
  ): Unit = {
    carbonOxideLabel.text =
      s"Carbon oxide: ${weather.currentWeather.airQuality.carbonOxide} μg/m3"
    nitrousDioxideLabel.text =
      s"Nitrous dioxide: ${weather.currentWeather.airQuality.nitrousDioxide} μg/m3"
    ozoneLabel.text = s"Ozone: ${weather.currentWeather.airQuality.ozone} μg/m3"
    sulphurDioxideLabel.text =
      s"Sulphur dioxide: ${weather.currentWeather.airQuality.sulphurDioxide} μg/m3"
    pm25Label.text = s"PM 2.5: ${weather.currentWeather.airQuality.pm25} μg/m3"
    pm10Label.text = s"PM 10: ${weather.currentWeather.airQuality.pm10} μg/m3"
    indexIcon.icon = new ImageIcon(indexImageData)
  }

  private def buildPanel(): Unit = {
    panel.background = Color.DARK_GRAY

    allLabels.foreach { label =>
      label.foreground = Color.WHITE
      label.border = new EmptyBorder(10, 0, 10, 0)
    }

    val carbonOxideConstraint = new panel.Constraints()
    carbonOxideConstraint.grid = (0, 0)
    panel.layout += carbonOxideLabel -> carbonOxideConstraint

    val nitrousDioxideConstraint = new panel.Constraints()
    nitrousDioxideConstraint.grid = (2, 0)
    panel.layout += nitrousDioxideLabel -> nitrousDioxideConstraint

    val ozoneConstraint = new panel.Constraints()
    ozoneConstraint.grid = (0, 1)
    panel.layout += ozoneLabel -> ozoneConstraint

    val sulphurDioxideConstraint = new panel.Constraints()
    sulphurDioxideConstraint.grid = (2, 1)
    panel.layout += sulphurDioxideLabel -> sulphurDioxideConstraint

    val pm25Constraint = new panel.Constraints()
    pm25Constraint.grid = (0, 2)
    panel.layout += pm25Label -> pm25Constraint

    val pm10Constraint = new panel.Constraints()
    pm10Constraint.grid = (2, 2)
    panel.layout += pm10Label -> pm10Constraint

    val indexConstraint = new panel.Constraints()
    indexConstraint.grid = (1, 3)
    panel.layout += indexIcon -> indexConstraint
    ()
  }
}
