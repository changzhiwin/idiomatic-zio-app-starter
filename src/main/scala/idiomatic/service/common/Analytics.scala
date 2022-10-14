package idiomatic.service.common

import zio._
import idiomatic.service.common.Logger

trait Analytics {
  def emit(name: String, value: String): UIO[Unit]
}

final case class AnalyticsLive(logger: Logger) extends Analytics {
  override def emit(name: String, value: String): UIO[Unit] =
    logger.log(s"Emitting Analytics: $name: $value")
}

object AnalyticsLive {
  val layer = ZLayer.fromFunction(AnalyticsLive.apply _)
}
