package idiomatic.demo

import java.nio.file.{Path, Paths}

import com.typesafe.config.Config

import zio._

import idiomatic.AppConfig

object DiffConfigSource extends ZIOAppDefault {

  val program = for {
    config <- ZIO.service[Config]
    _    <- Console.printLine(s"app.upstream.type = ${config.getString("app.upstream.type")}")
    _    <- Console.printLine(s"app.upstream.url = ${config.getString("app.upstream.url")}")
  } yield ()

  override def run = program.provide(
    AppConfig.layer
  )
  .exitCode
}