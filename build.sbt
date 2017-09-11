import org.scalajs.core.tools.linker.ModuleKind

name := "AMF"

scalaVersion in ThisBuild := "2.12.2"

val repository = sys.env.getOrElse("NEXUS_REPOSITORY", "https://nexus.build.msap.io/nexus")

lazy val root = project
  .in(file("."))
  .aggregate(amfJS, amfJVM)

lazy val amf = crossProject
  .in(file("."))
  .settings(
    name := "amf",
    organization := "org.mulesoft",
    version := "0.0.1-SNAPSHOT",

    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "test",

    scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-Xfatal-warnings"),
    scalacOptions ++= Seq("-encoding", "utf-8"),

    sonarProperties ++= Map(
      "sonar.host.url"             -> s"${sys.env.getOrElse("SONAR_URL", "http://es.sandbox.msap.io/sonar")}",
      "sonar.login"                -> s"${sys.env.getOrElse("SONAR_USR", "")}",
      "sonar.password"             -> s"${sys.env.getOrElse("SONAR_PSW", "")}",
      "sonar.projectKey"           -> "mulesoft.amf",
      "sonar.projectName"          -> "AMF",
      "sonar.projectVersion"       -> "0.1",
      "sonar.sourceEncoding"       -> "UTF-8",
      "sonar.github"               -> "repository=mulesoft/amf",
      "sonar.tests"                -> "shared/src/test/scala",
      "sonar.sources"              -> "shared/src/main/scala",
      "AMF.sonar.sources"          -> "shared/src/main/scala",
      "sonar.scoverage.reportPath" -> "amf-jvm/target/scala-2.12/scoverage-report/scoverage.xml"
    )
  )
  .jvmSettings(
    publishTo := Some(
      "snapshots" at s"$repository/content/repositories/ci-snapshots/"),
    credentials ++= Seq(
      Credentials(
        "Sonatype Nexus Repository Manager",
        new java.net.URL(repository).getHost,
        sys.env.getOrElse("NEXUS_USR", ""),
        sys.env.getOrElse("NEXUS_PSW", "")
      )
    ),
    addArtifact(artifact in (Compile, assembly), assembly),
    publishArtifact in (Compile, packageBin) := false,
    libraryDependencies += "org.scala-js"           %% "scalajs-stubs"          % scalaJSVersion % "provided",
    libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    test in assembly := {},
    assemblyOutputPath in assembly := baseDirectory.value / "target" / "artifact" / "amf.jar",
    artifactPath in (Compile, packageDoc) := baseDirectory.value / "target" / "artifact" / "amf-javadoc.jar"
  )
  .jsSettings(
    publish := {
      "./amf-js/build-scripts/deploy-develop.sh".!
    },
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
    scalaJSModuleKind := ModuleKind.CommonJSModule
  )

lazy val amfJVM = amf.jvm.in(file("amf-jvm"))
lazy val amfJS  = amf.js.in(file("amf-js"))

lazy val module = crossProject
  .in(file("amf-js/js-module"))
  .dependsOn(amf)
  .jsSettings(
    jsSettings("amf-module.js", ModuleKind.CommonJSModule): _*
  )
  .js

lazy val browser = crossProject
  .in(file("amf-js/js-browser"))
  .dependsOn(amf)
  .jsSettings(
    jsSettings("amf-browser.js", ModuleKind.NoModule): _*
  )
  .js

def jsSettings(fileName: String, kind: ModuleKind): Array[Def.SettingsDefinition] = Array(
  artifactPath in (Compile, fullOptJS) := baseDirectory.value / ".." / ".." / "target" / "artifact" / fileName,
  scalaJSOutputMode := org.scalajs.core.tools.linker.backend.OutputMode.ECMAScript6,
  scalaJSModuleKind := kind
)

addCommandAlias("generate", "; clean; generateModuleJS; generateBrowserJS; generateJVM")
addCommandAlias("generateBrowserJS", "; browserJS/fullOptJS")
addCommandAlias("generateModuleJS", "; moduleJS/fullOptJS")
addCommandAlias("generateJVM", "; amfJVM/assembly; amfJVM/packageDoc")
addCommandAlias("publish", "; clean; generateModuleJS; amfJS/publish; amfJVM/publish")
addCommandAlias("sonar", "; amfJVM/sonar")
