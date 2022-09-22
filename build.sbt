import sbt.Keys.mainClass

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

assembly / assemblyJarName := "scala-weather.jar"

scalacOptions := Seq(
  "-encoding",
  "utf-8",
  "-explaintypes",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Ymacro-annotations",
  // Warnings as errors!
  "-Xfatal-warnings",
  // Linting options
  "-unchecked",
  "-Xcheckinit",
  "-Xlint:adapted-args",
  "-Xlint:constant",
  "-Xlint:delayedinit-select",
  "-Xlint:deprecation",
  "-Xlint:doc-detached",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:nullary-unit",
  "-Xlint:option-implicit",
  "-Xlint:package-object-classes",
  "-Xlint:poly-implicit-overload",
  "-Xlint:private-shadow",
  "-Xlint:stars-align",
  "-Xlint:type-parameter-shadow",
  "-Wdead-code",
  "-Wextra-implicit",
  "-Wnumeric-widen",
  "-Wunused:implicits",
  "-Wunused:imports",
  "-Wunused:locals",
  "-Wunused:params",
  "-Wunused:patvars",
  "-Wunused:privates",
  "-Wvalue-discard"
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) =>
    xs map {
      _.toLowerCase
    } match {
      case "aop.xml" :: Nil     => MergeStrategy.concat
      case "manifest.mf" :: Nil => MergeStrategy.discard
      case _                    => MergeStrategy.concat
    }
  case "application.conf" => MergeStrategy.concat
  case "reference.conf"   => MergeStrategy.concat
  case x                  => MergeStrategy.first
}

lazy val root = (project in file("."))
  .settings(
    name := "scala-weather",
    libraryDependencies ++= Dependencies.all,
    assembly / mainClass := Some("com.gkleczek.Main")
  )
