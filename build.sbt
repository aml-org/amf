import Common.snapshots
import NpmOpsPlugin.autoImport._
import sbt.Keys.{libraryDependencies, resolvers}
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtsonar.SonarPlugin.autoImport.sonarProperties

import scala.language.postfixOps
import Versions.versions
import sbtassembly.AssemblyPlugin.autoImport.assembly

val ivyLocal = Resolver.file("ivy", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

name := "amf"

ThisBuild / version      := versions("amf.apicontract")
ThisBuild / organization := "com.github.amlorg"
ThisBuild / scalaVersion := "2.12.15"
ThisBuild / resolvers ++= List(
  ivyLocal,
  Common.releases,
  Common.snapshots,
  Common.public,
  Resolver.mavenLocal,
  Resolver.mavenCentral
)
ThisBuild / credentials ++= Common.credentials()

val npmDeps = List(("ajv", "6.12.6"), ("@aml-org/amf-antlr-parsers", versions("antlr4Version")))

val apiContractModelVersion = settingKey[String]("Version of the AMF API Contract Model").withRank(KeyRanks.Invisible)

publish := {}

jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv()

val commonSettings = Common.settings ++ Common.publish ++ Seq(
  assembly / aggregate := false,
  libraryDependencies ++= Seq(
    "org.mule.common" %%% "scala-common-test" % "0.1.13" % Test,
    "org.slf4j"         % "slf4j-nop"         % "1.7.36" % Test
  ),
  Test / logBuffered := false
)

val amlVersion = versions("amf.aml")

lazy val amlJVMRef = ProjectRef(Common.workspaceDirectory / "amf-aml", "amlJVM")
lazy val amlJSRef  = ProjectRef(Common.workspaceDirectory / "amf-aml", "amlJS")
lazy val amlLibJVM = "com.github.amlorg" %% "amf-aml"        % amlVersion
lazy val amlLibJS  = "com.github.amlorg" %% "amf-aml_sjs1" % amlVersion

lazy val rdfJVMRef = ProjectRef(Common.workspaceDirectory / "amf-aml", "rdfJVM")
lazy val rdfLibJVM = "com.github.amlorg" %% "amf-rdf"        % amlVersion
lazy val rdfJSRef  = ProjectRef(Common.workspaceDirectory / "amf-aml", "rdfJS")
lazy val rdfLibJS  = "com.github.amlorg" %% "amf-rdf_sjs1" % amlVersion

lazy val defaultProfilesGenerationTask = TaskKey[Unit](
  "defaultValidationProfilesGeneration",
  "Generates the validation dialect documents for the standard profiles"
)

/** ********************************************** AMF-Shapes *********************************************
  */
lazy val shapes = crossProject(JSPlatform, JVMPlatform)
  .settings(
    Seq(
      name := "amf-shapes"
    )
  )
  .in(file("./amf-shapes"))
  .settings(commonSettings)
  .jvmSettings(
    libraryDependencies += "org.scala-js"                     %% "scalajs-stubs"          % "1.1.0" % "provided",
    libraryDependencies += "com.github.everit-org.json-schema" % "org.everit.json.schema" % "1.12.2",
    libraryDependencies += "org.json"                          % "json"                   % "20230227",
    Compile / packageDoc / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-shapes-javadoc.jar"
  )
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    Compile / fullOptJS / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-shapes-module.js",
    npmDependencies ++= npmDeps
  )
  .settings(AutomaticModuleName.settings("amf.shapes"))

lazy val shapesJVM =
  shapes.jvm
    .in(file("./amf-shapes/jvm"))
    .sourceDependency(amlJVMRef, amlLibJVM)
    .disablePlugins(SonarPlugin)

lazy val shapesJS =
  shapes.js
    .in(file("./amf-shapes/js"))
    .sourceDependency(amlJSRef, amlLibJS)
    .disablePlugins(SonarPlugin, ScoverageSbtPlugin)
