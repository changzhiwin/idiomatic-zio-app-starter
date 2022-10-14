package idiomatic.db.table

import java.util.UUID
import javax.sql.DataSource

import zio._

import idiomatic.db.QuillContext
import idiomatic.db.model.Event
import idiomatic.service.common._

trait Events {
  def get(id: UUID): Task[Option[Event]]
  def all: Task[List[Event]]
  def create(ownerId: UUID, name: String): Task[Event]
}

object Events {
  def get(id: UUID)                       = ZIO.serviceWithZIO[Events](_.get(id))
  def all                                 = ZIO.serviceWithZIO[Events](_.all)
  def create(ownerId: UUID, name: String) = ZIO.serviceWithZIO[Events](_.create(ownerId, name))
}

final case class EventsLive(
  dataSource: DataSource,
  analytics: Analytics,
  logger: Logger
) extends Events {

  import QuillContext._

  def get(id: UUID): Task[Option[Event]] = for {
    _      <- logger.log(s"Fetching event $id")
    event  <- run(query[Event].filter(_.id == lift(id)))
                .map(_.headOption)
                .provideEnvironment(ZEnvironment(dataSource))
  } yield event

  def all: Task[List[Event]] = for {
    _      <- logger.log("Fetching all events")
    events <- run(query[Event])
                .provideEnvironment(ZEnvironment(dataSource))
  } yield events

  def create(ownerId: UUID, name: String): Task[Event] = for {
    _     <- logger.log(s"Creating event $name with owner $ownerId")
    id    <- Random.nextUUID
    event = Event(id, ownerId, name)
    _     <- run(query[Event].insertValue(lift(event)))
              .provideEnvironment(ZEnvironment(dataSource))
    _     <- analytics.emit("EventCreated", id.toString)
  } yield event

}

object EventsLive {

  val layer = ZLayer.fromFunction(EventsLive.apply _)
}