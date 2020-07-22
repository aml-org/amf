import org.scalajs.core.tools.linker.ModuleKind
import sbt.Keys.{libraryDependencies, resolvers}
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtsonar.SonarPlugin.autoImport.sonarProperties

import scala.language.postfixOps
import scala.sys.process._
import Versions.versions

val ivyLocal = Resolver.file("ivy", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

name := "amf"

version in ThisBuild := versions("amf.webapi")

publish := {}

jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv()

lazy val sonarUrl = sys.env.getOrElse("SONAR_SERVER_URL", "Not found url.")
lazy val sonarToken = sys.env.getOrElse("SONAR_SERVER_TOKEN", "Not found token.")
lazy val branch = sys.env.getOrElse("BRANCH_NAME", "develop")

//enablePlugins(ScalaJSBundlerPlugin)

sonarProperties ++= Map(
  "sonar.login" -> sonarToken,
  "sonar.projectKey" -> "mulesoft.amf",
  "sonar.projectName" -> "AMF",
  "sonar.projectVersion" -> versions("amf.webapi"),

  "sonar.sourceEncoding" -> "UTF-8",
  "sonar.github.repository" -> "mulesoft/amf",
  "sonar.branch.name" -> branch,

  "sonar.scala.coverage.reportPaths" -> "amf-client/jvm/target/scala-2.12/scoverage-report/scoverage.xml,amf-webapi/jvm/target/scala-2.12/scoverage-report/scoverage.xml,amf-validation/jvm/target/scala-2.12/scoverage-report/scoverage.xml",
  "sonar.sources" -> "amf-client/shared/src/main/scala,amf-webapi/shared/src/main/scala,amf-validation/shared/src/main/scala",
  "sonar.tests" -> "amf-client/shared/src/test/scala"
)

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

lazy val workspaceDirectory: File =
  sys.props.get("sbt.mulesoft") match {
    case Some(x) => file(x)
    case _       => Path.userHome / "mulesoft"
  }

val amfAmlVersion = versions("amf.aml")

lazy val amfAmlJVMRef = ProjectRef(workspaceDirectory / "amf-aml", "amlJVM")
lazy val amfAmlJSRef = ProjectRef(workspaceDirectory / "amf-aml", "amlJS")
lazy val amfAmlLibJVM = "com.github.amlorg" %% "amf-aml" % amfAmlVersion
lazy val amfAmlLibJS = "com.github.amlorg" %% "amf-aml_sjs0.6" % amfAmlVersion


lazy val defaultProfilesGenerationTask = TaskKey[Unit](
  "defaultValidationProfilesGeneration",
  "Generates the validation dialect documents for the standard profiles")

/** **********************************************
  * AMF-WebAPI
  * ********************************************* */
lazy val webapi = crossProject(JSPlatform, JVMPlatform)
  .settings(Seq(
    name := "amf-webapi"
  ))
  .in(file("./amf-webapi"))
  .settings(settings)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-native"         % "3.5.4",
    libraryDependencies += "com.github.everit-org.json-schema" % "org.everit.json.schema" % "1.9.2",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-webapi-javadoc.jar",
    mappings in (Compile, packageBin) += file("amf-webapi.versions") -> "amf-webapi.versions"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-webapi-module.js",
    scalacOptions += "-P:scalajs:suppressExportDeprecations"
  ).disablePlugins(SonarPlugin)

lazy val webapiJVM = webapi.jvm.in(file("./amf-webapi/jvm")).sourceDependency(amfAmlJVMRef, amfAmlLibJVM)
lazy val webapiJS  = webapi.js.in(file("./amf-webapi/js")).sourceDependency(amfAmlJSRef, amfAmlLibJS).disablePlugins(SonarPlugin)

/** **********************************************
  * AMF-Validation
  * ********************************************* */
