package amf.javaparser.org.raml

import amf.io.FunSuiteCycleTests
import amf.resolution.ResolutionTest
import org.mulesoft.common.io.{Fs, SyncFile}
import org.scalatest.compatible.Assertion

import scala.concurrent.Future

trait DirectoryTest extends FunSuiteCycleTests {

  directories.foreach(d => {
    if (ignoreDir(d)) {
      ignore("DirectoryTest for dir: " + d) {
        runTest(d)
      }
    } else {
      test("DirectoryTest for dir: " + d) {
        runTest(d)
      }
    }
  })

  def path: String
  def inputFileName: String
  def outputFileName: String
  def ignorableExtension: String
  def directories: List[String] = getDirectoriesWithFiles(Fs.syncFile(path)).map(_.path + "/")

  /** returns a list of contained directories including the given one if contains the corresponding files */
  private def getDirectoriesWithFiles(directory: SyncFile): List[SyncFile] = {
    val (files, directories) = directory.list.map(l => Fs.syncFile(directory.path + "/" + l)).partition(_.isFile)
    val sons                 = directories.flatMap(d => getDirectoriesWithFiles(d)).toList
    val result =
      if (validDir(files.toList)) List(directory) ++ sons
      else sons
    result
  }

  def runDirectory(d: String): Future[(String, Boolean)]

  case class directoryResult(outputFile: String, goldenFile: String)

  private def validDir(files: List[SyncFile]): Boolean = {
    val fileNames = files.map(_.name)
    files.nonEmpty && fileNames.contains(inputFileName) && (fileNames.contains(outputFileName) || fileNames.contains(
      outputFileName concat ignorableExtension) || fileNames.contains(outputFileName + s".${platform.name}"))
  }

  private def runTest(d: String): Future[Assertion] = {
    runDirectory(d).flatMap {
      case (t, usePlatform) =>
        val finalOutputFileName = if (usePlatform) outputFileName + s".${platform.name}" else outputFileName
        writeTemporaryFile(finalOutputFileName)(t).flatMap(assertDifferences(_, s"${d + finalOutputFileName}"))
    }
  }

  protected def ignoreDir(d: String): Boolean =
    Fs.syncFile(d).list.exists(_.equals(outputFileName concat ignorableExtension))
}
