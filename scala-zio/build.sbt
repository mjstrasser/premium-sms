ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.0"

val zioVersion = "2.1.14"

lazy val root = (project in file("."))
  .settings(
    name := "scala-zio",
    idePackagePrefix := Some("mjs.premsms")
  )

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-test" % zioVersion % Test
)
