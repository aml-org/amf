import org.scalajs.core.tools.linker.ModuleKind
import sbt.Keys.{libraryDependencies, resolvers}

name := "AMF"

val settings = Common.settings ++ Seq(
  name := "amf",
  version := "0.0.1-SNAPSHOT",

  libraryDependencies ++= Seq(
    "org.mulesoft" %%% "syaml" % "0.0.1",
    "org.scalatest" %%% "scalatest" % "3.0.0" % Test
  ),

  resolvers ++= List(Common.releases, Common.snapshots),
  credentials ++= Common.credentials()
)

lazy val root = project
  .in(file("."))
  .aggregate(amfJS, amfJVM)

lazy val amf = crossProject
  .in(file("."))
  .settings(settings: _*)
  .jvmSettings(
    Common.publish,
    addArtifact(artifact in (Compile, assembly), assembly),
    publishArtifact in (Compile, packageBin) := false,
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    test in assembly := {},
    assemblyOutputPath in assembly := baseDirectory.value / "target" / "artifact" / "amf.jar",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-javadoc.jar"
  )
  .jsSettings(
    publish := {
      "./amf-js/build-scripts/deploy-develop.sh".!
    },
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule
  )

lazy val amfJVM = amf.jvm.in(file("amf-jvm"))
lazy val amfJS  = amf.js.in(file("amf-js"))

addCommandAlias("generate", "; clean; generateModuleJS; generateBrowserJS; generateJVM")
addCommandAlias("generateBrowserJS", "; browserJS/fullOptJS")
addCommandAlias("generateModuleJS", "; moduleJS/fullOptJS")
addCommandAlias("generateJVM", "; amfJVM/assembly; amfJVM/packageDoc")
addCommandAlias("publish", "; clean; generateModuleJS; amfJS/publish; amfJVM/publish")
