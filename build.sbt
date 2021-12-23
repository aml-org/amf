
import Common.snapshots
import org.scalajs.core.tools.linker.ModuleKind
import sbt.Keys.{libraryDependencies, resolvers}
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtsonar.SonarPlugin.autoImport.sonarProperties

import scala.language.postfixOps
import scala.sys.process._
import Versions.versions
import sbtassembly.AssemblyPlugin.autoImport.assembly

val ivyLocal = Resolver.file("ivy", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

name := "amf"

ThisBuild / version := versions("amf.apicontract")
ThisBuild / organization := "com.github.amlorg"
ThisBuild / scalaVersion := "2.12.11"
ThisBuild / resolvers ++= List(ivyLocal, Common.releases, Common.snapshots, Common.public, Resolver.mavenLocal, Resolver.mavenCentral)
ThisBuild / credentials ++= Common.credentials()

val apiContractModelVersion = settingKey[String]("Version of the AMF API Contract Model")

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
  assembly / aggregate := false,
  libraryDependencies ++= Seq(
    "org.scalatest"   %%% "scalatest"         % "3.0.5" % Test,
    "org.mule.common" %%% "scala-common-test" % "0.0.6" % Test,
    "org.slf4j" % "slf4j-nop" % "1.7.28" % Test
  ),
  Test / logBuffered := false
)

val amlVersion = versions("amf.aml")

lazy val amlJVMRef = ProjectRef(Common.workspaceDirectory / "amf-aml", "amlJVM")
lazy val amlJSRef  = ProjectRef(Common.workspaceDirectory / "amf-aml", "amlJS")
lazy val amlLibJVM = "com.github.amlorg" %% "amf-aml" % amlVersion
lazy val amlLibJS  = "com.github.amlorg" %% "amf-aml_sjs0.6" % amlVersion

