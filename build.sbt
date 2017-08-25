import org.scalajs.core.tools.linker.ModuleKind

name := "AMF"

scalaVersion in ThisBuild := "2.12.2"
lazy val root = project
  .in(file("."))
  .aggregate(amfJS, amfJVM)

lazy val amf = crossProject
  .in(file("."))
  .settings(
    name := "amf",
    organization := "org.mulesoft",
    version := "0.0.1-SNAPSHOT",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  )
  .jvmSettings(
    publishTo := Some("snapshots" at "https://repository-master.mulesoft.org/nexus/content/repositories/ci-snapshots/"),
    credentials ++= Seq(
      Credentials("Sonatype Nexus Repository Manager",
                  "repository-master.mulesoft.org",
                  sys.env.getOrElse("NEXUS_USER", ""),
                  sys.env.getOrElse("NEXUS_PASS", "")),
      Credentials("Sonatype Nexus Repository Manager",
                  "repository.mulesoft.org",
                  sys.env.getOrElse("NEXUS_USER", ""),
                  sys.env.getOrElse("NEXUS_PASS", ""))
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

addCommandAlias("generate", "; clean; generateJS; generateJVM")
addCommandAlias("generateJS", "; moduleJS/fullOptJS; browserJS/fullOptJS")
addCommandAlias("generateJVM", "; amfJVM/assembly; amfJVM/packageDoc")
addCommandAlias("publish", "; clean; generateJS; amfJS/publish; amfJVM/publish")
