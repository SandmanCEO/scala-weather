import sbt._

object Dependencies {

  val all: Seq[ModuleID] = Seq(
    "org.scala-lang.modules" %% "scala-swing"            % Versions.swing,
    "ch.qos.logback"          % "logback-classic"        % Versions.logback,
    "org.http4s"             %% "http4s-dsl"             % Versions.http4s,
    "org.http4s"             %% "http4s-circe"           % Versions.http4s,
    "org.http4s"             %% "http4s-blaze-client"    % Versions.http4s,
    "io.circe"               %% "circe-core"             % Versions.circe,
    "co.fs2"                 %% "fs2-core"               % Versions.fs2,
    "co.fs2"                 %% "fs2-io"                 % Versions.fs2,
    "co.fs2"                 %% "fs2-reactive-streams"   % Versions.fs2,
    "org.typelevel"          %% "log4cats-slf4j"         % Versions.log4cats,
    "com.github.cb372"       %% "scalacache-core"        % Versions.cache,
    "com.github.cb372"       %% "scalacache-caffeine"    % Versions.cache,
    "com.github.cb372"       %% "scalacache-cats-effect" % Versions.cache,
    "com.github.pureconfig"  %% "pureconfig"             % Versions.config
  )

  object Versions {
    val swing    = "3.0.0"
    val logback  = "1.2.11"
    val http4s   = "1.0-234-d1a2b53"
    val circe    = "0.13.0"
    val fs2      = "2.5.0"
    val log4cats = "1.7.0"
    val config   = "0.17.2"
    val cache    = "0.28.0"
  }
}
