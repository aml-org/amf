import java.io.FileOutputStream
import java.util.Properties

import org.scalajs.core.tools.linker.ModuleKind
import sbt.Keys.{libraryDependencies, resolvers}
import sbtcrossproject.CrossPlugin.autoImport.crossProject

import scala.collection.JavaConversions
val ivyLocal = Resolver.file("ivy", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

name := "amf"

version in ThisBuild := "3.1.4-0"

publish := {}

jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv()

//libraryDependencies += "org.codehaus.sonar.runner" % "sonar-runner-dist" % "2.4"

enablePlugins(SonarRunnerPlugin)

val setSonarProperties = TaskKey[Unit](
  "setSonarProperties",
  "Set sonar properties!"
)

setSonarProperties := {
  lazy val url = sys.env.getOrElse("SONAR_SERVER_URL", "Not found url.")
  lazy val token = sys.env.getOrElse("SONAR_SERVER_TOKEN", "Not found token.")

  val values = Map(
    "sonar.host.url" -> url,
    "sonar.login" -> token,
    "sonar.projectKey" -> "mulesoft.amf",
    "sonar.projectName" -> "AMF",
    "sonar.projectVersion" -> "1.0.0",

    "sonar.sourceEncoding" -> "UTF-8",
    "sonar.github.repository" -> "mulesoft/amf",

    "sonar.modules" -> "amf-core,amf-webapi,amf-aml,amf-validation,amf-client",

    "amf-core.sonar.sources" -> "shared/src/main/scala",
    "amf-core.sonar.scoverage.reportPath" -> "jvm/target/scala-2.12/scoverage-report/scoverage.xml",

    "amf-webapi.sonar.sources" -> "shared/src/main/scala",
    "amf-webapi.sonar.scoverage.reportPath" -> "jvm/target/scala-2.12/scoverage-report/scoverage.xml",

    "amf-aml.sonar.sources" -> "shared/src/main/scala",
    "amf-aml.sonar.scoverage.reportPath" -> "jvm/target/scala-2.12/scoverage-report/scoverage.xml",

    "amf-validation.sonar.sources" -> "shared/src/main/scala",
    "amf-validation.sonar.scoverage.reportPath" -> "jvm/target/scala-2.12/scoverage-report/scoverage.xml",

    "amf-client.sonar.sources" -> "shared/src/main/scala",
    "amf-client.sonar.tests" -> "shared/src/test/scala",
    "amf-client.sonar.scoverage.reportPath" -> "jvm/target/scala-2.12/scoverage-report/scoverage.xml"
  )

  sonarProperties := values

  val p = new Properties()
  values.foreach(v => p.put(v._1, v._2))
  val stream = new FileOutputStream(file("./sonar-project.properties"))
  p.store(stream, null)
  stream.close()
}

val sonarMe = TaskKey[Unit](
  "sonarMe",
  "Run sonar!")
sonarMe := {

//  sonarRunnerOptions := Seq(
//    "-D",
//    s"sonar.host.url=$url",
//    "-D",
//    s"sonar.login=$token"
//  )

//  val a = generateSonarConfiguration.value

  setSonarProperties.value
  sonar.value
}

val settings = Common.settings ++ Common.publish ++ Seq(
  organization := "com.github.amlorg",
  resolvers ++= List(ivyLocal, Common.releases, Common.snapshots, Resolver.mavenLocal),
  resolvers += "jitpack" at "https://jitpack.io",
  credentials ++= Common.credentials(),
  aggregate in assembly := false,
  libraryDependencies ++= Seq(
    "org.scalatest"    %%% "scalatest" % "3.0.5" % Test,
    "com.github.scopt" %%% "scopt"     % "3.7.0"
  )
)

/** **********************************************
  * AMF-Core
  * ********************************************* */
lazy val defaultProfilesGenerationTask = TaskKey[Unit](
  "defaultValidationProfilesGeneration",
  "Generates the validation dialect documents for the standard profiles")

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .settings(
    Seq(
      name := "amf-core",
      libraryDependencies += "org.mule.syaml" %%% "syaml" % "0.6.5"
    ))
  .in(file("./amf-core"))
  .settings(settings)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-native"          % "3.5.4",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-core-javadoc.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-core-module.js"
  )

lazy val coreJVM = core.jvm.in(file("./amf-core/jvm"))
lazy val coreJS  = core.js.in(file("./amf-core/js"))

/** **********************************************
  * AMF-WebAPI
  * ********************************************* */
lazy val webapi = crossProject(JSPlatform, JVMPlatform)
  .settings(Seq(
    name := "amf-webapi"
  ))
  .dependsOn(core)
  .in(file("./amf-webapi"))
  .settings(settings)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-native"         % "3.5.4",
    libraryDependencies += "com.github.everit-org.json-schema" % "org.everit.json.schema" % "1.9.2",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-webapi-javadoc.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-webapi-module.js"
  )

