import com.gu.deploy.PlayArtifact._
import com.gu.deploy.PlayAssetHash._
import com.typesafe.sbtscalariform.ScalariformPlugin._
import org.sbtidea.SbtIdeaPlugin._
import sbt._
import sbt.Keys._
import sbt.PlayProject._
import sbtassembly.Plugin.AssemblyKeys._

object Frontend extends Build with Prototypes {
  val version = "1-SNAPSHOT"

  val common = library("common")

  val article = application("article").dependsOn(common)
  val gallery = application("gallery").dependsOn(common)

  val tag = application("tag").dependsOn(common)
  val section = application("section").dependsOn(common)
  val front = application("front").dependsOn(common)

  val main = root().aggregate(
    common, article, gallery, tag, section, front
  )
}

trait Prototypes {
  val version: String

  def root() = Project("root", base = file(".")).settings(ideaSettings: _*)

  def base(name: String) = PlayProject(name, version, path = file(name), mainLang = SCALA)
    .settings(playAssetHashDistSettings: _*)
    .settings(scalariformSettings: _*)
    .settings(
      scalaVersion := "2.9.1",

      maxErrors := 20,
      javacOptions := Seq("-g", "-source", "1.6", "-target", "1.6", "-encoding", "utf8"),
      scalacOptions := Seq("-unchecked", "-optimise", "-deprecation", "-Xcheckinit", "-encoding", "utf8"),

      ivyXML :=
        <dependencies>
          <exclude org="commons-logging"><!-- Conflicts with jcl-over-slf4j in Play. --></exclude>
          <exclude org="org.springframework"><!-- Because I don't like it. --></exclude>
        </dependencies>,

      organization := "com.gu",

      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "1.7.1" % "test"
      ),

      // Use ScalaTest https://groups.google.com/d/topic/play-framework/rZBfNoGtC0M/discussion
      testOptions in Test := Nil,

      // Copy unit test resources https://groups.google.com/d/topic/play-framework/XD3X6R-s5Mc/discussion
      unmanagedClasspath in Test <+= (baseDirectory) map { bd => Attributed.blank(bd / "test") },

      templatesImport ++= Seq(
        "common._",
        "views._",
        "views.support._"
      )
    )

  def library(name: String) = base(name).settings(
    staticFilesPackage := "frontend-static",
    resolvers += "Guardian Github Releases" at "http://guardian.github.com/maven/repo-releases",
    libraryDependencies ++= Seq(
      "com.gu" %% "management-play" % "5.8",
      "com.gu" %% "management-logback" % "5.8",
      "com.gu" %% "configuration" % "3.6",
      "com.gu.openplatform" %% "content-api-client" % "1.15",

      "org.codehaus.jackson" % "jackson-core-asl" % "1.9.6",
      "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.6",
      "org.jsoup" % "jsoup" % "1.6.2",
      "org.jboss.dna" % "dna-common" % "0.6"
    )
  )

  def application(name: String) = base(name).settings(
    staticFilesPackage := "frontend-static",
    templatesImport ++= Seq(
      "conf.Static"
    ),

    executableName := "frontend-%s" format  name,
    jarName in assembly <<= (executableName) { "%s.jar" format _ }
  )
}