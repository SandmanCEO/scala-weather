package com.gkleczek.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal

import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class ImageProvider()(implicit
    system: ActorSystem,
    ec: ExecutionContext,
    logger: LoggingAdapter
) {

  def loadImage(imageUri: String): Future[Array[Byte]] = {
    fetchImage(imageUri)
  }

  private def fetchImage(imageUri: String): Future[Array[Byte]] = {
    val request = HttpRequest(uri = Uri(imageUri))
    logger.info("Fetching image {}", imageUri)
    Http()
      .singleRequest(request)
      .flatMap(response => Unmarshal(response.entity).to[Array[Byte]])
  }

  def loadImageFromIndex(index: Int): Array[Byte] = Try {
    val url = getClass.getResource(s"/img_$index.png")
    val bufferedImage = ImageIO.read(url)
    val output = new ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "png", output)
    output.toByteArray
  }.recoverWith { ex =>
    logger.error(ex, "Exception while loading image from resources")
    Failure(ex)
  }.get
}
