ThisBuild / organization := "io.github.marcinzh"
ThisBuild / version := "0.4.0"
ThisBuild / scalaVersion := "3.3.3"
ThisBuild / crossScalaVersions := Seq(scalaVersion.value)

ThisBuild / watchBeforeCommand := Watch.clearScreen
ThisBuild / watchTriggeredMessage := Watch.clearScreenOnTrigger
ThisBuild / watchForceTriggerOnAnyChange := true

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Wnonunit-statement",
  "-Xfatal-warnings",
  "-Ykind-projector:underscores",
)

val Deps = {
  object deps {
    val turbolift = "io.github.marcinzh" %% "turbolift-core" % "0.82.0"
    val sourcecode = "com.lihaoyi" %% "sourcecode" % "0.3.1"
    val yamlist = "io.github.marcinzh" %% "yamlist" % "0.2.0"
  }
  deps
}

lazy val root = project
  .in(file("."))
  .settings(sourcesInBase := false)
  .settings(dontPublishMe: _*)
  .aggregate(core, demos)

lazy val core = project
  .in(file("modules/core"))
  .settings(name := "daae-core")
  .settings(libraryDependencies ++= Seq(
    Deps.turbolift,
    Deps.sourcecode,
  ))

lazy val demos = project
  .in(file("modules/demos"))
  .settings(name := "daae-demos")
  .settings(dontPublishMe: _*)
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq(
    Deps.yamlist,
  ))

//=================================================

lazy val dontPublishMe = Seq(
  publishTo := None,
  publish := (()),
  publishLocal := (()),
  publishArtifact := false
)

ThisBuild / description := "Debug as an Effect (DaaE)"
ThisBuild / organizationName := "marcinzh"
ThisBuild / homepage := Some(url("https://github.com/marcinzh/daae"))
ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/marcinzh/daae"), "scm:git@github.com:marcinzh/daae.git"))
ThisBuild / licenses := List("MIT" -> new URL("http://www.opensource.org/licenses/MIT"))
ThisBuild / versionScheme := Some("semver-spec")
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishMavenStyle := true
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  isSnapshot.value match {
    case true => Some("snapshots" at nexus + "content/repositories/snapshots")
    case false => Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
}
ThisBuild / pomExtra := (
  <developers>
    <developer>
      <id>marcinzh</id>
      <name>Marcin Żebrowski</name>
      <url>https://github.com/marcinzh</url>
    </developer>
  </developers>
)
