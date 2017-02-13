import sbt._
import Keys._


lazy val Versions = new {
	val finagle = "6.42.0"
	val scalatest = "3.0.0"
	val scalacheck = "1.13.4"
  val util = "0.28.2"
}

lazy val commonSettings = Seq(
	organization := "com.markit",
  scalaVersion := "2.11.8",
  resolvers ++= Seq(
    "Twitter Repository" at "http://maven.twttr.com",
    Resolver.typesafeRepo("releases"),
    Resolver.sonatypeRepo("releases"),
    Resolver.jcenterRepo
  ),
  scalacOptions in ThisBuild ++= Seq(
    "-language:experimental.macros",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:existentials",
    "-Xlint",
    "-deprecation",
    "-feature",
    "-unchecked"
  ),
  logLevel in ThisBuild := Level.Info,
	version := "0.1.0",
	scalacOptions in ThisBuild ++= Seq(
    "-language:experimental.macros",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:existentials",
    "-Xlint",
    "-deprecation",
    "-feature",
    "-unchecked"
  )
)

lazy val tickerTracker = (project in file("."))
	.settings(commonSettings: _*)
	.settings(
		name := "ticker-tracker",
		moduleName := "ticker-tracker",
		libraryDependencies ++= Seq(
			"com.twitter" %% "finagle-http" % Versions.finagle,
      "com.outworkers" %% "util-testing" % Versions.util,
			"org.scalatest" %% "scalatest" % Versions.scalatest % Test,
	    "org.scalacheck" %% "scalacheck" % Versions.scalacheck % Test
		)
	)