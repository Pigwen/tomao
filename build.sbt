name := "tomao"

version := "1.0"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.withSource := true

resolvers ++= Seq(
	"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
	)

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.2.1",
	"com.typesafe.akka" %% "akka-slf4j" % "2.2.1")
