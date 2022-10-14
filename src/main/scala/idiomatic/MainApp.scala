package idiomatic

import java.nio.file.{Path, Paths}

import zio._

import idiomatic.db.QuillContext
import idiomatic.db.table.{Users, UsersLive, Events, EventsLive, Rsvps, RsvpsLive, NotificationsLive}
import idiomatic.service.common._

object MainApp extends ZIOAppDefault {

  def runApp = for {
    u1 <- Users.create("bebest@88.com")
    e1 <- Events.create(u1.id, "create")
    r1 <- Rsvps.create(e1.id, u1.id)
    _  <- Users.get(u1.id).debug("Get User: ")
    _  <- Events.all.debug("All Event: ")
    _  <- Rsvps.allForEvent(e1.id).debug(s"For Event($e1): ")
  } yield ()

  override def run = 
    runApp
      .provide(
        QuillContext.dataSourceLayer,
        UsersLive.layer,
        EventsLive.layer,
        RsvpsLive.layer,
        NotificationsLive.layer,
        AnalyticsLive.layer,
        EmailServiceLive.layer,
        FileLogger.layer(Paths.get("/Users/changzhi/temp/start.log")),
      )
      //.exitCode // Not use this, will drop errors
}