lazy val rdfJVMRef = ProjectRef(Common.workspaceDirectory / "amf-aml", "rdfJVM")
lazy val rdfLibJVM = "com.github.amlorg" %% "amf-rdf" % amlVersion
lazy val rdfJSRef  = ProjectRef(Common.workspaceDirectory / "amf-aml", "rdfJS")
lazy val rdfLibJS  = "com.github.amlorg" %% "amf-rdf_sjs0.6" % amlVersion

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
    Compile / packageDoc / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-shapes-javadoc.jar"
  )
  .jsSettings(
    jsDependencies += ProvidedJS / "ajv.min.js",
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    Compile / fullOptJS / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-shapes-module.js",
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
  .enablePlugins(BuildInfoPlugin)
  .settings(
    Seq(
      name := "amf-api-contract"
    ))
  .in(file("./amf-api-contract"))
  .settings(commonSettings ++ Seq(
    apiContractModelVersion := versions("amf.model"),
    buildInfoKeys := Seq[BuildInfoKey](apiContractModelVersion),
    buildInfoPackage := "amf.apicontract.internal.unsafe"
  ))
  .dependsOn(shapes)
  .jvmSettings(
    libraryDependencies += "org.scala-js"                      %% "scalajs-stubs"         % scalaJSVersion % "provided",
    libraryDependencies += "org.reflections"                   % "reflections"            % "0.9.12" % Test,
    Compile / packageDoc / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-api-contract-javadoc.jar",
    Compile / packageBin / mappings += file("amf-apicontract.versions") -> "amf-apicontract.versions"
  )
  .jsSettings(
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    Compile / fullOptJS / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-api-contract-module.js",
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
  * AMF-ANTLR-SYNTAX
  * ********************************************* */

lazy val antlrv4JVMRef = ProjectRef(Common.workspaceDirectory / "amf-antlr-ast", "antlrastJVM")
lazy val antlrv4JSRef  = ProjectRef(Common.workspaceDirectory / "amf-antlr-ast", "antlrastJS")
val antlrv4Version = "0.4.0-SNAPSHOT"
lazy val antlrv4LibJVM = "com.github.amlorg" %% "antlr-ast" % antlrv4Version
lazy val antlrv4LibJS  = "com.github.amlorg" %% "antlr-ast_sjs0.6" % antlrv4Version

lazy val antlr = crossProject(JSPlatform, JVMPlatform)
  .settings(
    Seq(
      name := "amf-antlr-syntax"
    ))
  .in(file("./amf-antlr-syntax"))
  .settings(commonSettings)
  .dependsOn(apiContract)
  .jvmSettings(
    libraryDependencies += "org.scala-js"                      %% "scalajs-stubs"         % scalaJSVersion % "provided",
    libraryDependencies += antlrv4LibJVM,
    Compile / packageDoc / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-antlr-syntax-javadoc.jar",
    Compile / packageBin / mappings += file("amf-apicontract.versions") -> "amf-apicontract.versions"
  )
  .jsSettings(
    libraryDependencies += antlrv4LibJS,
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    Compile / fullOptJS / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-antlr-syntax.js",
    scalacOptions += "-P:scalajs:suppressExportDeprecations"
  )
  .disablePlugins(SonarPlugin)

lazy val antlrJVM =
  antlr.jvm
    .in(file("./amf-antlr-syntax/jvm"))
    .sourceDependency(antlrv4JVMRef, antlrv4LibJVM)
lazy val antlrJS =
  antlr.js
    .in(file("./amf-antlr-syntax/js"))
    .disablePlugins(SonarPlugin, ScalaJsTypingsPlugin)
    .sourceDependency(antlrv4JSRef, antlrv4LibJS)

/** **********************************************
  * AMF-GRPC
  * ********************************************* */

lazy val grpc = crossProject(JSPlatform, JVMPlatform)
  .settings(
    Seq(
      name := "amf-grpc"
    ))
  .in(file("./amf-grpc"))
  .settings(commonSettings)
  .dependsOn(apiContract, antlr)
  .jvmSettings(
    libraryDependencies += "org.scala-js"                      %% "scalajs-stubs"         % scalaJSVersion % "provided",
    Compile / packageDoc / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-grpc-javadoc.jar",
    Compile / packageBin / mappings += file("amf-apicontract.versions") -> "amf-apicontract.versions"
  )
  .jsSettings(
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    Compile / fullOptJS / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-grpc.js",
    scalacOptions += "-P:scalajs:suppressExportDeprecations"
  )
  .disablePlugins(SonarPlugin)

lazy val grpcJVM =
  grpc.jvm
    .in(file("./amf-grpc/jvm"))
lazy val grpcJS =
  grpc.js
    .in(file("./amf-grpc/js"))
    .disablePlugins(SonarPlugin, ScalaJsTypingsPlugin)

/** **********************************************
  * AMF-GRAPHQL
  * ********************************************* */

lazy val graphql = crossProject(JSPlatform, JVMPlatform)
  .settings(
    Seq(
      name := "amf-graphql"
    ))
  .in(file("./amf-graphql"))
  .settings(commonSettings)
  .dependsOn(apiContract, antlr)
  .jvmSettings(
    libraryDependencies += "org.scala-js"                      %% "scalajs-stubs"         % scalaJSVersion % "provided",
    Compile / packageDoc / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-graphql-javadoc.jar",
    Compile / packageBin / mappings += file("amf-apicontract.versions") -> "amf-apicontract.versions"
  )
  .jsSettings(
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    Compile / fullOptJS / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-graphql.js",
    scalacOptions += "-P:scalajs:suppressExportDeprecations"
  )
  .disablePlugins(SonarPlugin)

lazy val graphqlJVM =
  graphql.jvm
    .in(file("./amf-graphql/jvm"))
lazy val graphqlJS =
  graphql.js
    .in(file("./amf-graphql/js"))
    .disablePlugins(SonarPlugin, ScalaJsTypingsPlugin)

/** **********************************************
  * AMF CLI
  * ********************************************* */
lazy val cli = crossProject(JSPlatform, JVMPlatform)
  .settings(name := "amf-cli")
  .settings(fullRunTask(defaultProfilesGenerationTask, Compile, "amf.tasks.validations.ValidationProfileExporter"))
  .dependsOn(grpc,graphql)
  .in(file("./amf-cli"))
  .settings(commonSettings)
  .settings(
    libraryDependencies += "com.github.scopt" %%% "scopt" % "3.7.0"
  )
  .jvmSettings(
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"      % scalaJSVersion % "provided",
    libraryDependencies += "org.reflections"        % "reflections"         % "0.9.12",
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
    Compile / mainClass := Some("amf.cli.client.Main"),
    Compile / packageBin / packageOptions += Package.ManifestAttributes("Automatic-Module-Name" → "org.mule.amf"),
    Compile / packageBin / mappings += file("amf-apicontract.versions") -> "amf-apicontract.versions",
    assembly / aggregate := true,
    assembly / test := {},
    assembly / mainClass := Some("amf.cli.client.Main"),
    assembly / assemblyOutputPath := file(s"./amf-${version.value}.jar"),
    assembly / assemblyMergeStrategy := {
      case x if x.contains("commons/logging") => MergeStrategy.discard
      case x if x.endsWith("JS_DEPENDENCIES") => MergeStrategy.discard
      case x if x.contains("javax/annotation") => MergeStrategy.discard
      case PathList(ps @ _*) if ps.last endsWith "module-info.class" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith "JS_DEPENDENCIES" => MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
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
    Compile / fullOptJS / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-client-module.js",
  )
  .jsSettings(TypingGenerationSettings.settings:_*)
  .disablePlugins(SonarPlugin)

lazy val cliJVM = cli.jvm.in(file("./amf-cli/jvm"))
  .sourceDependency(rdfJVMRef % "test", rdfLibJVM % "test")
lazy val cliJS = cli.js.in(file("./amf-cli/js"))
  .sourceDependency(rdfJSRef % "test", rdfLibJS % "test")

// Tasks

val buildJS = TaskKey[Unit]("buildJS", "Build npm module")
buildJS := {
  val _ = (cliJS / Compile / fullOptJS).value
  "./amf-cli/js/build-scripts/create-bundle.sh" !
}


/** **********************************************
  * AD-HOC CLI
  * ********************************************* */

lazy val adhocCli = (project in file("adhoc-cli"))
  .settings(
    version := "0.1-SNAPSHOT",
    publishTo := Some(snapshots)
  )
  .settings(
    assembly / aggregate := true,
    assembly / test := {},
    assembly / mainClass := Some("amf.adhoc.cli.Main"),
    assembly / assemblyOutputPath := file("amf.jar"),
    assembly / assemblyMergeStrategy := {
      case x if x.contains("commons/logging") => MergeStrategy.discard
      case x if x.endsWith("JS_DEPENDENCIES") => MergeStrategy.discard
      case x if x.contains("javax/annotation") => MergeStrategy.discard
      case PathList(ps @ _*) if ps.last endsWith "module-info.class" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith "JS_DEPENDENCIES" => MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },
    assembly / artifact := {
      val art = (assembly / artifact).value
      art.withClassifier(Some("assembly"))
    },
    addArtifact(assembly / artifact, assembly))
  .dependsOn(apiContractJVM)
  .disablePlugins(SonarPlugin)


addCommandAlias(
  "buildCommandLine",
  "; clean; cliJVM/assembly"
)

ThisBuild / libraryDependencies ++= Seq(
  compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.1" cross CrossVersion.constant("2.12.11")),
  "com.github.ghik" % "silencer-lib" % "1.7.1" % Provided cross CrossVersion.constant("2.12.11")
)
