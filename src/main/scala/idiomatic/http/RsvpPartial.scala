package idiomatic.http

import java.util.UUID

import zio._
import zio.http._
import zio.http.model.{Method}
import zio.json._

import idiomatic.db.table.Rsvps

trait RsvpPartial {
  def routes: PartialFunction[Request, Task[Response]]
}

object RsvpPartial {
  def routes: ZIO[RsvpPartial, Nothing, PartialFunction[Request, Task[Response]]] = 
    ZIO.serviceWith[RsvpPartial](_.routes)
}

case class RsvpPartialLive(rsvps: Rsvps) extends RsvpPartial {

  override val routes: PartialFunction[Request, Task[Response]] = {

    case Method.GET -> !! / "rsvps" / eventId =>
      for {
        rsvps <- rsvps.allForEvent(UUID.fromString(eventId))
      } yield Response.json(rsvps.toJson)

    case req @ Method.POST -> !! / "rsvps" =>
      for {
        body       <- req.body.asString
        createRsvp <- ZIO.fromEither(body.fromJson[CreateRsvp].left.map(new Error(_)))
        rsvp       <- rsvps.create(eventId = createRsvp.eventId, userId = createRsvp.userId)
      } yield Response.json(rsvp.toJson)
  }
}

object RsvpPartialLive {
  val layer = ZLayer.fromFunction(RsvpPartialLive.apply _)
}

final case class CreateRsvp(userId: UUID, eventId: UUID)

object CreateRsvp {
  implicit val codec: JsonCodec[CreateRsvp] = DeriveJsonCodec.gen
}