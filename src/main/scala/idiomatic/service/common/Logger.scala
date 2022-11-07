package idiomatic.service.common

import java.io.{BufferedWriter, FileWriter}
import java.nio.file.{Path, Paths}

import zio._
import zio.stream.ZStream

trait Logger {
  def log(msg: String): UIO[Unit]
}

final case class FileLoggerConfig(path: Path)

object FileLoggerConfig {
  val default: FileLoggerConfig = FileLoggerConfig(Paths.get("/Users/changzhi/temp/start.log"))

  val live: ZLayer[Any, Nothing, FileLoggerConfig] = ZLayer.succeed(FileLoggerConfig.default)

  def live(config: FileLoggerConfig): ZLayer[Any, Nothing, FileLoggerConfig] =
    ZLayer.succeed(config)
}

final case class FileLogger(queue: Queue[String]) extends Logger {
  override def log(msg: String): UIO[Unit] = queue.offer(msg).unit
}

object FileLogger {

  def default: ZLayer[Any, Throwable, FileLogger] = {
    FileLoggerConfig.live >>> live
  }

  def live: ZLayer[FileLoggerConfig, Throwable, FileLogger] = ZLayer.scoped {
    for {
      config     <- ZIO.service[FileLoggerConfig]
      queue      <- Queue.unbounded[String]
      fileWriter <- ZIO.fromAutoCloseable(
                      ZIO.attempt(
                        new BufferedWriter(new FileWriter(config.path.toFile, true))
                      )
                    )
      _          <- ZStream
                      .fromQueue(queue)
                      .foreach { s =>
                        for {
                          now    <- Clock.instant
                          message = s"$now - $s\n"
                          _      <- ZIO.attempt { fileWriter.write(message); fileWriter.flush() }
                        } yield ()
                      }
                      .fork
    } yield FileLogger(queue)
  }
}