//    .disablePlugins(SonarPlugin, ScalaJsTypingsPlugin, ScoverageSbtPlugin)

/** ********************************************** AMF-Api-contract *********************************************
  */
lazy val apiContract = crossProject(JSPlatform, JVMPlatform)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    Seq(
      name := "amf-api-contract"
    )
  )
  .in(file("./amf-api-contract"))
  .settings(
    commonSettings ++ Seq(
      apiContractModelVersion := versions("amf.model"),
      buildInfoKeys           := Seq[BuildInfoKey](apiContractModelVersion),
      buildInfoPackage        := "amf.apicontract.internal.unsafe"
    )
  )
  .dependsOn(shapes)
  .jvmSettings(
    libraryDependencies += "org.scala-js"   %% "scalajs-stubs" % "1.1.0" % "provided",
    libraryDependencies += "org.reflections" % "reflections"   % "0.10.2"       % Test,
    Compile / packageDoc / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-api-contract-javadoc.jar",
    Compile / packageBin / mappings += file("amf-apicontract.versions") -> "amf-apicontract.versions"
  )
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    Compile / fullOptJS / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-api-contract-module.js",
    npmDependencies ++= npmDeps
  )
  .settings(AutomaticModuleName.settings("amf.apicontract"))

lazy val apiContractJVM =
  apiContract.jvm
    .in(file("./amf-api-contract/jvm"))

lazy val apiContractJS =
  apiContract.js
    .in(file("./amf-api-contract/js"))
    .disablePlugins(SonarPlugin, ScoverageSbtPlugin)
//    .disablePlugins(SonarPlugin, ScalaJsTypingsPlugin, ScoverageSbtPlugin)

/** ********************************************** AMF-ANTLR-SYNTAX *********************************************
  */

lazy val antlrv4JVMRef = ProjectRef(Common.workspaceDirectory / "amf-antlr-ast", "antlrastJVM")
lazy val antlrv4JSRef  = ProjectRef(Common.workspaceDirectory / "amf-antlr-ast", "antlrastJS")
val antlr4Version      = versions("antlr4Version")
lazy val antlrv4LibJVM = "com.github.amlorg" %% "antlr-ast"        % antlr4Version
lazy val antlrv4LibJS  = "com.github.amlorg" %% "antlr-ast_sjs1" % antlr4Version

lazy val antlr = crossProject(JSPlatform, JVMPlatform)
  .settings(
    Seq(
      name := "amf-antlr-syntax"
    )
  )
  .in(file("./amf-antlr-syntax"))
  .settings(commonSettings)
  .dependsOn(apiContract)
  .jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided",
    libraryDependencies += antlrv4LibJVM,
    Compile / packageDoc / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-antlr-syntax-javadoc.jar",
    Compile / packageBin / mappings += file("amf-apicontract.versions") -> "amf-apicontract.versions"
  )
  .jsSettings(
    libraryDependencies += antlrv4LibJS,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    Compile / fullOptJS / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-antlr-syntax.js",
    npmDependencies ++= npmDeps
  )
  .settings(AutomaticModuleName.settings("amf.antlr"))

lazy val antlrJVM =
  antlr.jvm
    .in(file("./amf-antlr-syntax/jvm"))
    .sourceDependency(antlrv4JVMRef, antlrv4LibJVM)
    .disablePlugins(SonarPlugin)

lazy val antlrJS =
  antlr.js
    .in(file("./amf-antlr-syntax/js"))
    .sourceDependency(antlrv4JSRef, antlrv4LibJS)
    .disablePlugins(SonarPlugin, ScoverageSbtPlugin)
//    .disablePlugins(SonarPlugin, ScalaJsTypingsPlugin, ScoverageSbtPlugin)

/** ********************************************** AMF-GRPC *********************************************
  */

