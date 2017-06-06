name := "AMF scala proof of concept"

scalaVersion in ThisBuild := "2.12.2"
lazy val root = project.in(file(".")).
  aggregate(amfJS, amfJVM).
  settings(
  )

lazy val amf = crossProject.in(file(".")).
  settings(
      name := "amf",
      version := "0.1",
      libraryDependencies += "com.lihaoyi" %%% "utest" % "0.4.7" % "test",
      testFrameworks += new TestFramework("utest.runner.Framework")
  ).
  jvmSettings(
      // Add JVM-specific settings here
      libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  ).
  jsSettings(
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
      scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
      scalaJSModuleKind := ModuleKind.CommonJSModule
  )

lazy val amfJVM = amf.jvm.in(file("amf-jvm"))
lazy val amfJS = amf.js.in(file("amf-js"))
