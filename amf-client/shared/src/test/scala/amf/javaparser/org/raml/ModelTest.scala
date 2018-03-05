package amf.javaparser.org.raml

import amf.ProfileNames
import amf.core.annotations.SourceVendor
import amf.core.client.GenerationOptions
import amf.core.model.document.{BaseUnit, EncodesModel}
import amf.core.parser.Position
import amf.core.remote._
import amf.core.validation.AMFValidationResult
import amf.facades.{AMFCompiler, AMFDumper, Validation}
import amf.resolution.ResolutionTest
import org.mulesoft.common.io.{Fs, SyncFile}
import org.scalatest.compatible.Assertion

import scala.concurrent.Future

trait ModelValidationTest extends DirectoryTest {

  override def ignorableExtention: String = ".ignore"

  override def runDirectory(d: String): Future[String] = {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(s"file://${d + inputFileName}", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, profileFromModel(model))
    } yield {
      if (report.conforms) {
        val vendor = target(model)
        AMFDumper(transform(model, d, vendor), vendor, vendor.defaultSyntax, GenerationOptions()).dumpToString
      } else {
        val ordered = report.results.sorted(new Ordering[AMFValidationResult] {
          override def compare(x: AMFValidationResult, y: AMFValidationResult): Int = {
            transform(x).compareTo(transform(y))
          }

          private def transform(r: AMFValidationResult) = r.position.map(_.range.start).getOrElse(Position.ZERO)
        })
        report.copy(results = ordered)
        report.toString
      }
    }
  }

  def transform(unit: BaseUnit, d: String, vendor: Vendor): BaseUnit =
    unit

  private def profileFromModel(unit: BaseUnit): String = {
    unit.annotations.find(classOf[SourceVendor]).map(_.value) match {
      case Some(Raml08.name) => ProfileNames.RAML08
      case _                 => ProfileNames.RAML
    }
  }

  def target(model: BaseUnit): Vendor = model match {
    case d: EncodesModel =>
      d.encodes.annotations
        .find(classOf[SourceVendor])
        .map(_.vendor)
        .getOrElse(throw new Exception("Source vendor annotation not found in model"))
  }
}

trait ModelResolutionTest extends ModelValidationTest {

  override def transform(unit: BaseUnit, d: String, vendor: Vendor): BaseUnit =
    transform(unit, CycleConfig("", "", hintFromTarget(vendor), vendor, d))

  private def hintFromTarget(t: Vendor) = t match {
    case _: Raml => RamlYamlHint
    case Oas     => OasJsonHint
    case _       => AmfJsonHint
  }
}

trait DirectoryTest extends ResolutionTest {

  def path: String
  def inputFileName: String
  def outputFileName: String
  def ignorableExtention: String
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

  def runDirectory(d: String): Future[String]

  case class directoryResult(outputFile: String, goldenFile: String)

  private def validDir(files: List[SyncFile]): Boolean = {
    val fileNames = files.map(_.name)
    files.nonEmpty && fileNames.contains(inputFileName) && (fileNames.contains(outputFileName) || fileNames.contains(
      outputFileName concat ignorableExtention))
  }

  private def testFunction(d: String): Future[Assertion] = {
    runDirectory(d).flatMap { t =>
      writeTemporaryFile(outputFileName)(t)
        .flatMap(assertDifferences(_, s"${d + outputFileName}"))
    }
  }
  directories.foreach(d => {
    if (ignoreDir(d)) {
      ignore("DirectoryTest for dir: " + d) {
        testFunction(d)
      }
    } else {
      test("DirectoryTest for dir: " + d) {
        testFunction(d)
      }
    }
  })

  protected def ignoreDir(d: String): Boolean =
    Fs.syncFile(d).list.exists(_.equals(outputFileName concat ignorableExtention))

}
