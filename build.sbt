ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val zioVersion = "2.0.2"

lazy val root = (project in file("."))
  .settings(
    name := "idiomatic-starter",
    libraryDependencies ++= Seq(
      "dev.zio"       %% "zio"               % zioVersion,
      "dev.zio"       %% "zio-test"          % zioVersion % Test,
      "dev.zio"       %% "zio-test-sbt"      % zioVersion % Test,
      "dev.zio"       %% "zio-test-magnolia" % zioVersion % Test,

      "dev.zio"       %% "zio-json"       % "0.3.0",
      //"io.d11"        %% "zhttp"          % "xx",
      "io.getquill"   %% "quill-jdbc-zio" % "4.6.0",
      "org.xerial"    % "sqlite-jdbc" % "3.28.0",
      //"mysql" % "mysql-connector-java" % "8.0.17",
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
