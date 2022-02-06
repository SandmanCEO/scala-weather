package com.gkleczek
package panels

import http.models.ApiResponses.WeatherResponse

import java.awt.Color
import javax.swing.ImageIcon
import javax.swing.border.EmptyBorder
import scala.swing.{GridBagPanel, Label}

class WeatherPanel {

  val panel = new GridBagPanel

  private val conditionLabel = new Label()
  private val temperatureLabel = new Label()
  private val feelsLikeTemperatureLabel = new Label()
  private val humidityLabel = new Label()
  private val windLabel = new Label()
  private val pressureLabel = new Label()
  private val weatherIcon = new Label()

  private val allLabels: Set[Label] = Set(
    conditionLabel,
    temperatureLabel,
    feelsLikeTemperatureLabel,
    humidityLabel,
    windLabel,
    pressureLabel,
    weatherIcon
  )

  buildPanel()

  def updateValues(weather: WeatherResponse, imageData: Array[Byte]): Unit = {
    conditionLabel.text =
      s"Weather in ${weather.location.name} for ${weather.currentWeather.lastUpdated} is ${weather.currentWeather.condition.condition}"
    temperatureLabel.text =
      s"Temperature: ${weather.currentWeather.temperature} C"
    feelsLikeTemperatureLabel.text =
      s"Sensed temperature: ${weather.currentWeather.feelsLikeTemperature} C"
    humidityLabel.text = s"Humidity: ${weather.currentWeather.humidity} %"
    windLabel.text = s"Wind: ${weather.currentWeather.windSpeed} kph"
    pressureLabel.text =
      s"Atmospheric pressure ${weather.currentWeather.pressure} hPa"
    weatherIcon.icon = new ImageIcon(imageData)
  }

  private def buildPanel(): Unit = {
    panel.background = Color.DARK_GRAY

    allLabels.foreach { label =>
      label.foreground = Color.WHITE
      label.border = new EmptyBorder(10, 10, 10, 10)
    }

    val conditionConstraint = new panel.Constraints()
    conditionConstraint.grid = (0, 0)
    conditionConstraint.gridwidth = 2
    panel.layout += conditionLabel -> conditionConstraint

    val temperatureConstraint = new panel.Constraints()
    temperatureConstraint.grid = (0, 1)
    panel.layout += temperatureLabel -> temperatureConstraint

    val feelsLikeConstraint = new panel.Constraints()
    feelsLikeConstraint.grid = (0, 2)
    panel.layout += feelsLikeTemperatureLabel -> feelsLikeConstraint

    val humidityConstraint = new panel.Constraints()
    humidityConstraint.grid = (0, 3)
    panel.layout += humidityLabel -> humidityConstraint

    val windLabelConstraint = new panel.Constraints()
    windLabelConstraint.grid = (0, 4)
    panel.layout += windLabel -> windLabelConstraint

    val pressureLabelConstraint = new panel.Constraints()
    pressureLabelConstraint.grid = (0, 5)
    panel.layout += pressureLabel -> pressureLabelConstraint

    val iconConstraint = new panel.Constraints()
    iconConstraint.grid = (1, 1)
    iconConstraint.gridheight = 2
    panel.layout += weatherIcon -> iconConstraint
    ()
  }
}
