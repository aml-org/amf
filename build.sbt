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
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule
  )

lazy val amfJVM = amf.jvm.in(file("amf-jvm"))
lazy val amfJS  = amf.js.in(file("amf-js"))

lazy val jsModule = amf.js
  .copy(id = "js-module")
  .in(file("amf-js/js-module"))
  .settings(
    jsSettings("amf-module.js", modules = true): _*
  )

lazy val jsBrowser = amf.js
  .copy(id = "js-browser")
  .in(file("amf-js/js-browser"))
  .settings(
    jsSettings("amf-browser.js", modules = false): _*
  )

def jsSettings(fileName: String, modules: Boolean): Array[Def.SettingsDefinition] = Array(
  artifactPath in (Compile, fullOptJS) := baseDirectory.value / ".." / "target" / "artifact" / fileName,
  scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
  scalaJSModuleKind := (if (modules) ModuleKind.CommonJSModule else ModuleKind.NoModule)
)

addCommandAlias("generate", ";js-browser/clean ;js-module/clean ;js-module/fullOptJS ;js-browser/fullOptJS")
