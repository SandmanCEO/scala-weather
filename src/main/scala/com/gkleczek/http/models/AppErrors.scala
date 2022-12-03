package com.gkleczek.http.models

import io.circe.DecodingFailure

object AppErrors {

  sealed trait AppError

  final case class ImageLoadingError(msg: String) extends AppError

  final case class JsonParsingError(err: DecodingFailure) extends AppError

}
