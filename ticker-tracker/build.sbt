import sbt._
import Keys._


lazy val Versions = new {
	val finagle = "6.42.0"
	val scalatest = "3.0.0"
	val scalacheck = ""
}

lazy val commonSettings = Seq(
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
		moduleName = "ticker-tracker",
		libraryDependencies ++= Seq(
			"com.twitter" %% "finagle-http" % Versions.finagle,
			"org.scalatest" %% "sclaatest" % Versions.scalatest % Test,
	    "org.scalacheck" %% "scalacheck" % Versions.scalacheck % Test
		)
	)