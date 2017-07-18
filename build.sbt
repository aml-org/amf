import scoverage.ScoverageKeys

name := "AMF"

scalaVersion in ThisBuild := "2.12.2"
lazy val root = project
  .in(file("."))
  .aggregate(amfJS, amfJVM)
  .settings(
      // Redefining publish for valkyr pipeline, which includes a publish task
      publish := { }
    )

lazy val amf = crossProject
  .in(file("."))
  .settings(
    name := "amf",
    version := "0.1",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "test",
    // Redefining publish for valkyr pipeline, which includes a publish task
    publish := { }
  )
  .jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule
  )

lazy val amfJVM = amf.jvm.in(file("amf-jvm"))
lazy val amfJS  = amf.js.in(file("amf-js"))
