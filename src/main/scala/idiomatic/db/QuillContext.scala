package idiomatic.db

import io.getquill.jdbczio.Quill
import io.getquill._
//import io.getquill.util.LoadConfig

object QuillContext extends SqliteZioJdbcContext(SnakeCase) {

  val dataSourceLayer = Quill.DataSource.fromPrefix("database").orDie
}