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

version in ThisBuild := versions("amf.apicontract")

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
  "sonar.projectVersion"             -> versions("amf.apicontract"),
  "sonar.sourceEncoding"             -> "UTF-8",
  "sonar.github.repository"          -> "mulesoft/amf",
  "sonar.branch.name"                -> branch,
  "sonar.scala.coverage.reportPaths" -> "amf-cli/jvm/target/scala-2.12/scoverage-report/scoverage.xml,amf-api-contract/jvm/target/scala-2.12/scoverage-report/scoverage.xml",
  "sonar.sources"                    -> "amf-cli/shared/src/main/scala,amf-api-contract/shared/src/main/scala",
  "sonar.tests"                      -> "amf-cli/shared/src/test/scala"
)

val commonSettings = Common.settings ++ Common.publish ++ Seq(
  organization := "com.github.amlorg",
  resolvers ++= List(ivyLocal, Common.releases, Common.snapshots, Resolver.mavenLocal),
  resolvers += "jitpack" at "https://jitpack.io",
  credentials ++= Common.credentials(),
  aggregate in assembly := false,
  libraryDependencies ++= Seq(
    "org.scalatest"   %%% "scalatest"         % "3.0.5" % Test,
    "org.mule.common" %%% "scala-common-test" % "0.0.6" % Test,
    "org.slf4j" % "slf4j-nop" % "1.7.28" % Test
  ),
  logBuffered in Test := false
)

val amlVersion = versions("amf.aml")

lazy val amlJVMRef = ProjectRef(Common.workspaceDirectory / "amf-aml", "amlJVM")
lazy val amlJSRef  = ProjectRef(Common.workspaceDirectory / "amf-aml", "amlJS")
lazy val amlLibJVM = "com.github.amlorg" %% "amf-aml" % amlVersion
lazy val amlLibJS  = "com.github.amlorg" %% "amf-aml_sjs0.6" % amlVersion

lazy val rdfJVMRef = ProjectRef(Common.workspaceDirectory / "amf-aml", "rdfJVM")
lazy val rdfLibJVM = "com.github.amlorg" %% "amf-rdf" % amlVersion
lazy val rdfJSRef  = ProjectRef(Common.workspaceDirectory / "amf-aml", "rdfJS")
lazy val rdfLibJS  = "com.github.amlorg" %% "amf-rdf.6" % amlVersion

lazy val defaultProfilesGenerationTask = TaskKey[Unit](
  "defaultValidationProfilesGeneration",
  "Generates the validation dialect documents for the standard profiles")

/** **********************************************
  * AMF-Shapes
  * ********************************************* */
lazy val shapes = crossProject(JSPlatform, JVMPlatform)
  .settings(
    Seq(
      name := "amf-shapes"
    ))
  .in(file("./amf-shapes"))
  .settings(commonSettings)
  .jvmSettings(
    libraryDependencies += "org.scala-js"                      %% "scalajs-stubs"         % scalaJSVersion % "provided",
    libraryDependencies += "com.github.everit-org.json-schema" % "org.everit.json.schema" % "1.12.2",
    libraryDependencies += "org.json"                          % "json"                   % "20201115",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-shapes-javadoc.jar"
  )
  .jsSettings(
    jsDependencies += ProvidedJS / "ajv.min.js",
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-shapes-module.js",
    scalacOptions += "-P:scalajs:suppressExportDeprecations"
  )
  .disablePlugins(SonarPlugin)

lazy val shapesJVM =
  shapes.jvm.in(file("./amf-shapes/jvm")).sourceDependency(amlJVMRef, amlLibJVM)

lazy val shapesJS =
  shapes.js
    .in(file("./amf-shapes/js"))
    .sourceDependency(amlJSRef, amlLibJS)
    .disablePlugins(SonarPlugin, ScalaJsTypingsPlugin)


/** **********************************************
  * AMF-Api-contract
  * ********************************************* */
lazy val apiContract = crossProject(JSPlatform, JVMPlatform)
  .settings(
    Seq(
      name := "amf-api-contract"
    ))
  .in(file("./amf-api-contract"))
  .settings(commonSettings)
  .dependsOn(shapes)
  .jvmSettings(
    libraryDependencies += "org.scala-js"                      %% "scalajs-stubs"         % scalaJSVersion % "provided",
    libraryDependencies += "org.reflections"                   % "reflections"            % "0.9.12" % Test,
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-api-contract-javadoc.jar",
    mappings in (Compile, packageBin) += file("amf-apicontract.versions") -> "amf-apicontract.versions"
  )
  .jsSettings(
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-api-contract-module.js",
    scalacOptions += "-P:scalajs:suppressExportDeprecations"
  )
  .disablePlugins(SonarPlugin)