lazy val grpc = crossProject(JSPlatform, JVMPlatform)
  .settings(
    Seq(
      name := "amf-grpc"
    )
  )
  .in(file("./amf-grpc"))
  .settings(commonSettings)
  .dependsOn(apiContract, antlr)
  .jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided",
    Compile / packageDoc / artifactPath   := baseDirectory.value / "target" / "artifact" / "amf-grpc-javadoc.jar",
    Compile / packageBin / mappings += file("amf-apicontract.versions") -> "amf-apicontract.versions"
  )
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    Compile / fullOptJS / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-grpc.js",

    npmDependencies ++= npmDeps
  )
  .settings(AutomaticModuleName.settings("amf.grpc"))

lazy val grpcJVM =
  grpc.jvm
    .in(file("./amf-grpc/jvm"))
    .disablePlugins(SonarPlugin)

lazy val grpcJS =
  grpc.js
    .in(file("./amf-grpc/js"))
    .disablePlugins(SonarPlugin, ScoverageSbtPlugin)
//    .disablePlugins(SonarPlugin, ScalaJsTypingsPlugin, ScoverageSbtPlugin)

/** ********************************************** AMF-GRAPHQL *********************************************
  */

lazy val graphql = crossProject(JSPlatform, JVMPlatform)
  .settings(
    Seq(
      name := "amf-graphql"
    )
  )
  .in(file("./amf-graphql"))
  .settings(commonSettings)
  .dependsOn(apiContract, antlr)
  .jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided",
    Compile / packageDoc / artifactPath   := baseDirectory.value / "target" / "artifact" / "amf-graphql-javadoc.jar",
    Compile / packageBin / mappings += file("amf-apicontract.versions") -> "amf-apicontract.versions"
  )
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    Compile / fullOptJS / artifactPath := baseDirectory.value / "target" / "artifact" / "amf-graphql.js",
    npmDependencies ++= npmDeps
  )
  .settings(AutomaticModuleName.settings("amf.graphql"))

lazy val graphqlJVM =
  graphql.jvm
    .in(file("./amf-graphql/jvm"))
    .disablePlugins(SonarPlugin)

lazy val graphqlJS =
  graphql.js
    .in(file("./amf-graphql/js"))
    .disablePlugins(SonarPlugin, ScoverageSbtPlugin)
//    .disablePlugins(SonarPlugin, ScalaJsTypingsPlugin, ScoverageSbtPlugin)

/** ********************************************** AMF CLI *********************************************
  */
