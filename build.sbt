ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "1.0.0"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

maintainer := "changzhiwin@gmail.com"

val zioVersion = "2.0.3"
val zioLoggingVersion = "2.1.3"
//val zioHttpVersion = "2.0.0-RC11+119-0cd216c3+20221024-1727-SNAPSHOT" // test ok
val zioHttpVersion = "2.0.0-RC11+159-5abadb95+20221108-1602-SNAPSHOT" //"2.0.0-RC11+50-7870fdce+20220919-2120-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging)
  .settings(
    name := "idiomatic-starter",
    libraryDependencies ++= Seq(
      "dev.zio"       %% "zio"               % zioVersion,
      "dev.zio"       %% "zio-test"          % zioVersion % Test,
      "dev.zio"       %% "zio-test-sbt"      % zioVersion % Test,
      "dev.zio"       %% "zio-test-magnolia" % zioVersion % Test,

      "dev.zio"       %% "zio-logging"       % zioLoggingVersion,
      //"dev.zio"       %% "zio-logging-slf4j" % zioLoggingVersion,
      //"org.slf4j"     % "slf4j-reload4j"       % "2.0.3",

      "dev.zio"       %% "zio-json"       % "0.3.0",
      "dev.zio"       %% "zio-http"       % zioHttpVersion,
      "io.getquill"   %% "quill-jdbc-zio" % "4.6.0",
      "org.xerial"    % "sqlite-jdbc" % "3.28.0",
      //"mysql" % "mysql-connector-java" % "8.0.17",

      "com.typesafe" % "config" % "1.4.2",
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

