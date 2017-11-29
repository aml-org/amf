import org.scalajs.core.tools.linker.ModuleKind
import sbt.Keys.{libraryDependencies, resolvers}

name := "AMF"

jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv()

scalaVersion in ThisBuild := "2.12.2"

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

lazy val amfCoreProject = project
  .in(file("./amf-core"))
  .aggregate(amfCoreJS, amfCoreJVM)
  .enablePlugins(ScalaJSPlugin)

lazy val importScalaTask = TaskKey[Unit]("tsvScalaImport", "Import validations from AMF TSV files and generates a Scala object with the information")
lazy val defaultProfilesGenerationTask = TaskKey[Unit]("defaultValidationProfilesGeneration", "Generates the validation dialect documents for the standard profiles")

lazy val amfCore = crossProject
  .settings(Seq(
    name := "amf-core",
    version := "1.0.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.mulesoft"     %%% "syaml"     % "0.0.6"
    )
  ))
  .in(file("./amf-core"))
  .settings(settings: _*)
  .jvmSettings(
    Common.publish,
    addArtifact(artifact in (Compile, assembly), assembly),
    publishArtifact in (Compile, packageBin) := false,
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-jackson"         % "3.5.2",
    test in assembly := {},
    assemblyOutputPath in assembly := baseDirectory.value / "target" / "artifact" / "amf-core.jar",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-core-javadoc.jar",
    mainClass in Compile := Some("amf.client.Main")
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-core-main-module.js",
    scalaJSUseMainModuleInitializer := true,
    assemblyMergeStrategy in assembly := {
      case "JS_DEPENDENCIES"              => MergeStrategy.discard
      case PathList("META-INF", xs @ _ *) => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

lazy val amfCoreJVM = amfCore.jvm.in(file("./amf-core/jvm"))
lazy val amfCoreJS = amfCore.js.in(file("./amf-core/js"))


/************************************************
  * AMF-WebAPI
  ***********************************************/

lazy val amfWebApiProject = project
  .in(file("./amf-webapi"))
  .aggregate(amfWebApiJS, amfWebApiJVM)
  .enablePlugins(ScalaJSPlugin)

lazy val amfWebApi = crossProject
  .settings(Seq(
    name := "amf-webapi",
    version := "1.0.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.mulesoft"     %%% "syaml"     % "0.0.6"
    )
  ))
  .dependsOn(amfCore)
  .in(file("./amf-webapi"))
  .settings(settings: _*)
  .jvmSettings(
    Common.publish,
    addArtifact(artifact in (Compile, assembly), assembly),
    publishArtifact in (Compile, packageBin) := false,
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-jackson"         % "3.5.2",
    test in assembly := {},
    assemblyOutputPath in assembly := baseDirectory.value / "target" / "artifact" / "amf-webapi.jar",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-webapi-javadoc.jar",
    fullRunTask(importScalaTask, Compile, "amf.tasks.tsvimport.ScalaExporter")
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-webapi-main-module.js",
    scalaJSUseMainModuleInitializer := true,
    assemblyMergeStrategy in assembly := {
      case "JS_DEPENDENCIES"              => MergeStrategy.discard
      case PathList("META-INF", xs @ _ *) => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

lazy val amfWebApiJVM = amfWebApi.jvm.in(file("./amf-webapi/jvm"))
lazy val amfWebApiJS = amfWebApi.js.in(file("./amf-webapi/js"))


/************************************************
  * AMF-Vocabularies
  ***********************************************/


lazy val amfVocabulariesRoot = project
  .in(file("./amf-vocabularies"))
  .aggregate(amfVocabulariesJS, amfVocabulariesJVM)
  .enablePlugins(ScalaJSPlugin)

lazy val amfVocabularies = crossProject
  .settings(Seq(
    name := "amf-vocabularies",
    version := "1.0.0-SNAPSHOT"
  ))
  .dependsOn(amfCore)
  .in(file("./amf-vocabularies"))
  .settings(settings: _*)
  .jvmSettings(
    Common.publish,
    addArtifact(artifact in (Compile, assembly), assembly),
    publishArtifact in (Compile, packageBin) := false,
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-jackson"         % "3.5.2",
    test in assembly := {},
    assemblyOutputPath in assembly := baseDirectory.value / "target" / "artifact" / "amf-vocabularies.jar",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-vocabularies-javadoc.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-vocabularies-main-module.js",
    scalaJSUseMainModuleInitializer := true,
    assemblyMergeStrategy in assembly := {
      case "JS_DEPENDENCIES"              => MergeStrategy.discard
      case PathList("META-INF", xs @ _ *) => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

lazy val amfVocabulariesJVM = amfVocabularies.jvm.in(file("./amf-vocabularies/jvm"))
lazy val amfVocabulariesJS = amfVocabularies.js.in(file("./amf-vocabularies/js"))


/************************************************
  * AMF-Validation
  ***********************************************/


lazy val amfValidationRoot = project
  .in(file("./amf-validation"))
  .aggregate(amfValidationJS, amfValidationJVM)
  .enablePlugins(ScalaJSPlugin)

lazy val amfValidation = crossProject
  .settings(Seq(
    name := "amf-validation",
    version := "1.0.0-SNAPSHOT"
  ))
  .dependsOn(amfCore, amfVocabularies)
  .in(file("./amf-validation"))
  .settings(settings: _*)
  .jvmSettings(
    Common.publish,
    addArtifact(artifact in (Compile, assembly), assembly),
    publishArtifact in (Compile, packageBin) := false,
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-jackson"         % "3.5.2",
    libraryDependencies += "org.topbraid" % "shacl" % "1.0.1",
    test in assembly := {},
    assemblyOutputPath in assembly := baseDirectory.value / "target" / "artifact" / "amf-vocabularies.jar",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-validation-javadoc.jar"
  )
  .jsSettings(
    jsDependencies += ProvidedJS / "shacl.js",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-validation-main-module.js",
    scalaJSUseMainModuleInitializer := true,
    assemblyMergeStrategy in assembly := {
      case "JS_DEPENDENCIES"              => MergeStrategy.discard
      case PathList("META-INF", xs @ _ *) => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

lazy val amfValidationJVM = amfValidation.jvm.in(file("./amf-validation/jvm"))
lazy val amfValidationJS = amfValidation.js.in(file("./amf-validation/js"))

/************************************************
  * AMF Client
  ***********************************************/


lazy val amfClientRoot = project
  .in(file("./amf-client"))
  .aggregate(amfClientJS, amfClientJVM)
  .enablePlugins(ScalaJSPlugin)

lazy val amfClient = crossProject
  .settings(Seq(
    name := "amf-client",
    version := "1.0.0-SNAPSHOT",
    fullRunTask(importScalaTask, Compile, "amf.tasks.tsvimport.ScalaExporter"),
    fullRunTask(defaultProfilesGenerationTask, Compile, "amf.tasks.validations.ValidationProfileExporter")
  ))
  .dependsOn(amfCore, amfWebApi, amfVocabularies, amfValidation)
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

lazy val amfClientJVM = amfClient.jvm.in(file("./amf-client/jvm"))
lazy val amfClientJS = amfClient.js.in(file("./amf-client/js"))

// Taks

addCommandAlias("generate", "; clean; moduleJS/fullOptJS; generateJSMainModule; generateJVM")
addCommandAlias("generateJSMainModule", "; amfClientJS/fullOptJS")
addCommandAlias("generateJVM", "; amfJClientJVM/assembly; amfClientJVM/packageDoc")
addCommandAlias("publish", "; clean; moduleJS/fullOptJS; moduleJS/publish; amfJVM/publish")
