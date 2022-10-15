package idiomatic.service.common

import java.io.{BufferedWriter, FileWriter}
import java.nio.file.Path

import zio._
import zio.stream.ZStream

trait Logger {
  def log(msg: String): UIO[Unit]
}

final case class FileLoggerConfig(path: Path)

final case class FileLogger(queue: Queue[String]) extends Logger {
  override def log(msg: String): UIO[Unit] = queue.offer(msg).unit
}

object FileLogger {

  //TODO: return ZIO?
  def make(config: FileLoggerConfig): ZIO[Scope, Throwable, FileLogger] = {
    for {
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

  def layer(path: Path): ZLayer[Any, Throwable, FileLogger] =
    ZLayer.scoped {
      make(FileLoggerConfig(path))
    }
}