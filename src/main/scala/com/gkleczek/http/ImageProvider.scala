package com.gkleczek.http

import cats.effect.{ContextShift, IO}
import com.github.benmanes.caffeine.cache.{Cache, Caffeine}
import org.http4s.Uri
import org.http4s.client.blaze.BlazeClientBuilder
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scalacache.caffeine.CaffeineCache
import scalacache.memoization.memoizeF
import scalacache.{CacheConfig, Entry, Mode}

import java.io.ByteArrayOutputStream
import java.time.Duration
import javax.imageio.ImageIO
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class ImageProvider()(implicit
    cs: ContextShift[IO]
) {

  private val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private val clientResource =
    BlazeClientBuilder[IO](ExecutionContext.global).resource

  private val underlyingCache: Cache[String, Entry[Array[Byte]]] =
    Caffeine
      .newBuilder()
      .expireAfterWrite(Duration.ofHours(24))
      .build()

  implicit val mode: Mode[IO] = scalacache.CatsEffect.modes.async

  private implicit val cache: CaffeineCache[Array[Byte]] =
    CaffeineCache(underlyingCache)(CacheConfig.defaultCacheConfig)

  def loadImage(imageUri: String): IO[Array[Byte]] = {
    memoizeF[IO, Array[Byte]](Some(1.day)) {
      fetchImage(imageUri)
    }
  }

  private def fetchImage(imageUri: String): IO[Array[Byte]] =
    for {
      _ <- logger.info(s"Fetching image $imageUri")
      image <- clientResource.use { client =>
        val uri = Uri.unsafeFromString(imageUri)
        client.expect[Array[Byte]](uri)
      }
    } yield image

  def loadImageFromIndex(index: Int): IO[Array[Byte]] = {
    IO.pure {
      val url = getClass.getResource(s"/img_$index.png")
      val bufferedImage = ImageIO.read(url)
      val output = new ByteArrayOutputStream()
      ImageIO.write(bufferedImage, "png", output)
      output.toByteArray
    }.handleErrorWith { ex =>
      logger
        .error(ex)("Exception while loading image from resources")
        .flatMap(_ => IO.raiseError(ex))
    }
  }
}
