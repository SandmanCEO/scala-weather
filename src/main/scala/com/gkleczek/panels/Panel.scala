package com.gkleczek.panels

import cats.effect.IO

import scala.swing.GridBagPanel

trait Panel {
  def update(): IO[Unit]

  def panel: GridBagPanel
}
