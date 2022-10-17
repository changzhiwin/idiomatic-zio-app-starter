
package idiomatic.service.business

import zio._

import idiomatic.db.model.Rsvp
import idiomatic.db.table._
import idiomatic.service.common._

trait Notifications {
  def notifyOwner(rsvp: Rsvp): Task[Unit]
}

final case class NotificationsLive(
  events: Events,
  users: Users,
  analytics: Analytics,
  emailService: EmailService
) extends Notifications {
  override def notifyOwner(rsvp: Rsvp): Task[Unit] =
    for {
      _        <- analytics.emit("Cool", "Beans")
      event    <- events.get(rsvp.eventId).someOrFailException
      attendee <- users.get(rsvp.userId).someOrFailException
      owner    <- users.get(event.ownerId).someOrFailException
      _        <- emailService.send(
                    s"${attendee.email} has RSVP'd for ${event.name}",
                    owner.email
                  )
    } yield ()
}

object NotificationsLive {
  val layer =
    ZLayer.fromFunction(NotificationsLive.apply _)
}