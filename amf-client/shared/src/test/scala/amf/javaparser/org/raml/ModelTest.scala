package amf.javaparser.org.raml

import amf.core.client.GenerationOptions
import amf.core.remote.{Raml10, RamlYamlHint}
import amf.core.remote.Syntax.Yaml
import amf.facades.{AMFCompiler, AMFDumper, Validation}
import amf.resolution.ResolutionTest
import org.mulesoft.common.io.{Fs, SyncFile}

trait ModelTest extends ResolutionTest with DirectoryTest {

  private def golden(d: String)     = d + "/" + outputFileName
  private def errorsFile(d: String) = d + "/" + "error.txt"
  directories.foreach(d => {
    test("ModelTest for dir: " + d) {

      Validation(platform)
        .map(_.withEnabledValidation(false))
        .flatMap(v => {
          val future = AMFCompiler(s"file://$d/$inputFileName", platform, RamlYamlHint, v).build()
          future
            //            .map(bu => {
            //              AMFDumper(bu, Amf, Json, GenerationOptions()).dumpToString
            //            })
            //            .flatMap(s => {
            //              AMFCompiler(s"file://$d/amf-model.jsonld", TrunkPlatform(s, Some(platform)), AmfJsonHint, v).build()
            //            })
            .map(bu => {
            AMFDumper(bu, Raml10, Yaml, GenerationOptions()).dumpToString
          })
            .flatMap(s => {
              writeTemporaryFile(outputFileName)(s)
            })
            .flatMap(assertDifferences(_, s"$d/$outputFileName"))
        })
      //        .map(Tests.checkDiffIgnoreAllSpaces)
    }

  })

  override def inputFileName: String  = "input.raml"
  override def outputFileName: String = "out.raml"

  override protected def validDir(files: List[SyncFile]): Boolean = {
    val fileNames = files.map(_.name)
    files.nonEmpty && fileNames.contains(inputFileName) && fileNames.contains(outputFileName)
  }
}

trait DirectoryTest {

  def path: String
  def inputFileName: String
  def outputFileName: String
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

  protected def validDir(files: List[SyncFile]): Boolean
}
