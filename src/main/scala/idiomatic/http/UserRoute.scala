package idiomatic.http

import java.util.UUID

import zio._
import zio.http._
import zio.http.model.{Method}
import zio.json._

import idiomatic.db.table.Users

trait UserRoute {
  def routes: HttpApp[Any, Throwable]
}

object UserRoute {
  def routes = Http.fromZIO( ZIO.serviceWith[UserRoute](_.routes) ).flatten
}

case class UserRouteLive(users: Users) extends UserRoute {

  override val routes: HttpApp[Any, Throwable] = Http.collectZIO[Request] {

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
        createUser <- ZIO.fromEither(body.fromJson[CreateUser].left.map(new Error(_)))
        user       <- users.create(createUser.email)
      } yield Response.json(user.toJson)
  }
}

object UserRouteLive {
  val layer = ZLayer.fromFunction(UserRouteLive.apply _)
}

// AIP Models, only process input value

final case class CreateUser(email: String)

object CreateUser {
  implicit val codec: JsonCodec[CreateUser] = DeriveJsonCodec.gen
}