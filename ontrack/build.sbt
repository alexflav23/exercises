import sbt._
import Keys._

lazy val Versions = new {
	val scalatest = "3.0.0"
	val scalacheck = "1.13.4"
  val util = "0.30.1"
}

lazy val Utils = new {
  def isJdk8: Boolean = sys.props("java.specification.version") == "1.8"

  def isCIBuild: Boolean = sys.env.get("CI").exists("true" ==)
}

lazy val commonSettings = Seq(
	organization := "com.ontrackretail",
  scalaVersion := "2.11.8",
  resolvers ++= Seq(
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

lazy val onTrack = (project in file("."))
	.settings(commonSettings: _*)
	.settings(
		name := "on-track",
		moduleName := "on-track",
		libraryDependencies ++= Seq(
      "com.outworkers" %% "util-parsers-cats" % Versions.util,
      "com.outworkers" %% "util-testing" % Versions.util % Test,
			"org.scalatest" %% "scalatest" % Versions.scalatest % Test,
	    "org.scalacheck" %% "scalacheck" % Versions.scalacheck % Test
		)
	)