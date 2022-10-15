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
    http = Http.collectZIO[Request] { user }
    _    <- Server.serve(http)
  } yield ()
    // UserRoute.routes ++ EventRoute.routes ++ RsvpRoute.routes

  override def run = httpApp  //Server.serve(http)
      .provide(
        Server.default,
        QuillContext.dataSourceLayer,
        UsersLive.layer,
        EventsLive.layer,
        RsvpsLive.layer,
        NotificationsLive.layer,
        AnalyticsLive.layer,
        EmailServiceLive.layer,
        FileLogger.layer(Paths.get("/Users/changzhi/temp/start.log")),
        UserRouteLive.layer,
        RsvpRouteLive.layer,
        EventRouteLive.layer,
        UserPartialLive.layer,
      )
      //.exitCode // Not use this, will drop errors

  /*
  def runAppOld = for {
    u1 <- Users.create("bebest@88.com")
    e1 <- Events.create(u1.id, "create")
    r1 <- Rsvps.create(e1.id, u1.id)
    _  <- Users.get(u1.id).debug("Get User: ")
    _  <- Events.all.debug("All Event: ")
    _  <- Rsvps.allForEvent(e1.id).debug(s"For Event($e1): ")
  } yield ()
  */
}