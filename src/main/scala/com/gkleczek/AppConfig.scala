package com.gkleczek

import scala.concurrent.duration.FiniteDuration

final case class AppConfig(city: String, weatherCacheTtl: FiniteDuration)
