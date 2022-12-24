import sbtghpackages.GitHubPackagesPlugin.autoImport.githubRepository

val libVersion = "0.1.0"
val scala212Version = "2.12.14"
val scala213Version = "2.13.6"

lazy val commonSettings = Seq(
  organization := "com.nbasnet",
  version := libVersion,
  scalaVersion := scala212Version,
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"
  ),
  scalafmtOnCompile := true,
  // Github package publish info
  githubRepository := "scala-xmlparser",
  githubOwner := "nischalbasnet",
  githubTokenSource := TokenSource.Environment("GITHUB_TOKEN"),
  libraryDependencies ++= Seq(
    // Test dependencies
    "org.scalatest" %% "scalatest" % "3.2.0" % "test"
  )
)

lazy val root = (project in file("."))
  .aggregate(`n-xmlparser`)
  .settings(
    publish := {}
  )

lazy val `n-xmlparser` = (project in file("n-xmlparser"))
  .settings(commonSettings)
  .settings(
    name := "n-xmlparser",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
    )
  )

lazy val `n-xmlparser-macro` = (project in file("n-xmlparser-macro"))
  .dependsOn(`n-xmlparser`)
  .settings(commonSettings)
  .settings(
    name := "n-xmlparser-macro",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
    )
  )

lazy val `parser-test` = (project in file("parser-test"))
  .dependsOn(`n-xmlparser`, `n-xmlparser-macro`)
  .settings(commonSettings)
  .settings(
    name := "parser-test",
    publish := {}
  )
