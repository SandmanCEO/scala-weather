package com.gkleczek
package panels

import java.awt.Dimension
import scala.swing.{Frame, Panel}

class MainWindow {

  private val defaultFrameSize = new Dimension(470, 270)

  private val frame = new Frame {
    title = "Hello from Scala Weather"
    size = defaultFrameSize

    centerOnScreen()
    open()
  }

  def showPanel(panel: Panel): Unit = {
    frame.contents = panel
    frame.size = defaultFrameSize
  }
}
