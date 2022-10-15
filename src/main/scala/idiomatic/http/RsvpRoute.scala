package idiomatic.http

import java.util.UUID

import zio._
import zio.http._
import zio.http.model.{Method}
import zio.json._

import idiomatic.db.table.Rsvps

trait RsvpRoute {
  def routes: HttpApp[Any, Throwable]
}

object RsvpRoute {
  def routes = Http.fromZIO( ZIO.serviceWith[RsvpRoute](_.routes) ).flatten
}

case class RsvpRouteLive(rsvps: Rsvps) extends RsvpRoute {

  override val routes: HttpApp[Any, Throwable] = Http.collectZIO[Request] {

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

object RsvpRouteLive {
  val layer = ZLayer.fromFunction(RsvpRouteLive.apply _)
}

final case class CreateRsvp(userId: UUID, eventId: UUID)

object CreateRsvp {
  implicit val codec: JsonCodec[CreateRsvp] = DeriveJsonCodec.gen
}