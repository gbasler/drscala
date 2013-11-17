import sbt._
import Keys._

object build extends Build {
  lazy val root = Project(
    id = "root",
    base = file("."),
    aggregate = Seq(plugin, main),
    settings = sharedSettings
  )

  lazy val sharedSettings = Defaults.defaultSettings ++ Seq(
    scalaVersion := "2.10.3",
    organization := "com.github.aloiscochard",
    name := "drscala",
    version := "0.1.0-SNAPSHOT",
    description := "A doctor for your code"
  )

  // This subproject contains the Scala compiler plugin
  lazy val plugin = Project(
    id = "plugin",
    base = file("plugin"),
    settings = sharedSettings ++ Seq[Project.Setting[_]](
      libraryDependencies ++= Seq("org.scala-lang" % "scala-compiler" % scalaVersion.value,
        "org.kohsuke" % "github-api" % "1.44"),
      publishArtifact in Compile := false
    )
  )

  // Scalac command line options to install our compiler plugin.
  lazy val usePluginSettings = Seq(
    scalacOptions in Compile ++= {
      val jar: File = (Keys.`package` in(plugin, Compile)).value
      val addPlugin = "-Xplugin:" + jar.getAbsolutePath
      // add plugin timestamp to compiler options to trigger recompile of
      // main after editing the plugin. (Otherwise a 'clean' is needed.)
      val dummy = "-Jdummy=" + jar.lastModified
      val drscala = Seq(
        "debug",
        "warn"
      ).map("-P:drscala:" + _)
      Seq(addPlugin, dummy) ++ drscala
    }
//    javaOptions in Compile := Seq("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
  )

  // A regular module with the application code.
  lazy val main = Project(
    id = "main",
    base = file("main"),
    settings = sharedSettings ++ usePluginSettings
  )
}
