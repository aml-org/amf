import org.scalajs.core.tools.linker.ModuleKind
import sbt.Keys.{libraryDependencies, resolvers}
import sbtcrossproject.CrossPlugin.autoImport.crossProject
val ivyLocal = Resolver.file("ivy", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

name := "amf"

version in ThisBuild := "2.1.0-SNAPSHOT"

publish := {}

jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv()

val settings = Common.settings ++ Common.publish ++ Seq(
  organization := "com.github.amlorg",
  resolvers ++= List(ivyLocal, Common.releases, Common.snapshots, Resolver.mavenLocal),
  resolvers += "jitpack" at "https://jitpack.io",
  credentials ++= Common.credentials(),
  aggregate in assembly := false,
  libraryDependencies ++= Seq(
    "org.scalatest"    %%% "scalatest" % "3.0.0" % Test,
    "com.github.scopt" %%% "scopt"     % "3.7.0"
  )
)

/** **********************************************
  * AMF-Core
  ********************************************** */
lazy val defaultProfilesGenerationTask = TaskKey[Unit](
  "defaultValidationProfilesGeneration",
  "Generates the validation dialect documents for the standard profiles")

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .settings(
    Seq(
      name := "amf-core",
      libraryDependencies += "org.mule.syaml" %%% "syaml" % "0.4.6"
    ))
  .in(file("./amf-core"))
  .settings(settings: _*)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-native"         % "3.5.4",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-core-javadoc.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-core-module.js"
  )

lazy val coreJVM = core.jvm.in(file("./amf-core/jvm"))
lazy val coreJS  = core.js.in(file("./amf-core/js"))

/** **********************************************
  * AMF-WebAPI
  ********************************************** */
lazy val webapi = crossProject(JSPlatform, JVMPlatform)
  .settings(name := "amf-webapi")
  .dependsOn(core)
  .in(file("./amf-webapi"))
  .settings(settings: _*)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-native"         % "3.5.4",
    libraryDependencies += "com.github.everit-org.json-schema" % "org.everit.json.schema" % "1.9.1",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-webapi-javadoc.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-webapi-module.js"
  )

lazy val webapiJVM = webapi.jvm.in(file("./amf-webapi/jvm"))
lazy val webapiJS  = webapi.js.in(file("./amf-webapi/js"))

/** **********************************************
  * AMF-AML
  ********************************************** */
lazy val vocabularies = crossProject(JSPlatform, JVMPlatform)
  .settings(name := "amf-aml")
  .dependsOn(core)
  .in(file("./amf-aml"))
  .settings(settings: _*)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-native"         % "3.5.4",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-aml-javadoc.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-aml-module.js"
  )

lazy val vocabulariesJVM = vocabularies.jvm.in(file("./amf-aml/jvm"))
lazy val vocabulariesJS  = vocabularies.js.in(file("./amf-aml/js"))

/** **********************************************
  * AMF-Validation
  ********************************************** */
lazy val validation = crossProject(JSPlatform, JVMPlatform)
  .settings(name := "amf-validation")
  .dependsOn(core, vocabularies)
  .in(file("./amf-validation"))
  .settings(settings: _*)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-native"         % "3.5.4",
    libraryDependencies += "org.topbraid"           % "shacl"                   % "1.2.0-INTERNAL",
    libraryDependencies += "org.slf4j"              % "slf4j-simple"            % "1.7.12",
    libraryDependencies += "org.apache.commons" % "commons-compress" % "1.18",
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.7",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-validation-javadoc.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-validation-module.js"
  )

lazy val validationJVM = validation.jvm.in(file("./amf-validation/jvm"))
lazy val validationJS  = validation.js.in(file("./amf-validation/js"))

/** **********************************************
  * AMF Client
  ********************************************** */
lazy val client = crossProject(JSPlatform, JVMPlatform)
  .settings(Seq(
    name := "amf-client",
    fullRunTask(defaultProfilesGenerationTask, Compile, "amf.tasks.validations.ValidationProfileExporter")))
  .dependsOn(core, webapi, vocabularies, validation)
  .in(file("./amf-client"))
  .settings(settings: _*)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-native"         % "3.5.4",
    libraryDependencies += "org.topbraid"           % "shacl"                   % "1.2.0-INTERNAL",
    libraryDependencies += "org.apache.commons" % "commons-compress" % "1.18",
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.7",
    mainClass in Compile := Some("amf.Main"),
    packageOptions in (Compile, packageBin) += Package.ManifestAttributes("Automatic-Module-Name" → "org.mule.amf"),
    aggregate in assembly := true,
    test in assembly := {},
    mainClass in assembly := Some("amf.Main"),
    assemblyOutputPath in assembly := file(s"./amf-${version.value}.jar"),
    assemblyMergeStrategy in assembly := {
      case x if x.toString.contains("commons/logging")             => {
        MergeStrategy.discard
      }
      case x if x.toString.endsWith("JS_DEPENDENCIES")             => {
        MergeStrategy.discard
      }
      case PathList(ps @ _*) if ps.last endsWith "JS_DEPENDENCIES" => {
        MergeStrategy.discard
      }
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )
  .jsSettings(
    jsDependencies += ProvidedJS / "shacl.js",
    jsDependencies += ProvidedJS / "ajv.min.js",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-client-module.js"
  )

lazy val clientJVM = client.jvm.in(file("./amf-client/jvm"))
lazy val clientJS  = client.js.in(file("./amf-client/js"))

// Tasks

val buildJS = TaskKey[Unit](
  "buildJS",
  "Build npm module")
buildJS := {
  val _ = (fullOptJS in Compile in clientJS).value
  "./amf-client/js/build-scripts/buildjs.sh".!
}

addCommandAlias(
  "buildCommandLine",
  "; clean; clientJVM/assembly"
)
