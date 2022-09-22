package com.gkleczek.http.models

import com.gkleczek.http.models.ApiResponses._
import spray.json.{
  DefaultJsonProtocol,
  DeserializationException,
  JsString,
  JsValue,
  RootJsonFormat
}

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, LocalTime, ZoneId}
import scala.util.Try

trait JsonSupport extends DefaultJsonProtocol {

  lazy implicit val localDateTimeFormat: RootJsonFormat[LocalDateTime] =
    new RootJsonFormat[LocalDateTime] {
      override def write(obj: LocalDateTime): JsValue =
        JsString(obj.format(DateTimeFormatter.ISO_DATE_TIME))

      override def read(json: JsValue): LocalDateTime = {
        json match {
          case JsString(dateTime) => LocalDateTime.parse(dateTime)
          case other =>
            throw DeserializationException(
              s"Expected date time as String but got $other"
            )
        }
      }
    }

  lazy implicit val eitherLocalTimeFormat
      : RootJsonFormat[Either[String, LocalTime]] =
    new RootJsonFormat[Either[String, LocalTime]] {
      override def write(obj: Either[String, LocalTime]): JsValue = JsString(
        obj.toString
      )

      override def read(json: JsValue): Either[String, LocalTime] = {
        val formatter = DateTimeFormatter.ofPattern("h:m a")
        json match {
          case JsString(time) =>
            Try(Right(LocalTime.parse(time, formatter))).getOrElse(Left(time))
          case other =>
            throw DeserializationException(s"Expected LocalTime but got $other")
        }

      }
    }

  lazy implicit val weatherConditionFormat: RootJsonFormat[WeatherCondition] =
    new RootJsonFormat[WeatherCondition] {
      override def write(obj: WeatherCondition): JsValue =
        jsonFormat2(WeatherCondition).write(obj)

      override def read(json: JsValue): WeatherCondition = {
        val fields = json.asJsObject.fields

        val condition = fields("text").convertTo[String]
        val icon = fields("icon").convertTo[String]
        WeatherCondition(condition, s"http:$icon")
      }
    }

  lazy implicit val airQualityFormat: RootJsonFormat[AirQuality] =
    new RootJsonFormat[AirQuality] {
      override def write(obj: AirQuality): JsValue =
        jsonFormat7(AirQuality).write(obj)

      override def read(json: JsValue): AirQuality = {
        val fields = json.asJsObject.fields

        val carbonOxide =
          Math.round(fields("co").convertTo[Double] * 10.0) / 10.0
        val nitrousDioxide =
          Math.round(fields("no2").convertTo[Double] * 10.0) / 10.0
        val ozone = Math.round(fields("o3").convertTo[Double] * 10.0) / 10.0
        val sulphurDioxide =
          Math.round(fields("so2").convertTo[Double] * 10.0) / 10.0
        val pm25 = Math.round(fields("pm2_5").convertTo[Double] * 10.0) / 10.0
        val pm10 = Math.round(fields("pm10").convertTo[Double] * 10.0) / 10.0
        val index = fields("us-epa-index").convertTo[Int]
        AirQuality(
          carbonOxide,
          nitrousDioxide,
          ozone,
          sulphurDioxide,
          pm25,
          pm10,
          index
        )
      }
    }

  lazy implicit val currentWeatherFormat: RootJsonFormat[CurrentWeather] =
    new RootJsonFormat[CurrentWeather] {
      override def write(obj: CurrentWeather): JsValue =
        jsonFormat8(CurrentWeather).write(obj)

      override def read(json: JsValue): CurrentWeather = {
        val fields = json.asJsObject.fields

        val lastUpdatedEpoch = fields("last_updated_epoch").convertTo[Long]
        val lastUpdatedInstant = Instant.ofEpochSecond(lastUpdatedEpoch)
        val lastUpdatedLocalDate = LocalDateTime.ofInstant(
          lastUpdatedInstant,
          ZoneId.of("Europe/Warsaw")
        )
        val temperature = fields("temp_c").convertTo[Double]
        val feelsLikeTemperature = fields("feelslike_c").convertTo[Double]
        val windSpeed = fields("wind_kph").convertTo[Double]
        val pressure = fields("pressure_mb").convertTo[Double]
        val humidity = fields("humidity").convertTo[Int]
        val condition = fields("condition").convertTo[WeatherCondition]
        val airQuality = fields("air_quality").convertTo[AirQuality]
        CurrentWeather(
          lastUpdatedLocalDate,
          temperature,
          feelsLikeTemperature,
          windSpeed,
          pressure,
          humidity,
          condition,
          airQuality
        )
      }
    }

  lazy implicit val weatherLocationFormat: RootJsonFormat[WeatherLocation] =
    new RootJsonFormat[WeatherLocation] {
      override def write(obj: WeatherLocation): JsValue =
        jsonFormat2(WeatherLocation).write(obj)

      override def read(json: JsValue): WeatherLocation = {
        val fields = json.asJsObject.fields

        val name = fields("name").convertTo[String]
        val country = fields("country").convertTo[String]

        WeatherLocation(name, country)
      }
    }

  lazy implicit val weatherResponseFormat: RootJsonFormat[WeatherResponse] =
    new RootJsonFormat[WeatherResponse] {
      override def write(obj: WeatherResponse): JsValue =
        jsonFormat2(WeatherResponse).write(obj)

      override def read(json: JsValue): WeatherResponse = {
        val fields = json.asJsObject.fields

        val location = fields("location").convertTo[WeatherLocation]
        val weather = fields("current").convertTo[CurrentWeather]

        WeatherResponse(location, weather)
      }
    }

  lazy implicit val astronomyDataFormat: RootJsonFormat[AstronomyData] =
    new RootJsonFormat[AstronomyData] {
      override def write(obj: AstronomyData): JsValue =
        jsonFormat4(AstronomyData).write(obj)

      override def read(json: JsValue): AstronomyData = {
        val fields = json.asJsObject.fields("astro").asJsObject.fields

        val sunrise = fields("sunrise").convertTo[Either[String, LocalTime]]
        val sunset = fields("sunset").convertTo[Either[String, LocalTime]]
        val moonrise = fields("moonrise").convertTo[Either[String, LocalTime]]
        val moonSet = fields("moonset").convertTo[Either[String, LocalTime]]

        AstronomyData(sunrise, sunset, moonrise, moonSet)
      }
    }

  lazy implicit val astronomyResponseFormat: RootJsonFormat[AstronomyResponse] =
    new RootJsonFormat[AstronomyResponse] {
      override def write(obj: AstronomyResponse): JsValue =
        jsonFormat2(AstronomyResponse).write(obj)
      override def read(json: JsValue): AstronomyResponse = {
        val fields = json.asJsObject.fields

        val location = fields("location").convertTo[WeatherLocation]
        val astro = fields("astronomy").convertTo[AstronomyData]

        AstronomyResponse(location, astro)
      }

    }

}