lazy val webapiJVM = webapi.jvm.in(file("./amf-webapi/jvm"))
lazy val webapiJS  = webapi.js.in(file("./amf-webapi/js"))

/** **********************************************
  * AMF-AML
  * ********************************************* */
lazy val vocabularies = crossProject(JSPlatform, JVMPlatform)
  .settings(Seq(
    name := "amf-aml"
  ))
  .dependsOn(core)
  .in(file("./amf-aml"))
  .settings(settings)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-native"          % "3.5.4",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-aml-javadoc.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-aml-module.js"
  )

lazy val vocabulariesJVM = vocabularies.jvm.in(file("./amf-aml/jvm"))
lazy val vocabulariesJS  = vocabularies.js.in(file("./amf-aml/js"))

/** **********************************************
  * AMF-Validation
  * ********************************************* */
lazy val validation = crossProject(JSPlatform, JVMPlatform)
  .settings(Seq(
    name := "amf-validation"
  ))
  .dependsOn(core, vocabularies)
  .in(file("./amf-validation"))
  .settings(settings)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-native"          % "3.5.4",
    libraryDependencies += "org.topbraid"           % "shacl"                   % "1.2.0-INTERNAL",
    libraryDependencies += "org.slf4j"              % "slf4j-simple"            % "1.7.12",
    libraryDependencies += "org.apache.commons" % "commons-compress" % "1.18",
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-validation-javadoc.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-validation-module.js"
  )

lazy val validationJVM = validation.jvm.in(file("./amf-validation/jvm"))
lazy val validationJS  = validation.js.in(file("./amf-validation/js"))

/** **********************************************
  * AMF Client
  * ********************************************* */
lazy val client = crossProject(JSPlatform, JVMPlatform)
  .settings(Seq(
    name := "amf-client",
    fullRunTask(defaultProfilesGenerationTask, Compile, "amf.tasks.validations.ValidationProfileExporter")
  ))
  .dependsOn(core, webapi, vocabularies, validation)
  .in(file("./amf-client"))
  .settings(settings)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-native"          % "3.5.4",
    libraryDependencies += "org.topbraid"           % "shacl"                   % "1.2.0-INTERNAL",
    libraryDependencies += "org.apache.commons" % "commons-compress" % "1.18",
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8",
    mainClass in Compile := Some("amf.Main"),
    packageOptions in (Compile, packageBin) += Package.ManifestAttributes("Automatic-Module-Name" → "org.mule.amf"),
    aggregate in assembly := true,
    test in assembly := {},
    mainClass in assembly := Some("amf.Main"),
    assemblyOutputPath in assembly := file(s"./amf-${version.value}.jar"),
    assemblyMergeStrategy in assembly := {
      case x if x.toString.contains("commons/logging") => MergeStrategy.discard
      case x if x.toString.endsWith("JS_DEPENDENCIES") => MergeStrategy.discard
      case PathList(ps @ _*) if ps.last endsWith "JS_DEPENDENCIES" => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )
  .jsSettings(
    jsDependencies += ProvidedJS / "shacl.js",
    jsDependencies += ProvidedJS / "ajv.min.js",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-client-module.js"
  )

lazy val clientJVM = client.jvm.in(file("./amf-client/jvm"))
lazy val clientJS  = client.js.in(file("./amf-client/js"))

/** **********************************************
  * AMF Tools
  ********************************************** */
lazy val tools = crossProject(JVMPlatform)
  .settings(Seq(
    name := "amf-tools",
    fullRunTask(defaultProfilesGenerationTask, Compile, "amf.tasks.validations.ValidationProfileExporter")))
  .dependsOn(core, webapi, vocabularies, validation)
  .in(file("./amf-tools"))
  .settings(settings)
  .jvmSettings(
    libraryDependencies += "org.reflections" % "reflections" % "0.9.11",
    mainClass in Compile := Some("amf.VocabularyExporter"),
    mainClass in assembly := Some("amf.VocabularyExporter"),
    assemblyOutputPath in assembly := file(s"./amf-${version.value}.jar")
  )

lazy val toolsJVM = tools.jvm.in(file("./amf-tools/jvm"))

// Tasks

val buildJS = TaskKey[Unit]("buildJS", "Build npm module")
buildJS := {
  val _ = (fullOptJS in Compile in clientJS).value
  "./amf-client/js/build-scripts/buildjs.sh".!
}

addCommandAlias(
  "buildCommandLine",
  "; clean; clientJVM/assembly"
)

/** **********************************************
  * AMF Runner
  ********************************************** */

lazy val amfRunner = project
  .in(file("./amf-runner"))
  .dependsOn(clientJVM)
  .settings(settings)
