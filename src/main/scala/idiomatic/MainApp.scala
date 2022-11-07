package idiomatic

import java.nio.file.{Path, Paths}

import com.typesafe.config.Config

import zio._
import zio.http._

import idiomatic.db.QuillContext
import idiomatic.db.table.{Users, UsersLive, Events, EventsLive, Rsvps, RsvpsLive}
import idiomatic.service.common._
import idiomatic.service.business.{NotificationsLive}
import idiomatic.http._

object MainApp extends ZIOAppDefault {

  val httpApp = for {
    // Just compare two different config file
    config <- ZIO.service[Config]
    _    <- Console.printLine(s"app.upstream.type = ${config.getString("app.upstream.type")}")
    _    <- Console.printLine(s"app.upstream.url = ${config.getString("app.upstream.url")}")

    user <- UserPartial.routes
    rsvp <- RsvpPartial.routes

    // Tow style of define routes
    http = Http.collectZIO[Request] { user.orElse(rsvp) } ++ EventRoute.routes
    _    <- Server.serve(http)
  } yield ()

  override def run = httpApp.provide(
    AppConfig.layer,
    configurableLayer,

    FileLogger.live,
    Server.live,
    QuillContext.dataSourceLayer,
    UsersLive.layer,
    EventsLive.layer,
    RsvpsLive.layer,

    NotificationsLive.layer,
    AnalyticsLive.layer,
    EmailServiceLive.layer,

    RsvpPartialLive.layer,
    UserPartialLive.layer,
    EventRouteLive.layer,
  )
  //.exitCode // Not use this, will drop errors

  val configurableLayer: ZLayer[Config, Throwable, ServerConfig with FileLoggerConfig] = ZLayer.fromZIOEnvironment {
    for {
      config    <- ZIO.service[Config]

      logCfg    = FileLoggerConfig(Paths.get(config.getString("app.logFile")))
      serverCfg = ServerConfig.default.port(config.getInt("app.bindPort"))
    } yield ZEnvironment[ServerConfig, FileLoggerConfig](serverCfg, logCfg)
  }
}