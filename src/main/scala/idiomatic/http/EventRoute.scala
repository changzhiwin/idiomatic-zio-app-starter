package idiomatic.http

import java.util.UUID

import zio._
import zio.http._
import zio.http.model.{Method}
import zio.json._

import idiomatic.db.table.Events

trait EventRoute {
  def routes: HttpApp[Any, Throwable]
}

object EventRoute {
  def routes = Http.fromZIO( ZIO.serviceWith[EventRoute](_.routes) ).flatten
}

case class EventRouteLive(events: Events) extends EventRoute {

  override val routes: HttpApp[Any, Throwable] = Http.collectZIO[Request] {

    case Method.GET -> !! / "events" / id =>
      events.get(UUID.fromString(id)).map { event =>
        Response.json(event.toJson)
      }

    case Method.GET -> !! / "events" =>
      events.all.map { events =>
        Response.json(events.toJson)
      }

    case req @ Method.POST -> !! / "events" =>
      for {
        body        <- req.body.asString
        createEvent <- ZIO.fromEither(body.fromJson[CreateEvent].left.map(new Error(_)))
        event       <- events.create(createEvent.ownerId, createEvent.name)
      } yield Response.json(event.toJson)
  }
}

object EventRouteLive {
  val layer = ZLayer.fromFunction(EventRouteLive.apply _)
}

final case class CreateEvent(ownerId: UUID, name: String)

object CreateEvent {
  implicit val codec: JsonCodec[CreateEvent] = DeriveJsonCodec.gen
}