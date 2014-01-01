name := "editors"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.2.1",
  "com.typesafe.play" %% "play-test" % "2.2.1" % "test",
  "org.specs2" %% "specs2" % "2.3.4" % "test"
)

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

scalacOptions ++= Seq("-feature")

//offline := true