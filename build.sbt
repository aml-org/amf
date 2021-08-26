import org.scalajs.core.tools.linker.ModuleKind
import sbt.Keys.{libraryDependencies, resolvers}
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtsonar.SonarPlugin.autoImport.sonarProperties

import scala.language.postfixOps
import scala.sys.process._
import Versions.versions
import org.mulesoft.typings.generation.ScalaClassFilterBuilder
import org.mulesoft.typings.resolution.BuiltInMappings.{dictionary, option, overwrite}
import org.mulesoft.typings.resolution.MappingFactory
import org.mulesoft.typings.resolution.namespace.PrefixNamespaceReplacer

val ivyLocal = Resolver.file("ivy", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

name := "amf"

version in ThisBuild := versions("amf.webapi")

publish := {}

jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv()

lazy val sonarUrl   = sys.env.getOrElse("SONAR_SERVER_URL", "Not found url.")
lazy val sonarToken = sys.env.getOrElse("SONAR_SERVER_TOKEN", "Not found token.")
lazy val branch     = sys.env.getOrElse("BRANCH_NAME", "develop")

//enablePlugins(ScalaJSBundlerPlugin)

sonarProperties ++= Map(
  "sonar.login"                      -> sonarToken,
  "sonar.projectKey"                 -> "mulesoft.amf",
  "sonar.projectName"                -> "AMF",
  "sonar.projectVersion"             -> versions("amf.webapi"),
  "sonar.sourceEncoding"             -> "UTF-8",
  "sonar.github.repository"          -> "mulesoft/amf",
  "sonar.branch.name"                -> branch,
  "sonar.scala.coverage.reportPaths" -> "amf-client/jvm/target/scala-2.12/scoverage-report/scoverage.xml,amf-webapi/jvm/target/scala-2.12/scoverage-report/scoverage.xml",
  "sonar.sources"                    -> "amf-client/shared/src/main/scala,amf-webapi/shared/src/main/scala",
  "sonar.tests"                      -> "amf-client/shared/src/test/scala"
)

val commonSettings = Common.settings ++ Common.publish ++ Seq(
  organization := "com.github.amlorg",
  resolvers ++= List(ivyLocal, Common.releases, Common.snapshots, Resolver.mavenLocal),
  resolvers += "jitpack" at "https://jitpack.io",
  credentials ++= Common.credentials(),
  aggregate in assembly := false,
  libraryDependencies ++= Seq(
    "org.scalatest"   %%% "scalatest"         % "3.0.5" % Test,
    "org.mule.common" %%% "scala-common-test" % "0.0.6" % Test
  ),
  logBuffered in Test := false
)

val customValidationVersion = versions("amf.custom.validations")

lazy val customValidationJVMRef = ProjectRef(Common.workspaceDirectory / "amf-aml", "customValidationJVM")
lazy val customValidationJSRef  = ProjectRef(Common.workspaceDirectory / "amf-aml", "customValidationJS")
lazy val customValidationLibJVM = "com.github.amlorg" %% "amf-custom-validation" % customValidationVersion
lazy val customValidationLibJS  = "com.github.amlorg" %% "amf-custom-validation_sjs0.6" % customValidationVersion

val coreVersion = versions("amf.core")

lazy val coreJVMRef = ProjectRef(Common.workspaceDirectory / "amf-core", "coreJVM")
lazy val coreJSRef  = ProjectRef(Common.workspaceDirectory / "amf-core", "coreJS")
lazy val coreLibJVM = "com.github.amlorg" %% "amf-core" % coreVersion
lazy val coreLibJS  = "com.github.amlorg" %% "amf-core_sjs0.6" % coreVersion

lazy val defaultProfilesGenerationTask = TaskKey[Unit](
  "defaultValidationProfilesGeneration",
  "Generates the validation dialect documents for the standard profiles")

/** **********************************************
  * AMF-WebAPI
  * ********************************************* */
lazy val webapi = crossProject(JSPlatform, JVMPlatform)
  .settings(
    Seq(
      name := "amf-webapi"
    ))
  .in(file("./amf-webapi"))
  .settings(commonSettings)
  .jvmSettings(
    libraryDependencies += "org.scala-js"                      %% "scalajs-stubs"         % scalaJSVersion % "provided",
    libraryDependencies += "com.github.everit-org.json-schema" % "org.everit.json.schema" % "1.12.2",
    libraryDependencies += "org.json"                          % "json"                   % "20201115",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-webapi-javadoc.jar",
    mappings in (Compile, packageBin) += file("amf-webapi.versions") -> "amf-webapi.versions"
  )
  .jsSettings(
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-webapi-module.js",
    scalacOptions += "-P:scalajs:suppressExportDeprecations"
  )
  .disablePlugins(SonarPlugin)

lazy val webapiJVM =
  webapi.jvm.in(file("./amf-webapi/jvm")).sourceDependency(coreJVMRef, coreLibJVM).sourceDependency(customValidationJVMRef, customValidationLibJVM)
lazy val webapiJS =
  webapi.js
    .in(file("./amf-webapi/js"))
    .sourceDependency(coreJSRef, coreLibJS)
    .sourceDependency(customValidationJSRef, customValidationLibJS)
    .disablePlugins(SonarPlugin, ScalaJsTypingsPlugin)

/** **********************************************
  * AMF Client
  * ********************************************* */
lazy val client = crossProject(JSPlatform, JVMPlatform)
  .settings(name := "amf-client")
  .settings(fullRunTask(defaultProfilesGenerationTask, Compile, "amf.tasks.validations.ValidationProfileExporter"))
  .dependsOn(webapi)
  .in(file("./amf-client"))
  .settings(commonSettings)
  .settings(
    libraryDependencies += "com.github.scopt" %%% "scopt" % "3.7.0"
  )
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"      % scalaJSVersion % "provided",
    libraryDependencies += "org.reflections"        % "reflections"         % "0.9.12",
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
    mainClass in Compile := Some("amf.Main"),
    packageOptions in (Compile, packageBin) += Package.ManifestAttributes("Automatic-Module-Name" → "org.mule.amf"),
    mappings in (Compile, packageBin) += file("amf-webapi.versions") -> "amf-webapi.versions",
    aggregate in assembly := true,
    test in assembly := {},
    mainClass in assembly := Some("amf.Main"),
    assemblyOutputPath in assembly := file(s"./amf-${version.value}.jar"),
    assemblyMergeStrategy in assembly := {
      case x if x.contains("commons/logging") => MergeStrategy.discard
      case x if x.contains("javax/annotation") => MergeStrategy.discard
      case x if x.endsWith("JS_DEPENDENCIES") => MergeStrategy.discard
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
    },
    assembly / artifact := {
      val art = (assembly / artifact).value
      art.withClassifier(Some("assembly"))
    },
    addArtifact(assembly / artifact, assembly)
  )
  .jsSettings(
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-client-module.js",
    typingModuleName := "amf-client-js",
    customMappings := MappingFactory()
      .map("ClientList").to("Array")
      .map("ClientFuture").to("Promise")
      .map("AmfCustomClass").to("AnotherCustomClass")
      .map("ClientOption").to(option())
      .map("ClientMap").to(dictionary())
      .map("AnyVal").to("any")
      .map("ClientLoader").to("ClientResourceLoader")
      .map("ClientReference").to("ClientReferenceResolver")
      .map("DocBuilder").to(overwrite("JsOutputBuilder"))
      .map("Unit").to("void"),
    namespaceReplacer := PrefixNamespaceReplacer("amf\\.client\\.", ""),
    scalaFilteredClasses := ScalaClassFilterBuilder()
      .withClassFilter("^.*\\.DataTypes$")
      .withClassFilter("^.*\\.remod\\..*$")
      .withClassFilter("^.*\\.JsFs$")
      .withClassFilter("^.*\\.SysError$")
      .withClassFilter("^amf\\.core\\.remote\\.*$")
      .withClassFilter("^.*\\.JSValidation.*$")
      .withClassFilter("^.*\\.Main.*$")
      .withClassFilter("^.*\\.Https$")
      .withClassFilter("^.*\\.Http$")
      .withClassFilter("^.*\\.ExecutionLog.*$")
      .withClassFilter("^.*\\.ErrorHandler.*$")
      .withMethodFilter("^.*\\.ValidationReport$", "toString")
      .withMethodFilter("^.*\\.BaseUnit", "toNativeRdfModel")
      .withMethodFilter("^.*\\.Linkable", "link")
      .withClassFilter("^.*\\.JSONSchemaVersions.*$")
      .withTypeFilter("^.*$", "JSONSchemaVersion")
      .withTypeFilter("^.*$", "Option")
      .withTypeFilter("^.*$", "Long")
      .withTypeFilter("^.*$", "Seq")
      .withTypeFilter("^.*$", "CharStream")
  )
  .disablePlugins(SonarPlugin)

lazy val clientJVM =
  client.jvm.in(file("./amf-client/jvm"))
lazy val clientJS = client.js.in(file("./amf-client/js"))

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
