package com.gkleczek
package panels

import http.models.ApiResponses.AstronomyResponse

import java.awt.Color
import javax.swing.ImageIcon
import javax.swing.border.EmptyBorder
import scala.swing.{GridBagPanel, Label}

class AstronomyPanel {

  val panel = new GridBagPanel

  private val sunriseLabel = new Label()
  private val sunsetLabel = new Label()
  private val moonriseLabel = new Label()
  private val moonSetLabel = new Label()
  private val sunIcon = new Label()
  private val moonIcon = new Label()

  private val allLabels: Set[Label] = Set(
    sunriseLabel,
    sunsetLabel,
    moonriseLabel,
    moonSetLabel,
    sunIcon,
    moonIcon
  )

  buildPanel()

  def updateValues(
      astronomy: AstronomyResponse,
      sunIconData: Array[Byte],
      moonIconData: Array[Byte]
  ): Unit = {
    sunriseLabel.text = s"Sunrise: ${astronomy.astronomy.sunrise}"
    sunsetLabel.text = s"Sunset: ${astronomy.astronomy.sunset}"
    moonriseLabel.text = s"Moonrise: ${astronomy.astronomy.moonrise}"
    moonSetLabel.text = s"Moon set: ${astronomy.astronomy.moonSet}"
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
