package idiomatic.demo

import java.util.UUID
import java.nio.file.{Path, Paths}
import java.time.format.DateTimeFormatter

import zio._
import zio.logging.{LogAnnotation, LogFormat, console, fileAsync}

object HowToLog extends ZIOAppDefault {

  import LogFormat._

  private val users = List.fill(3)(UUID.randomUUID())

  val basic = for {
    traceId <- ZIO.succeed(UUID.randomUUID())
    _       <- ZIO.foreachDiscard(users) { uId =>
      ZIO.logSpan("cost") {
        ZIO.logInfo("Starting operation") *>
          ZIO.sleep(500.millis) *>
          ZIO.logInfo("Stopping operation")
      } @@ userLogAnnotation(uId)
    } @@ LogAnnotation.TraceId(traceId)
    _       <- ZIO.logInfo("Done")
  } yield ExitCode.success

  private val userLogAnnotation = LogAnnotation[UUID]("user", (_, i) => i, _.toString)

  private val formater = 
    bracketStart |-| 
      timestamp(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).fixed(19) |-| 
      level  |-| 
    bracketEnd |-|
    line |-|
    annotation(userLogAnnotation) |-| annotation(LogAnnotation.TraceId) |-|
    spans |-| 
    cause

  override val bootstrap = Runtime.removeDefaultLoggers >>> fileAsync(
    destination = Paths.get("./logs/demo.log"),
    format = formater,
    logLevel = LogLevel.Info
  )

  def run = basic

}