lazy val apiContractJVM =
  apiContract.jvm
    .in(file("./amf-api-contract/jvm"))
lazy val apiContractJS =
  apiContract.js
    .in(file("./amf-api-contract/js"))
    .disablePlugins(SonarPlugin, ScalaJsTypingsPlugin)

/** **********************************************
  * AMF-GRPC
  * ********************************************* */

lazy val antlrv4JVMRef = ProjectRef(Common.workspaceDirectory / "amf-antlr-ast", "antlrastJVM")
lazy val antlrv4JSRef  = ProjectRef(Common.workspaceDirectory / "amf-antlr-ast", "antlrastJS")
val antlrv4Version = "0.3.0-SNAPSHOT"
lazy val antlrv4LibJVM = "com.github.amlorg" %% "antlr-ast" % antlrv4Version
lazy val antlrv4LibJS  = "com.github.amlorg" %% "antlr-ast_sjs0.6" % antlrv4Version

lazy val grpc = crossProject(JSPlatform, JVMPlatform)
  .settings(
    Seq(
      name := "amf-grpc"
    ))
  .in(file("./amf-grpc"))
  .settings(commonSettings)
  .dependsOn(apiContract)
  .jvmSettings(
    libraryDependencies += "org.scala-js"                      %% "scalajs-stubs"         % scalaJSVersion % "provided",
    libraryDependencies += antlrv4LibJVM,
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-grpc-javadoc.jar",
    mappings in (Compile, packageBin) += file("amf-apicontract.versions") -> "amf-apicontract.versions"
  )
  .jsSettings(
    libraryDependencies += antlrv4LibJS,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "target" / "artifact" / "amf-grpc.js",
    scalacOptions += "-P:scalajs:suppressExportDeprecations"
  )
  .disablePlugins(SonarPlugin)

lazy val grpcJVM =
  grpc.jvm
    .in(file("./amf-grpc/jvm"))
    .sourceDependency(antlrv4JVMRef, antlrv4LibJVM)
lazy val grpcJS =
  grpc.js
    .in(file("./amf-grpc/js"))
    .disablePlugins(SonarPlugin, ScalaJsTypingsPlugin)
    .sourceDependency(antlrv4JSRef, antlrv4LibJS)

/** **********************************************
  * AMF CLI
  * ********************************************* */
lazy val cli = crossProject(JSPlatform, JVMPlatform)
  .settings(name := "amf-cli")
  .settings(fullRunTask(defaultProfilesGenerationTask, Compile, "amf.tasks.validations.ValidationProfileExporter"))
  .dependsOn(grpc)
  .in(file("./amf-cli"))
  .settings(commonSettings)
  .settings(
    libraryDependencies += "com.github.scopt" %%% "scopt" % "3.7.0"
  )
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"      % scalaJSVersion % "provided",
    libraryDependencies += "org.reflections"        % "reflections"         % "0.9.12",
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
    mainClass in Compile := Some("amf.cli.client.Main"),
    packageOptions in (Compile, packageBin) += Package.ManifestAttributes("Automatic-Module-Name" → "org.mule.amf"),
    mappings in (Compile, packageBin) += file("amf-apicontract.versions") -> "amf-apicontract.versions",
    aggregate in assembly := true,
    test in assembly := {},
    mainClass in assembly := Some("amf.cli.client.Main"),
    assemblyOutputPath in assembly := file(s"./amf-${version.value}.jar"),
    assemblyMergeStrategy in assembly := {
      case x if x.contains("commons/logging") => MergeStrategy.discard
      case x if x.endsWith("JS_DEPENDENCIES") => MergeStrategy.discard
      case x if x.contains("javax/annotation") => MergeStrategy.discard
      case PathList(ps @ _*) if ps.last endsWith "module-info.class" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith "JS_DEPENDENCIES" => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
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
  )
  .disablePlugins(SonarPlugin)

lazy val cliJVM = cli.jvm.in(file("./amf-cli/jvm"))
  .sourceDependency(rdfJVMRef % "test", rdfLibJVM % "test")
lazy val cliJS = cli.js.in(file("./amf-cli/js"))
  .sourceDependency(rdfJSRef % "test", rdfLibJS % "test")

// Tasks

val buildJS = TaskKey[Unit]("buildJS", "Build npm module")
buildJS := {
  val _ = (fullOptJS in Compile in cliJS).value
  "./amf-cli/js/build-scripts/buildjs.sh" !
}

addCommandAlias(
  "buildCommandLine",
  "; clean; cliJVM/assembly"
)
