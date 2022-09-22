import sbt._

object Dependencies {

  val all: Seq[ModuleID] = Seq(
    "org.scala-lang.modules" %% "scala-swing" % Versions.swing,
    "com.typesafe.akka" %% "akka-http" % Versions.akkaHttp,
    "com.typesafe.akka" %% "akka-http-spray-json" % Versions.akkaHttp,
    "io.spray" %% "spray-json" % Versions.spray,
    "com.typesafe.akka" %% "akka-actor" % Versions.akka,
    "com.typesafe.akka" %% "akka-slf4j" % Versions.akka,
    "com.typesafe.akka" %% "akka-stream" % Versions.akka,
    "com.github.blemale" %% "scaffeine" % Versions.scaffeine,
    "ch.qos.logback" % "logback-classic" % Versions.logback
  )

  object Versions {
    val swing = "3.0.0"
    val akkaHttp = "10.2.7"
    val spray = "1.3.6"
    val akka = "2.6.18"
    val scaffeine = "4.1.0"
    val logback = "1.2.11"
  }
}
