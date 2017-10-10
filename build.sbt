import org.scalajs.core.tools.linker.ModuleKind
import sbt.Keys.{libraryDependencies, resolvers}

name := "AMF"

jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv()

scalaVersion in ThisBuild := "2.12.2"

val settings = Common.settings ++ Seq(
  name := "amf",
  version := "0.0.1-SNAPSHOT",
  libraryDependencies ++= Seq(
    "org.mulesoft"     %%% "syaml"     % "0.0.2",
    "org.scalatest"    %%% "scalatest" % "3.0.0" % Test,
    "com.github.scopt" %%% "scopt"     % "3.7.0"
  ),
  resolvers ++= List(Common.releases, Common.snapshots, Resolver.mavenLocal),
  credentials ++= Common.credentials()
)

lazy val root = project
  .in(file("."))
  .aggregate(amfJS, amfJVM)
  .enablePlugins(ScalaJSPlugin)

lazy val importScalaTask = TaskKey[Unit]("tsvScalaImport", "Import validations from AMF TSV files and generates a Scala object with the information")

lazy val amf = crossProject
  .in(file("."))
  .settings(settings: _*)
  .jvmSettings(
    Common.publish,
    addArtifact(artifact in (Compile, assembly), assembly),
    publishArtifact in (Compile, packageBin) := false,
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-jackson"         % "3.5.2",
    //
    // This is temporary until SHACL has been published in some Maven repository
    unmanagedJars in Compile += file("lib/shacl-1.0.1-SNAPSHOT.jar"),
    // libraryDependencies += "org.topbraid" % "shacl" % "1.0.1-SNAPSHOT",
    //
    test in assembly := {},
    assemblyOutputPath in assembly := baseDirectory.value / "target" / "artifact" / "amf.jar",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-javadoc.jar",
    fullRunTask(importScalaTask, Compile, "amf.tasks.tsvimport.ScalaExporter"),
    mainClass in Compile := Some("amf.client.Main")
  )
  .jsSettings(
    jsDependencies += ProvidedJS / "shacl.js",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "js-main-module.js",
    scalaJSUseMainModuleInitializer := true,
    assemblyMergeStrategy in assembly := {
      case "JS_DEPENDENCIES"              => MergeStrategy.discard
      case PathList("META-INF", xs @ _ *) => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

lazy val amfJVM = amf.jvm.in(file("amf-jvm"))
lazy val amfJS  = amf.js.in(file("amf-js"))

lazy val module = crossProject
  .in(file("amf-js/js-module"))
  .dependsOn(amf)
  .enablePlugins(ScalaJSPlugin)
  .jsSettings(
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / ".." / ".." / "target" / "artifact" / "amf-module.js",
    scalaJSUseMainModuleInitializer := false,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    publish := {
      "./amf-js/build-scripts/deploy-develop.sh".!
    }
  )
  .js

addCommandAlias("generate", "; clean; moduleJS/fullOptJS; generateJSMainModule; generateJVM")
addCommandAlias("generateJSMainModule", "; amfJS/fullOptJS")
addCommandAlias("generateJVM", "; amfJVM/assembly; amfJVM/packageDoc")
addCommandAlias("publish", "; clean; moduleJS/fullOptJS; moduleJS/publish; amfJVM/publish")
