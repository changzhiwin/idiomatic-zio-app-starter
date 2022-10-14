package idiomatic.db.table

import java.util.UUID
import javax.sql.DataSource

import zio._

import idiomatic.db.QuillContext
import idiomatic.db.model.User
import idiomatic.service.common._

trait Users {
  def get(id: UUID): Task[Option[User]]
  def all: Task[List[User]]
  def create(email: String): Task[User]
}

object Users {
  def get(id: UUID)         = ZIO.serviceWithZIO[Users](_.get(id))
  def all                   = ZIO.serviceWithZIO[Users](_.all)
  def create(email: String) = ZIO.serviceWithZIO[Users](_.create(email))
}

final case class UsersLive(
  dataSource: DataSource
) extends Users {
  
  import QuillContext._

  override def get(id: UUID): Task[Option[User]] = 
    run(query[User].filter(_.id == lift(id)))
      .map(_.headOption)
      .provideEnvironment(ZEnvironment(dataSource))

  override def all: Task[List[User]] =
    run(query[User])
      .provideEnvironment(ZEnvironment(dataSource))
  
  override def create(email: String): Task[User] =
    for {
      id   <- Random.nextUUID
      user =  User(id, email)
      _    <- run(query[User].insertValue(lift(user)))
                .provideEnvironment(ZEnvironment(dataSource))
    } yield user
}

object UsersLive {

  val layer = ZLayer.fromFunction(UsersLive.apply _)
}


