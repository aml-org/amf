package amf.org.raml.api

import amf.core.remote.RamlYamlHint
import amf.facades.{AMFCompiler, Validation}
import amf.resolution.ResolutionTest
import org.mulesoft.common.io.{Fs, SyncFile}

class ApiModelParserTestCase extends ModelTest {
  override val basePath: String = path
  override def path: String     = "amf-client/shared/src/test/resources/org/raml/api"
}

class TypeToJsonSchemaTest extends ModelTest {
  override val basePath: String = path
  override def path: String     = "amf-client/shared/src/test/resources/org/raml/json_schema"

  override def outputFileName: String = "output.json"
}

class ApiTckTestCase extends ModelTest {
  override val basePath: String = path
  override def path: String     = "amf-client/shared/src/test/resources/org/raml/parser"

  override def outputFileName: String = "output.txt"
}

class Raml08BuilderTestCase extends ModelTest {
  override val basePath: String = path
  override def path: String     = "amf-client/shared/src/test/resources/org/raml/v08/parser"

  override def outputFileName: String = "output.txt"
}

trait ModelTest extends ResolutionTest with DirectoryTest {

  private def golden(d: String)     = d + "/" + outputFileName
  private def errorsFile(d: String) = d + "/" + "error.txt"
  directories.foreach(d => {
    test("ModelTest for dir: " + d) {

//      cycle(inputFileName, outputFileName, RamlYamlHint, Amf, directory = d)

//      for {
//        validation <- Validation(platform).map(_.withEnabledValidation(false))
//        model      <- AMFCompiler(s"file://$d/$inputFileName", platform, RamlYamlHint, validation).build()
//        report     <- validation.validate(model, ProfileNames.RAML, ProfileNames.RAML)
//      } yield {
//        if(report.conforms){
//            //resolve
////          transform(model, ProfileNames)
//            // dump jsonld
//          //parse jsonld
//            //dump raml
//          succeed
//
//        }else{
//          // assert agains error file
//          writeTemporaryFile(errorsFile(d))(report.toString)
//            .flatMap(assertDifferences(_, errorsFile(d)))
//        }
//
//      }
      Validation(platform)
        .map(_.withEnabledValidation(false))
        .flatMap(v => {
          val future = AMFCompiler(s"file://$d/$inputFileName", platform, RamlYamlHint, v).build()
          future
        })
//        .map(bu => {
//          AMFDumper(bu, Raml10, Yaml,GenerationOptions()).dumpToString
//        })
        .map(_ => succeed)
    }
  })

  override def inputFileName: String  = "input.raml"
  override def outputFileName: String = "model.json"

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
