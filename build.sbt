import sbt._
import Keys._

val scioVersion = "0.7.0-beta1"
val beamVersion = "2.8.0"
val scalaMacrosVersion = "2.1.1"

lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "recommender",
  // Semantic versioning http://semver.org/
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.8",
  scalacOptions ++= Seq(
    "-target:jvm-1.8",
    "-deprecation",
    "-feature",
    "-unchecked"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  updateOptions := updateOptions.value.withLatestSnapshots(false),
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  libraryDependencies ++= Seq(
    "org.platanios" %% "tensorflow" % "0.4.2-SNAPSHOT" classifier "linux-cpu-x86_64",
  )
)

lazy val paradiseDependency = "org.scalamacros" % "paradise" % scalaMacrosVersion cross CrossVersion.full

lazy val macroSettings = Seq(
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  addCompilerPlugin(paradiseDependency)
)

lazy val root: Project = project
  .in(file("."))
  .settings(commonSettings)
  .settings(macroSettings)
  .settings(
    name := "elo-recommender",
    description := "Elo category recommender",
    publish / skip := true,
    libraryDependencies ++= Seq(
      "com.spotify" %% "scio-core" % scioVersion,
      "com.spotify" %% "scio-tensorflow" % scioVersion,
      "com.spotify" %% "scio-test" % scioVersion % Test,
      "org.apache.beam" % "beam-runners-direct-java" % beamVersion,
      // optional dataflow runner
      "org.apache.beam" % "beam-runners-google-cloud-dataflow-java" % beamVersion,
      "org.slf4j" % "slf4j-simple" % "1.7.25"
    )
  )
  .enablePlugins(PackPlugin)

lazy val repl: Project = project
  .in(file(".repl"))
  .settings(commonSettings)
  .settings(macroSettings)
  .settings(
    name := "repl",
    description := "Scio REPL for recommender",
    libraryDependencies ++= Seq(
      "com.spotify" %% "scio-repl" % scioVersion,
    ),
    Compile / mainClass := Some("com.spotify.scio.repl.ScioShell"),
    publish / skip := true
  )
  .dependsOn(root)
