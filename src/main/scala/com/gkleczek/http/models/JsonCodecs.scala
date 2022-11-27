package com.gkleczek.http.models

import com.gkleczek.http.models.ApiResponses._

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, LocalTime, ZoneId}
import scala.io.Codec
import scala.util.Try

trait JsonCodecs {

  lazy implicit val localDateTimeCodec: Codec[LocalDateTime] =
    new Codec[LocalDateTime] {
      override def apply(c: HCursor): Result[LocalDateTime] =
        for {
          dateTimeString <- c.as[String]
        } yield LocalDateTime.parse(dateTimeString)

      override def apply(a: LocalDateTime): Json = a.toString.asJson
    }

  lazy implicit val eitherLocalTimeFormat: Codec[Either[String, LocalTime]] =
    new Codec[Either[String, LocalTime]] {
      override def apply(c: HCursor): Result[Either[String, LocalTime]] = {
        val formatter = DateTimeFormatter.ofPattern("h:m a")
        for {
          localTime <- c.as[String]
        } yield Try(LocalTime.parse(localTime, formatter)).toEither
          .fold(_ => localTime, identity)
      }

      override def apply(a: Either[String, LocalTime]): Json = {
        a.fold(_.asJson, _.toString.asJson)
      }
    }

  lazy implicit val weatherConditionCodec: Codec[WeatherCondition] =
    new Codec[WeatherCondition] {
      override def apply(c: HCursor): Result[WeatherCondition] =
        for {
          condition <- c.downField("text").as[String]
          icon <- c.downField("icon").as[String]
        } yield WeatherCondition(condition, s"http:$icon")

      override def apply(a: WeatherCondition): Json = Json.obj(
        "text" -> a.condition.asJson,
        "icon" -> a.icon.asJson
      )
    }

  lazy implicit val airQualityCodec: Codec[AirQuality] = new Codec[AirQuality] {
    override def apply(c: HCursor): Result[AirQuality] =
      for {
        carbonDioxide <- c.downField("co").as[Double]
        nitrousDioxide <- c.downField("no2").as[Double]
        ozone <- c.downField("o3").as[Double]
        sulphurDioxide <- c.downField("so2").as[Double]
        pm25 <- c.downField("pm2_5").as[Double]
        pm10 <- c.downField("pm10").as[Double]
        index <- c.downField("us-epa-index").as[Int]
      } yield AirQuality(
        carbonOxide = Math.round(carbonDioxide * 10.0) / 10.0,
        nitrousDioxide = Math.round(nitrousDioxide * 10.0) / 10.0,
        ozone = Math.round(ozone * 10.0) / 10.0,
        sulphurDioxide = Math.round(sulphurDioxide * 10.0) / 10.0,
        pm25 = Math.round(pm25 * 10.0) / 10.0,
        pm10 = Math.round(pm10 * 10.0) / 10.0,
        index = index
      )

    override def apply(a: AirQuality): Json = Json.obj()
  }

  lazy implicit val weatherLocationCodec: Codec[WeatherLocation] =
    new Codec[WeatherLocation] {
      override def apply(c: HCursor): Result[WeatherLocation] =
        for {
          name <- c.downField("name").as[String]
          country <- c.downField("country").as[String]
        } yield WeatherLocation(name, country)

      override def apply(a: WeatherLocation): Json = Json.obj()
    }

  lazy implicit val currentWeatherCodec: Codec[CurrentWeather] =
    new Codec[CurrentWeather] {
      override def apply(c: HCursor): Result[CurrentWeather] =
        for {
          lastUpdatedEpoch <- c.downField("last_updated_epoch").as[Long]
          lastUpdatedInstant = Instant.ofEpochSecond(lastUpdatedEpoch)
          lastUpdatedLocalDate = LocalDateTime.ofInstant(
            lastUpdatedInstant,
            ZoneId.of("Europe/Warsaw")
          )
          temperature <- c.downField("temp_c").as[Double]
          feelsLikeTemperature <- c.downField("feelslike_c").as[Double]
          windSpeed <- c.downField("wind_kph").as[Double]
          pressure <- c.downField("pressure_mb").as[Double]
          humidity <- c.downField("humidity").as[Int]
          condition <- c.downField("condition").as[WeatherCondition]
          airQuality <- c.downField("air_quality").as[AirQuality]
        } yield CurrentWeather(
          lastUpdatedLocalDate,
          temperature,
          feelsLikeTemperature,
          windSpeed,
          pressure,
          humidity,
          condition,
          airQuality
        )

      override def apply(a: CurrentWeather): Json = Json.obj()
    }

  lazy implicit val weatherResponseCodec: Codec[WeatherResponse] =
    new Codec[WeatherResponse] {
      override def apply(c: HCursor): Result[WeatherResponse] =
        for {
          location <- c.downField("location").as[WeatherLocation]
          current <- c.downField("current").as[CurrentWeather]
        } yield WeatherResponse(location, current)

      override def apply(a: WeatherResponse): Json = Json.obj()
    }

  lazy implicit val astronomyDataCodec: Codec[AstronomyData] =
    new Codec[AstronomyData] {
      override def apply(c: HCursor): Result[AstronomyData] = {
        val astronomyCursor = c.downField("astro")
        for {
          sunrise <- astronomyCursor
            .downField("sunrise")
            .as[Either[String, LocalTime]]
          sunset <- astronomyCursor
            .downField("sunset")
            .as[Either[String, LocalTime]]
          moonrise <- astronomyCursor
            .downField("moonrise")
            .as[Either[String, LocalTime]]
          moonSet <- astronomyCursor
            .downField("moonset")
            .as[Either[String, LocalTime]]
        } yield AstronomyData(sunrise, sunset, moonrise, moonSet)
      }

      override def apply(a: AstronomyData): Json = Json.obj()
    }

  lazy implicit val astronomyResponseCodec: Codec[AstronomyResponse] =
    new Codec[AstronomyResponse] {
      override def apply(c: HCursor): Result[AstronomyResponse] =
        for {
          location <- c.downField("location").as[WeatherLocation]
          astronomy <- c.downField("astronomy").as[AstronomyData]
        } yield AstronomyResponse(location, astronomy)

      override def apply(a: AstronomyResponse): Json = Json.obj()
    }
}
