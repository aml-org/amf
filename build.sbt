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

/*
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
    libraryDependencies += "org.topbraid" % "shacl" % "1.0.1",

    test in assembly := {},
    assemblyOutputPath in assembly := baseDirectory.value / "target" / "artifact" / "amf.jar",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-javadoc.jar",
    fullRunTask(importScalaTask, Compile, "amf.tasks.tsvimport.ScalaExporter"),
    fullRunTask(defaultProfilesGenerationTask, Compile, "amf.tasks.validations.ValidationProfileExporter"),
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
    resolvers ++= List(Common.releases, Common.snapshots, Resolver.mavenLocal),
    credentials ++= Common.credentials(),
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / ".." / ".." / "target" / "artifact" / "amf-module.js",
    scalaJSUseMainModuleInitializer := false,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    publish := {
      "./amf-js/build-scripts/deploy-develop.sh".!
    }
  )
  .js
*/

// New modules

/************************************************
  * AMF-Core
  ***********************************************/

lazy val amfCore = project
  .in(file("./amf-core"))
  .aggregate(amfCoreJS, amfCoreJVM)
  .enablePlugins(ScalaJSPlugin)

lazy val importScalaTask = TaskKey[Unit]("tsvScalaImport", "Import validations from AMF TSV files and generates a Scala object with the information")
lazy val defaultProfilesGenerationTask = TaskKey[Unit]("defaultValidationProfilesGeneration", "Generates the validation dialect documents for the standard profiles")

lazy val amfCoreCrossProject = crossProject
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
//    fullRunTask(defaultProfilesGenerationTask, Compile, "amf.tasks.validations.ValidationProfileExporter"),
    mainClass in Compile := Some("amf.client.Main")
  )
  .jsSettings(
//    jsDependencies += ProvidedJS / "shacl.js",
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

lazy val amfCoreJVM = amfCoreCrossProject.jvm.in(file("./amf-core/jvm"))
lazy val amfCoreJS = amfCoreCrossProject.js.in(file("./amf-core/js"))


/************************************************
  * AMF-WebAPI
  ***********************************************/

lazy val amfWebApi = project
  .in(file("./amf-webapi"))
  .aggregate(amfWebApiJS, amfWebApiJVM)
  .enablePlugins(ScalaJSPlugin)

lazy val amfWebApiCrossProject = crossProject
  .settings(Seq(
    name := "amf-webapi",
    version := "1.0.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.mulesoft"     %%% "syaml"     % "0.0.6"
    )
  ))
  .dependsOn(amfCoreCrossProject)
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

lazy val amfWebApiJVM = amfWebApiCrossProject.jvm.in(file("./amf-webapi/jvm"))
lazy val amfWebApiJS = amfWebApiCrossProject.js.in(file("./amf-webapi/js"))


/************************************************
  * AMF-Vocabularies
  ***********************************************/


lazy val amfVocabularies = project
  .in(file("./amf-vocabularies"))
  .aggregate(amfVocabulariesJS, amfVocabulariesJVM)
  .enablePlugins(ScalaJSPlugin)

lazy val amfVocabulariesCrossProject = crossProject
  .settings(Seq(
    name := "amf-vocabularies",
    version := "1.0.0-SNAPSHOT"
  ))
  .dependsOn(amfCoreCrossProject)
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

lazy val amfVocabulariesJVM = amfVocabulariesCrossProject.jvm.in(file("./amf-vocabularies/jvm"))
lazy val amfVocabulariesJS = amfVocabulariesCrossProject.js.in(file("./amf-vocabularies/js"))


/************************************************
  * AMF-Validation
  ***********************************************/


lazy val amfValidation = project
  .in(file("./amf-validation"))
  .aggregate(amfValidationJS, amfValidationJVM)
  .enablePlugins(ScalaJSPlugin)

lazy val amValidationCrossProject = crossProject
  .settings(Seq(
    name := "amf-validation",
    version := "1.0.0-SNAPSHOT"
  ))
  .dependsOn(amfCoreCrossProject, amfVocabulariesCrossProject)
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
    //    jsDependencies += ProvidedJS / "shacl.js",
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

lazy val amfValidationJVM = amValidationCrossProject.jvm.in(file("./amf-validation/jvm"))
lazy val amfValidationJS = amValidationCrossProject.js.in(file("./amf-validation/js"))


// Taks

addCommandAlias("generate", "; clean; moduleJS/fullOptJS; generateJSMainModule; generateJVM")
addCommandAlias("generateJSMainModule", "; amfJS/fullOptJS")
addCommandAlias("generateJVM", "; amfJVM/assembly; amfJVM/packageDoc")
addCommandAlias("publish", "; clean; moduleJS/fullOptJS; moduleJS/publish; amfJVM/publish")