lazy val validation = crossProject(JSPlatform, JVMPlatform)
  .settings(Seq(
    name := "amf-validation"
  ))
  .in(file("./amf-validation"))
  .settings(settings)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-native"          % "3.5.4",
    libraryDependencies += "org.apache.jena"        % "apache-jena-libs"        % "3.14.0" pomOnly(),
    libraryDependencies += "org.apache.jena"        % "jena-shacl"              % "3.14.0",
    libraryDependencies += "org.apache.commons" % "commons-compress" % "1.19",
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-validation-javadoc.jar"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-validation-module.js"
  ).disablePlugins(SonarPlugin)

lazy val validationJVM = validation.jvm.in(file("./amf-validation/jvm")).sourceDependency(amfAmlJVMRef, amfAmlLibJVM)
lazy val validationJS  = validation.js.in(file("./amf-validation/js")).sourceDependency(amfAmlJSRef, amfAmlLibJS).disablePlugins(SonarPlugin)

/** **********************************************
  * AMF Client
  * ********************************************* */
lazy val client = crossProject(JSPlatform, JVMPlatform)
  .settings(name := "amf-client")
  .settings(fullRunTask(defaultProfilesGenerationTask, Compile, "amf.tasks.validations.ValidationProfileExporter"))
  .dependsOn( webapi, validation)
  .in(file("./amf-client"))
  .settings(settings)
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.reflections" % "reflections" % "0.9.12",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    libraryDependencies += "org.json4s"             %% "json4s-native"          % "3.5.4",
    libraryDependencies += "org.topbraid"           % "shacl"                   % "1.3.0",
    libraryDependencies += "org.apache.commons" % "commons-compress" % "1.18",
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8",
    mainClass in Compile := Some("amf.Main"),
    packageOptions in (Compile, packageBin) += Package.ManifestAttributes("Automatic-Module-Name" → "org.mule.amf"),
    mappings in (Compile, packageBin) += file("amf-webapi.versions") -> "amf-webapi.versions",
    aggregate in assembly := true,
    test in assembly := {},
    mainClass in assembly := Some("amf.Main"),
    assemblyOutputPath in assembly := file(s"./amf-${version.value}.jar"),
    assemblyMergeStrategy in assembly := {
      case x if x.toString.contains("commons/logging") => MergeStrategy.discard
      case x if x.toString.endsWith("JS_DEPENDENCIES") => MergeStrategy.discard
      case PathList(ps @ _*) if ps.last endsWith "module-info.class" => {
        MergeStrategy.first
      }
      case PathList(ps @ _*) if ps.last endsWith "JS_DEPENDENCIES" => {
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
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-client-module.js"
  ).disablePlugins(SonarPlugin)

lazy val clientJVM = client.jvm.in(file("./amf-client/jvm")).sourceDependency(amfAmlJVMRef, amfAmlLibJVM)
lazy val clientJS  = client.js.in(file("./amf-client/js")).sourceDependency(amfAmlJSRef, amfAmlLibJS).disablePlugins(SonarPlugin)

/** **********************************************
  * AMF Tools
  ********************************************** */
//lazy val tools = crossProject(JVMPlatform)
//  .settings(Seq(
//    name := "amf-tools",
//    fullRunTask(defaultProfilesGenerationTask, Compile, "amf.tasks.validations.ValidationProfileExporter")))
//  .dependsOn(core, webapi, vocabularies, validation, client % "compile->compile;test->test")
//  .in(file("./amf-tools"))
//  .settings(settings)
//  .jvmSettings(
//    mainClass in Compile := Some("amf.VocabularyExporter"),
//    mainClass in assembly := Some("amf.VocabularyExporter"),
//    assemblyOutputPath in assembly := file(s"./amf-${version.value}.jar")
//  )
//
//lazy val toolsJVM = tools.jvm.in(file("./amf-tools/jvm"))

// Tasks

val buildJS = TaskKey[Unit]("buildJS", "Build npm module")
buildJS := {
  val _ = (fullOptJS in Compile in clientJS).value
  "./amf-client/js/build-scripts/buildjs.sh" !
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
  .disablePlugins(SonarPlugin)
