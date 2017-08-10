import org.scalajs.core.tools.linker.ModuleKind

name := "AMF"

scalaVersion in ThisBuild := "2.12.2"
lazy val root = project
  .in(file("."))
  .aggregate(amfJS, amfJVM)
  .settings(
    // Redefining publish for valkyr pipeline, which includes a publish task
    publish := {}
  )

lazy val amf = crossProject
  .in(file("."))
  .settings(
    name := "amf",
    version := "0.1",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "test",
    // Redefining publish for valkyr pipeline, which includes a publish task
    publish := {}
  )
  .jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided",
    test in assembly := {},
    assemblyOutputPath in assembly := baseDirectory.value / "target" / "artifact" / "amf.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule
  )

lazy val amfJVM = amf.jvm.in(file("amf-jvm"))
lazy val amfJS  = amf.js.in(file("amf-js"))

lazy val module = crossProject
  .in(file("amf-js/js-module"))
  .dependsOn(amf)
  .jsSettings(
    jsSettings("amf-module.js", ModuleKind.CommonJSModule): _*
  ).js

lazy val browser = crossProject
  .in(file("amf-js/js-browser"))
  .dependsOn(amf)
  .jsSettings(
    jsSettings("amf-browser.js", ModuleKind.NoModule): _*
  ).js

def jsSettings(fileName: String, kind: ModuleKind): Array[Def.SettingsDefinition] = Array(
  artifactPath in (Compile, fullOptJS) := baseDirectory.value / ".." / ".." / "target" / "artifact" / fileName,
  scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
  scalaJSModuleKind := kind
)

addCommandAlias("generate", "; clean; moduleJS/fullOptJS; browserJS/fullOptJS; amfJVM/assembly")
