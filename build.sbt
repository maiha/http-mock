val Scala211 = "2.11.11"
val Scala212 = "2.12.4"

crossScalaVersions := Scala212 :: Scala211 :: Nil
scalaVersion := Scala212

scalacOptions := Seq(
  "-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation",
  "-feature", "-unchecked", "-language:implicitConversions", "-language:postfixOps")

xerial.sbt.Sonatype.sonatypeRootSettings

// Maven Publishing
// http://www.scala-sbt.org/0.13/docs/Using-Sonatype.html

publishMavenStyle := true
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

version := "0.3.4-SNAPSHOT"
organization := "sc.ala"
name := "http-mock"
description := "Real http server for stubbing and expectations in Scala"
homepage := Some(url("https://github.com/maiha/http-mock"))
licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))

pomExtra := (
     <developers>
        <developer>
          <id>maiha</id>
          <name>Kazunori Nishi</name>
          <url>https://github.com/maiha</url>
        </developer>
      </developers>
      <scm>
        <url>https://github.com/maiha/http-mock</url>
        <connection>scm:git:git@github.com:maiha/http-mock.git</connection>
      </scm>
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-netty-server" % "2.6.7",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "com.ning" % "async-http-client" % "1.9.40" % "test"
)

fork in run := true
fork in Test := true
