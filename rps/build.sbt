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
	organization := "com.rps",
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
  logLevel in ThisBuild := Level.Info,
	version := "0.1.0",
	scalacOptions in ThisBuild ++= Seq(
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

lazy val onlineStore = (project in file("."))
  .enablePlugins(BuildInfoPlugin, PlayScala)
  .settings(commonSettings: _*)
  .settings(
    name := "rps-api",
    scalaVersion := "2.11.8",
    buildInfoPackage := "controllers",
    javaOptions in Test += "-Denvironment=local",
    fork in run := true,
    buildInfoKeys ++= Seq[BuildInfoKey](
      resolvers,
      libraryDependencies
    ),
    routesGenerator := InjectedRoutesGenerator,
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoOptions += BuildInfoOption.ToJson,

    // no javadoc for BuildInfo.scala
    sources in(Compile, doc) <<= sources in(Compile, doc) map {
      _.filterNot(_.getName endsWith ".scala")
    },

    assemblyJarName in assembly := "rps-api.jar",
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", "io.netty.versions.properties", xs@_ *) => MergeStrategy.last
      case PathList("org", "joda", "time", "base", "BaseDateTime.class") => MergeStrategy.first // ES shades Joda
      case x => (assemblyMergeStrategy in assembly).value.apply(x)
    },
    mainClass in assembly := Some("play.core.server.ProdServerStart"),
    fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value),
    libraryDependencies ++= Seq(
      filters,
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "ch.qos.logback" % "logback-classic" % "1.1.7",
      "com.outworkers" %% "util-play" % Versions.util,
      "com.outworkers" %% "util-testing" % Versions.util % Test,
      "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0-M1" % Test
    )
  )
