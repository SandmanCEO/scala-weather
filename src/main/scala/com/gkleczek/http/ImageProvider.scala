package com.gkleczek.http

import cats.data.EitherT
import cats.effect.{ContextShift, IO}
import com.github.benmanes.caffeine.cache.{Cache, Caffeine}
import com.gkleczek.http.models.AppErrors.{AppError, ImageLoadingError}
import org.http4s.Uri
import org.http4s.client.blaze.BlazeClientBuilder
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scalacache.caffeine.CaffeineCache
import scalacache.{CacheConfig, Entry, Mode, cachingF}

import java.io.ByteArrayOutputStream
import java.time.Duration
import javax.imageio.ImageIO
import scala.concurrent.ExecutionContext

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

  def loadImage(imageUri: String): EitherT[IO, AppError, Array[Byte]] = {
    val result = cachingF(imageUri)(None)(fetchImage(imageUri))
    EitherT.right(result)
  }

  def loadImageFromIndex(index: Int): EitherT[IO, AppError, Array[Byte]] = {
    val result: IO[Either[AppError, Array[Byte]]] = IO {
      val url           = getClass.getResource(s"/img_$index.png")
      val bufferedImage = ImageIO.read(url)
      val output        = new ByteArrayOutputStream()
      ImageIO.write(bufferedImage, "png", output)
      Right(output.toByteArray)
    }.handleErrorWith { ex =>
      for {
        _    <- logger.error(ex)("Exception while loading image from resources")
        error = ImageLoadingError(ex.getMessage)
      } yield Left(error)
    }

    EitherT(result)
  }

  private def fetchImage(imageUri: String): IO[Array[Byte]] =
    for {
      _     <- logger.info(s"Fetching image $imageUri")
      image <- clientResource.use { client =>
                 val uri = Uri.unsafeFromString(imageUri)
                 client.expect[Array[Byte]](uri)
               }
    } yield image
}
