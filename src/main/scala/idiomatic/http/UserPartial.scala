package idiomatic.http

import java.util.UUID

import zio._
import zio.http._
import zio.http.model.{Method}
import zio.json._

import idiomatic.db.table.Users

trait UserPartial {
  def routes: PartialFunction[Request, Task[Response]]
}

object UserPartial {
  def routes = ZIO.serviceWith[UserPartial](_.routes)
}

case class UserPartialLive(users: Users) extends UserPartial {

  override val routes: PartialFunction[Request, Task[Response]] = {

    case Method.GET -> !! / "users" / id =>
      users.get(UUID.fromString(id)).map { user =>
        Response.json(user.toJson)
      }

    case Method.GET -> !! / "users" =>
      users.all.map { users =>
        Response.json(users.toJson)
      }

    case req @ Method.POST -> !! / "users" =>
      for {
        body       <- req.body.asString
        createUser <- ZIO.fromEither(body.fromJson[CreateUser0].left.map(new Error(_)))
        user       <- users.create(createUser.email)
      } yield Response.json(user.toJson)
  }
}

object UserPartialLive {
  val layer = ZLayer.fromFunction(UserPartialLive.apply _)
}

// AIP Models, only process input value

final case class CreateUser0(email: String)

object CreateUser0 {
  implicit val codec: JsonCodec[CreateUser0] = DeriveJsonCodec.gen
}