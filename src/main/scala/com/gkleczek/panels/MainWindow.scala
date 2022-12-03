package com.gkleczek.panels

import cats.data.EitherT
import cats.effect.IO
import com.gkleczek.http.models.AppErrors.AppError

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

  def showPanel(panel: Panel): EitherT[IO, AppError, Unit] = {
    EitherT.right {
      IO {
        frame.contents = panel
        frame.size = defaultFrameSize
      }
    }
  }

  def close: IO[Unit] = {
    IO(frame.close())
  }
}
