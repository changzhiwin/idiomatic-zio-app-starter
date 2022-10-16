package idiomatic

import java.nio.file.{Path, Paths}

import zio._
import zio.http._

import idiomatic.db.QuillContext
import idiomatic.db.table.{Users, UsersLive, Events, EventsLive, Rsvps, RsvpsLive, NotificationsLive}
import idiomatic.service.common._
import idiomatic.http._

object MainApp extends ZIOAppDefault {

  val httpApp = for {
    user <- UserPartial.routes
    rsvp <- RsvpPartial.routes

    // Tow style of define routes
    http = Http.collectZIO[Request] { user.orElse(rsvp) } ++ EventRoute.routes
    _    <- Server.serve(http)
  } yield ()

  override def run = httpApp.provide(
    Server.default,

    QuillContext.dataSourceLayer,
    UsersLive.layer,
    EventsLive.layer,
    RsvpsLive.layer,

    NotificationsLive.layer,
    AnalyticsLive.layer,
    EmailServiceLive.layer,

    FileLogger.layer(Paths.get("/Users/changzhi/temp/start.log")),

    RsvpPartialLive.layer,
    UserPartialLive.layer,
    EventRouteLive.layer,
  )
  //.exitCode // Not use this, will drop errors
}