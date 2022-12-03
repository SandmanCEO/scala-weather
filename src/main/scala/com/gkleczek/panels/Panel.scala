package com.gkleczek.panels

import cats.data.EitherT
import cats.effect.IO
import com.gkleczek.http.models.AppErrors.AppError

import scala.swing.GridBagPanel

trait Panel {
  def update(): EitherT[IO, AppError, Unit]

  def panel: GridBagPanel
}
