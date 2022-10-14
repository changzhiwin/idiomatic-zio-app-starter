package idiomatic.db.table

import java.util.UUID
import javax.sql.DataSource

import zio._

import idiomatic.db.QuillContext
import idiomatic.db.model.Rsvp
import idiomatic.service.common._

trait Rsvps {
  def allForEvent(eventId: UUID): Task[List[Rsvp]]
  def create(eventId: UUID, userId: UUID): Task[Rsvp]
  def delete(eventId: UUID, userId: UUID): Task[Unit]
}

object Rsvps {
  def allForEvent(eventId: UUID)          = ZIO.serviceWithZIO[Rsvps](_.allForEvent(eventId))
  def create(eventId: UUID, userId: UUID) = ZIO.serviceWithZIO[Rsvps](_.create(eventId, userId))
  def delete(eventId: UUID, userId: UUID) = ZIO.serviceWithZIO[Rsvps](_.delete(eventId, userId))
}

final case class RsvpsLive(
  notifications: Notifications,
  dataSource: DataSource,
  analytics: Analytics,
  logger: Logger
) extends Rsvps {

  import QuillContext._

  def allForEvent(eventId: UUID): Task[List[Rsvp]] = for {
    _     <- logger.log(s"Fetching all RSVPs for event $eventId")
    rsvps <- run(query[Rsvp].filter(_.eventId == lift(eventId)))
               .provideEnvironment(ZEnvironment(dataSource))
  } yield rsvps

  def create(eventId: UUID, userId: UUID): Task[Rsvp] = for {
    _   <- logger.log(s"Creating RSVP for event $eventId with user $userId")
    rsvp = Rsvp(eventId, userId)
    _   <- run(query[Rsvp].insertValue(lift(rsvp)))
             .provideEnvironment(ZEnvironment(dataSource))
    _   <- notifications.notifyOwner(rsvp)
  } yield rsvp

  def delete(eventId: UUID, userId: UUID): Task[Unit] = for {
    _ <- logger.log(s"Deleting RSVP for event $eventId with user $userId")
    _ <- run(
           query[Rsvp]
             .filter(rsvp => rsvp.eventId == lift(eventId) && rsvp.userId == lift(userId))
             .delete
         ).provideEnvironment(ZEnvironment(dataSource))
  } yield ()
}

object RsvpsLive {

  val layer = ZLayer.fromFunction(RsvpsLive.apply _)
}

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
      _ <- emailService.send(
             s"${attendee.email} has RSVP'd for ${event.name}",
             owner.email
           )
    } yield ()
}

object NotificationsLive {
  val layer =
    ZLayer.fromFunction(NotificationsLive.apply _)
}