import org.scalajs.core.tools.linker.ModuleKind
import sbt.Keys.{libraryDependencies, resolvers}
//val ivyLocal = Resolver.file("ivy", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

name := "amf"

version in ThisBuild := "1.5.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.2"

publish := {}

jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv()

val settings = Common.settings ++ Common.publish ++ Seq(
  organization := "amf",
  resolvers ++= List(Common.releases, Common.snapshots, Resolver.mavenLocal),
  credentials ++= Common.credentials(),
  aggregate in assembly := false,
  libraryDependencies ++= Seq(
    "org.scalatest"    %%% "scalatest" % "3.0.0" % Test,
    "com.github.scopt" %%% "scopt"     % "3.7.0"
  )
)

/** **********************************************
  * Parser-Core
  ********************************************** */
lazy val defaultProfilesGenerationTask = TaskKey[Unit](
  "defaultValidationProfilesGeneration",
  "Generates the validation dialect documents for the standard profiles")

lazy val core = crossProject
  .settings(
    Seq(
      name := "parser-core",
      libraryDependencies += "org.mule.syaml" %%% "syaml" % "0.2.2"
    ))
  .in(file("./parser-core"))
  .settings(settings: _*)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-jackson"         % "3.5.2",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "parser-core-javadoc.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "parser-core-module.js"
  )

lazy val coreJVM = core.jvm.in(file("./parser-core/jvm"))
lazy val coreJS  = core.js.in(file("./parser-core/js"))

/** **********************************************
  * Parser-WebAPI
  ********************************************** */
lazy val webapi = crossProject
  .settings(name := "parser-webapi")
  .dependsOn(core)
  .in(file("./parser-webapi"))
  .settings(settings: _*)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-jackson"         % "3.5.2",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "parser-webapi-javadoc.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "parser-webapi-module.js"
  )

lazy val webapiJVM = webapi.jvm.in(file("./parser-webapi/jvm"))
lazy val webapiJS  = webapi.js.in(file("./parser-webapi/js"))

/** **********************************************
  * Parser-AML
  ********************************************** */
lazy val vocabularies = crossProject
  .settings(name := "parser-aml")
  .dependsOn(core)
  .in(file("./parser-aml"))
  .settings(settings: _*)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-jackson"         % "3.5.2",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "parser-aml-javadoc.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "parser-aml-module.js"
  )

lazy val vocabulariesJVM = vocabularies.jvm.in(file("./parser-aml/jvm"))
lazy val vocabulariesJS  = vocabularies.js.in(file("./parser-aml/js"))

/** **********************************************
  * Parser-Validation
  ********************************************** */
lazy val validation = crossProject
  .settings(name := "parser-validation")
  .dependsOn(core, vocabularies)
  .in(file("./parser-validation"))
  .settings(settings: _*)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-jackson"         % "3.5.2",
    libraryDependencies += "org.topbraid"           % "shacl"                   % "1.1.0",
    libraryDependencies += "org.slf4j"              % "slf4j-simple"            % "1.7.12",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "parser-validation-javadoc.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "parser-validation-module.js"
  )

lazy val validationJVM = validation.jvm.in(file("./parser-validation/jvm"))
lazy val validationJS  = validation.js.in(file("./parser-validation/js"))

/** **********************************************
  * AMF Client
  ********************************************** */
lazy val client = crossProject
  .settings(Seq(
    name := "parser-client",
    fullRunTask(defaultProfilesGenerationTask, Compile, "amf.tasks.validations.ValidationProfileExporter")))
  .dependsOn(core, webapi, vocabularies, validation)
  .in(file("./parser-client"))
  .settings(settings: _*)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-jackson"         % "3.5.2",
    libraryDependencies += "org.topbraid"           % "shacl"                   % "1.1.0",
    mainClass in Compile := Some("amf.Main"),
    packageOptions in (Compile, packageBin) += Package.ManifestAttributes("Automatic-Module-Name" → "amf"),
      aggregate in assembly := true,
    test in assembly := {},
    mainClass in assembly := Some("amf.Main"),
    assemblyOutputPath in assembly := file(s"./parser-${version.value}.jar"),
    assemblyMergeStrategy in assembly := {
      case x if x.toString.endsWith("JS_DEPENDENCIES")             => MergeStrategy.discard
      case PathList(ps @ _*) if ps.last endsWith "JS_DEPENDENCIES" => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )
  .jsSettings(
    jsDependencies += ProvidedJS / "shacl.js",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "parser-client-module.js"
  )

lazy val clientJVM = client.jvm.in(file("./parser-client/jvm"))
lazy val clientJS  = client.js.in(file("./parser-client/js"))

// Tasks

val publishJS = TaskKey[Unit](
  "publishJS",
  "Publish npm module")

publishJS := {
  val _ = (fullOptJS in Compile in clientJS).value
  "./parser-client/js/build-scripts/deploy-develop.sh".!
}

val buildJS = TaskKey[Unit](
  "buildJS",
  "Build npm module")
buildJS := {
  val _ = (fullOptJS in Compile in clientJS).value
  "./parser-client/js/build-scripts/buildjs.sh".!
}

publish := {
  val _ = (publish.value, publishJS.value)
  ()
}

addCommandAlias(
  "buildCommandLine",
  "; clean; clientJVM/assembly"
)
