import sbt._
import Keys._

lazy val Versions = new {
	val finagle = "6.42.0"
	val scalatest = "3.0.0"
	val scalacheck = "1.13.4"
  val util = "0.28.2"
  val scalaCsv = "1.3.4"
}

lazy val Utils = new {
  def isJdk8: Boolean = sys.props("java.specification.version") == "1.8"

  def isCIBuild: Boolean = sys.env.get("CI").exists("true" ==)
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
  fork in Test := true,
  javaOptions in Test ++= {
    if (Utils.isJdk8) {
      println("Using metaspace settings for JDK8 compatible VMs")
      Seq(
        "-XX:MetaspaceSize=256m",
        "-XX:MaxMetaspaceSize=512m"
      )
    } else {
      println("Using legacy perm gen settings.")
      Seq(
        "-XX:PermSize=256m",
        "-XX:MaxPermSize=512m"
      )
    }
  },
  testOptions in Test ++= {
    if (Utils.isCIBuild) {
      // Under CI show test durations with short stack traces.
      Seq(Tests.Argument("-oD"), Tests.Argument("-oS"))
    } else {
      // Under normal circumstances show full stack traces and duration.
      Seq(Tests.Argument("-oF"), Tests.Argument("-oD"))
    }
  },
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
      "com.github.tototoshi" %% "scala-csv" % Versions.scalaCsv,
			"com.twitter" %% "finagle-http" % Versions.finagle,
      "com.outworkers" %% "util-validators-cats" % Versions.util,
      "com.outworkers" %% "util-testing" % Versions.util,
			"org.scalatest" %% "scalatest" % Versions.scalatest % Test,
	    "org.scalacheck" %% "scalacheck" % Versions.scalacheck % Test
		)
	)