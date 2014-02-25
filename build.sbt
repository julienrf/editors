val commonSettings = Seq(
  organization := "com.julienrf",
  scalacOptions ++= Seq("-feature", "-Xlint", "-unchecked"),
  scalaVersion := "2.10.3",
  resolvers += Resolver.sonatypeRepo("snapshots")
)

lazy val library = project.settings(commonSettings: _*).settings(
  name := "editors",
  version := "0.1-SNAPSHOT",
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % "2.2.1",
    "com.typesafe.play" %% "play-test" % "2.2.1" % "test",
    "org.specs2" %% "specs2" % "2.3.4" % "test",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "org.scalamacros" %% "quasiquotes" % "2.0.0-SNAPSHOT" cross CrossVersion.full
  ),
  resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.0-SNAPSHOT" cross CrossVersion.full)
).dependsOn(playDatacommons)

lazy val playDatacommons = project.in(file("dependencies/play-datacommons")).settings(commonSettings: _*).settings(
  libraryDependencies := Seq(
    "joda-time" % "joda-time" % "2.2",
    "org.joda" % "joda-convert" % "1.3.1",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "org.specs2" %% "specs2" % "2.1.1" % "test",
    "org.scalamacros" %% "quasiquotes" % "2.0.0-SNAPSHOT" cross CrossVersion.full
  ),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.0-SNAPSHOT" cross CrossVersion.full)
).dependsOn(playFunctional)

lazy val playFunctional = project.in(file("dependencies/play-functional")).settings(commonSettings: _*)

lazy val sampleBacklog = Project("sample-backlog", file("samples/backlog")).settings(commonSettings: _*).settings(
  name := "sample-backlog",
  libraryDependencies += "play-autosource" %% "reactivemongo" % "0.1-SNAPSHOT" exclude("org.scala-stm", "scala-stm_2.10.0"),
  resolvers += "Mandubian snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/"
).settings(play.Project.playScalaSettings: _*)
.dependsOn(library)

lazy val editors = project.in(file(".")).aggregate(library)