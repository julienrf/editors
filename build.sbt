val commonSettings = Seq(
  organization := "com.julienrf",
  scalacOptions ++= Seq("-feature", "-Xlint", "-unchecked"),
  scalaVersion := "2.10.3"
)

lazy val library = project.settings(commonSettings: _*).settings(
  name := "editors",
  version := "0.1-SNAPSHOT",
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % "2.2.1",
    "com.typesafe.play" %% "play-test" % "2.2.1" % "test",
    "org.specs2" %% "specs2" % "2.3.4" % "test"
  ),
  resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

lazy val sampleBacklog = Project("sample-backlog", file("samples/backlog")).settings(commonSettings: _*).settings(
  name := "sample-backlog",
  libraryDependencies += "play-autosource" %% "reactivemongo" % "0.1-SNAPSHOT" exclude("org.scala-stm", "scala-stm_2.10.0"),
  resolvers += "Mandubian snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/"
).settings(play.Project.playScalaSettings: _*)
.dependsOn(library)

lazy val root = project.in(file(".")).aggregate(library)