lazy val cli = crossProject(JSPlatform, JVMPlatform)
  .settings(name := "amf-cli")
  .settings(fullRunTask(defaultProfilesGenerationTask, Compile, "amf.tasks.validations.ValidationProfileExporter"))
  .dependsOn(grpc, graphql)
  .in(file("./amf-cli"))
  .settings(commonSettings)
  .settings(
    libraryDependencies += "com.github.scopt" %%% "scopt" % "4.0.0"
  )
  .jvmSettings(
    libraryDependencies += "org.scala-js"   %% "scalajs-stubs" % "1.1.0" % "provided",
    libraryDependencies += "org.reflections" % "reflections"   % "0.10.2",
    Compile / mainClass                     := Some("amf.cli.client.Main"),
    Compile / packageBin / packageOptions += Package.ManifestAttributes("Automatic-Module-Name" → "org.mule.amf"),
    Compile / packageBin / mappings += file("amf-apicontract.versions") -> "amf-apicontract.versions",
    assembly / aggregate                                                := true,
    assembly / test                                                     := {},
    assembly / mainClass                                                := Some("amf.cli.client.Main"),
    assembly / assemblyOutputPath                                       := file(s"./amf-${version.value}.jar"),
    assembly / assemblyMergeStrategy := {
      case x if x.contains("commons/logging")                        => MergeStrategy.discard
      case x if x.endsWith("JS_DEPENDENCIES")                        => MergeStrategy.discard
      case x if x.contains("javax/annotation")                       => MergeStrategy.discard
      case PathList(ps @ _*) if ps.last endsWith "module-info.class" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith "JS_DEPENDENCIES"   => MergeStrategy.discard
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
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    Compile / fullOptJS / artifactPath := baseDirectory.value / "amf.js",
    npmDependencies ++= npmDeps,
    jsDependencies += rdfLibJS / "shacl.js" % "test"
  )
  .settings(AutomaticModuleName.settings("amf.cli"))
//  .jsSettings(TypingGenerationSettings.settings: _*)

lazy val cliJVM = cli.jvm.in(file("./amf-cli/jvm")).sourceDependency(rdfJVMRef % "test", rdfLibJVM % "test")

lazy val cliJS = cli.js
  .in(file("./amf-cli/js"))
  .sourceDependency(rdfJSRef % "test", rdfLibJS % "test")
  .disablePlugins(SonarPlugin, ScoverageSbtPlugin)
  .enablePlugins(JSDependenciesPlugin)

/** ********************************************** AD-HOC CLI *********************************************
  */

lazy val adhocCli = (project in file("adhoc-cli"))
  .settings(
    version                                    := "0.1-SNAPSHOT",
    publishTo                                  := Some(snapshots),
    libraryDependencies += "com.github.amlorg" %% "amf-validation-profile-dialect" % versions("amf.validation.profile.dialect"),
    libraryDependencies += "com.github.amlorg" %% "amf-validation-report-dialect"  % versions("amf.validation.report.dialect"),
    libraryDependencies += "commons-io"         % "commons-io"                     % "2.11.0",
    libraryDependencies += "org.mule.common"  %%% "scala-common-test"              % "0.1.13" % Test
  )
  .settings(
    assembly / aggregate          := true,
    assembly / test               := {},
    assembly / mainClass          := Some("amf.adhoc.cli.Main"),
    assembly / assemblyOutputPath := file("amf.jar"),
    assembly / assemblyMergeStrategy := {
      case x if x.contains("commons/logging")                        => MergeStrategy.discard
      case x if x.endsWith("JS_DEPENDENCIES")                        => MergeStrategy.discard
      case x if x.contains("javax/annotation")                       => MergeStrategy.discard
      case PathList(ps @ _*) if ps.last endsWith "module-info.class" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith "JS_DEPENDENCIES"   => MergeStrategy.discard
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
  .dependsOn(apiContractJVM)
  .disablePlugins(SonarPlugin, NpmOpsPlugin, ScoverageSbtPlugin)

addCommandAlias(
  "buildCommandLine",
  "; clean; cliJVM/assembly"
)

ThisBuild / libraryDependencies ++= Seq(
  compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.6" cross CrossVersion.constant("2.12.15")),
  "com.github.ghik" % "silencer-lib" % "1.7.6" % Provided cross CrossVersion.constant("2.12.15")
)

lazy val sonarUrl   = sys.env.getOrElse("SONAR_SERVER_URL", "Not found url.")
lazy val sonarToken = sys.env.getOrElse("SONAR_SERVER_TOKEN", "Not found token.")
lazy val branch     = sys.env.getOrElse("BRANCH_NAME", "develop")

sonarProperties ++= Map(
  "sonar.login"             -> sonarToken,
  "sonar.projectKey"        -> "mulesoft.amf",
  "sonar.projectName"       -> "AMF",
  "sonar.projectVersion"    -> versions("amf.apicontract"),
  "sonar.sourceEncoding"    -> "UTF-8",
  "sonar.github.repository" -> "aml-org/amf",
  "sonar.branch.name"       -> branch,
  "sonar.sources" -> "amf-api-contract/shared/src/main/scala, amf-cli/shared/src/main/scala, amf-grpc/shared/src/main/scala",
  "sonar.tests" -> "amf-api-contract/shared/src/test/scala, amf-cli/shared/src/test/scala, amf-grpc/shared/src/test/scala",
  "sonar.userHome" -> "${buildDir}/.sonar"
)

Global / concurrentRestrictions += Tags.limit(Tags.Untagged, 1)
