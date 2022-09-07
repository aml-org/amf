import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport.{fastOptJS, fullOptJS}
import sbt.Keys.{baseDirectory, sLog}
import sbt.util.Logger
import sbt.{AutoPlugin, Compile, Def, Test, settingKey, taskKey}

import java.io.File
import scala.language.postfixOps
import scala.sys.process.Process

object NpmOpsPlugin extends AutoPlugin {

  override def trigger  = allRequirements
  override def requires = ScalaJSPlugin

  object autoImport {
    val npmLinkDependencies = settingKey[List[String]]("List of npm dependencies to link")
    val npmDependencies = settingKey[List[(String, String)]]("List of npm dependencies name and version")
    val npmInstallDeps  = taskKey[Unit]("Install NPM dependencies if not installed")
    val npmPackageLoc   = settingKey[File]("Path to the location of the packages' 'package.json'")
  }

  import autoImport._
  lazy val npmInstallDepsTask = Def.task {
    val log = sLog.value
    log.info("Installing NPM dependencies...")
    installDepsIfNotAlreadyInstalled(npmDependencies.value, npmPackageLoc.value, log)
    linkDependencies(npmLinkDependencies.value, npmPackageLoc.value, log)
  }

  private def linkDependencies(deps: List[String], npmPackageLoc: File, logger: Logger) = {
    if (deps.nonEmpty) {
      val npmDepsAsString = deps.mkString(" ")
      logger.info(s"Installing NPM dependencies: $npmDepsAsString")
      Process(s"npm install --save-exact $npmDepsAsString", npmPackageLoc) !!
    } else logger.info("Skipping as there aren't any NPM dependencies to link")
  }

  private def installDepsIfNotAlreadyInstalled(
      deps: List[(String, String)],
      packageLoc: File,
      logger: Logger
  ): Unit = {
    val npmDeps = filterOutInstalledPackages(computeNpmFullPackages(deps), packageLoc)
    if (npmDeps.nonEmpty) {
      val npmDepsAsString = npmDeps.mkString(" ")
      logger.info(s"Installing NPM dependencies: $npmDepsAsString")
      Process(s"npm install --save-exact $npmDepsAsString", packageLoc) !!
    } else logger.info("Skipping as NPM dependencies are already installed.")
  }

  private def filterOutInstalledPackages(deps: List[String], packageLoc: File): List[String] = {
    deps.map(dep => (dep, Process(s"npm list $dep", packageLoc) !)).collect {
      case (dep, exitCode) if exitCode != 0 => dep
    }
  }

  private def computeNpmFullPackages(deps: List[(String, String)]): List[String] =
    deps.map(tuple => s"${tuple._1}@${tuple._2}")

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    npmInstallDeps      := npmInstallDepsTask.value,
    npmDependencies     := Nil,
    npmLinkDependencies := Nil,
    npmPackageLoc       := baseDirectory.value,
    Compile / fastOptJS := (Compile / fastOptJS).dependsOn(npmInstallDepsTask).value,
    Compile / fullOptJS := (Compile / fullOptJS).dependsOn(npmInstallDepsTask).value,
    Test / fastOptJS    := (Test / fastOptJS).dependsOn(npmInstallDepsTask).value,
    Test / fullOptJS    := (Test / fullOptJS).dependsOn(npmInstallDepsTask).value
  )
}
