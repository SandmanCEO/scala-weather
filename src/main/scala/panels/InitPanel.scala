package com.gkleczek
package panels

import java.awt.Color
import javax.swing.border.EmptyBorder
import scala.swing.{GridBagPanel, Label}

class InitPanel {

  val panel = new GridBagPanel

  private val loadingLabel = new Label("Loading ...")

  private val allLabels: Set[Label] = Set(
    loadingLabel
  )

  buildPanel()

  private def buildPanel(): Unit = {
    panel.background = Color.DARK_GRAY

    allLabels.foreach { label =>
      label.foreground = Color.WHITE
      label.border = new EmptyBorder(10, 10, 10, 10)
    }

    val loadingConstraint = new panel.Constraints()
    loadingConstraint.grid = (0, 0)
    panel.layout += loadingLabel -> loadingConstraint
    ()
  }
}
