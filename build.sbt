import org.scalajs.core.tools.linker.ModuleKind
import sbt.Keys.{libraryDependencies, resolvers}

name := "amf"

version in ThisBuild := "1.0.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.2"

jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv()

val settings = Common.settings ++ Seq(
  resolvers ++= List(Common.releases, Common.snapshots, Resolver.mavenLocal),
  credentials ++= Common.credentials(),
  libraryDependencies ++= Seq(
    "org.scalatest"    %%% "scalatest" % "3.0.0" % Test,
    "com.github.scopt" %%% "scopt"     % "3.7.0"
  )
)

/************************************************
  * AMF-Core
  ***********************************************/

lazy val importScalaTask = TaskKey[Unit]("tsvScalaImport", "Import validations from AMF TSV files and generates a Scala object with the information")
lazy val defaultProfilesGenerationTask = TaskKey[Unit]("defaultValidationProfilesGeneration", "Generates the validation dialect documents for the standard profiles")

lazy val core = crossProject
  .settings(Seq(
    libraryDependencies ++= Seq(
      "org.mulesoft"     %%% "syaml"     % "0.0.6"
    )
  ))
  .in(file("./amf-core"))
  .settings(settings: _*)
  .jvmSettings(
    Common.publish,
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-jackson"         % "3.5.2",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-core-javadoc.jar",
    mainClass in Compile := Some("amf.client.Main")
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-core-main-module.js",
    scalaJSUseMainModuleInitializer := true
  )

lazy val coreJVM = core.jvm.in(file("./amf-core/jvm"))
lazy val coreJS = core.js.in(file("./amf-core/js"))


/************************************************
  * AMF-WebAPI
  ***********************************************/

lazy val webapi = crossProject
  .dependsOn(core)
  .in(file("./amf-webapi"))
  .settings(settings: _*)
  .jvmSettings(
    Common.publish,
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-jackson"         % "3.5.2",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-webapi-javadoc.jar",
    fullRunTask(importScalaTask, Compile, "amf.tasks.tsvimport.ScalaExporter")
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-webapi-main-module.js",
    scalaJSUseMainModuleInitializer := true
  )

lazy val webapiJVM = webapi.jvm.in(file("./amf-webapi/jvm"))
lazy val webapiJS = webapi.js.in(file("./amf-webapi/js"))


/************************************************
  * AMF-Vocabularies
  ***********************************************/

lazy val vocabularies = crossProject
  .dependsOn(core)
  .in(file("./amf-vocabularies"))
  .settings(settings: _*)
  .jvmSettings(
    Common.publish,
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-jackson"         % "3.5.2",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-vocabularies-javadoc.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-vocabularies-main-module.js",
    scalaJSUseMainModuleInitializer := true
  )

lazy val vocabulariesJVM = vocabularies.jvm.in(file("./amf-vocabularies/jvm"))
lazy val vocabulariesJS = vocabularies.js.in(file("./amf-vocabularies/js"))


/************************************************
  * AMF-Validation
  ***********************************************/

lazy val validation = crossProject
  .dependsOn(core, vocabularies)
  .in(file("./amf-validation"))
  .settings(settings: _*)
  .jvmSettings(
    Common.publish,
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-jackson"         % "3.5.2",
    libraryDependencies += "org.topbraid" % "shacl" % "1.0.1",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-validation-javadoc.jar"
  )
  .jsSettings(
    jsDependencies += ProvidedJS / "shacl.js",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-validation-main-module.js",
    scalaJSUseMainModuleInitializer := true
  )

lazy val validationJVM = validation.jvm.in(file("./amf-validation/jvm"))
lazy val validationJS = validation.js.in(file("./amf-validation/js"))

/************************************************
  * AMF Client
  ***********************************************/

lazy val client = crossProject
  .settings(Seq(
    fullRunTask(importScalaTask, Compile, "amf.tasks.tsvimport.ScalaExporter"),
    fullRunTask(defaultProfilesGenerationTask, Compile, "amf.tasks.validations.ValidationProfileExporter")
  ))
  .dependsOn(core, webapi, vocabularies, validation)
  .in(file("./amf-client"))
  .settings(settings: _*)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-jackson"         % "3.5.2",
    libraryDependencies += "org.topbraid" % "shacl" % "1.0.1"
  )
  .jsSettings(
    jsDependencies += ProvidedJS / "shacl.js",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSModuleKind := ModuleKind.CommonJSModule
  )

lazy val clientJVM = client.jvm.in(file("./amf-client/jvm"))
lazy val clientJS = client.js.in(file("./amf-client/js"))

// Tasks

addCommandAlias("generate", "; clean; moduleJS/fullOptJS; generateJSMainModule; generateJVM")
addCommandAlias("generateJSMainModule", "; amfClientJS/fullOptJS")
addCommandAlias("generateJVM", "; amfJClientJVM/assembly; amfClientJVM/packageDoc")
addCommandAlias("publish", "; clean; moduleJS/fullOptJS; moduleJS/publish; amfJVM/publish")
