package amf.javaparser.org.raml

import amf._
import amf.core.annotations.SourceVendor
import amf.core.emitter.RenderOptions
import amf.core.model.document.{BaseUnit, Document, EncodesModel, Module}
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline.EDITING_PIPELINE
import amf.core.validation.AMFValidationReport
import amf.facades.{AMFCompiler, AMFRenderer, Validation}
import amf.plugins.document.webapi.resolution.pipelines.AmfEditingPipeline
import amf.plugins.document.webapi.{OAS20Plugin, OAS30Plugin, RAML08Plugin, RAML10Plugin}
import amf.resolution.ResolutionTest
import _root_.org.mulesoft.common.io.{Fs, SyncFile}
import _root_.org.scalatest.compatible.Assertion

import scala.concurrent.Future

trait ModelValidationTest extends DirectoryTest {

  override def ignorableExtention: String = ".ignore"

  override def runDirectory(d: String): Future[String] = {
    for {
      validation <- Validation(platform)
      model      <- AMFCompiler(s"file://${d + inputFileName}", platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, profileFromModel(model))
      output     <- renderOutput(d, model, report)
    } yield {
      output
    }
  }

  private def renderOutput(d: String, model: BaseUnit, report: AMFValidationReport): Future[String] = {
    if (report.conforms) {
      val vendor = target(model)
      render(model, d, vendor)
    } else {
      val ordered = report.results.sorted
      Future.successful(report.copy(results = ordered).toString)
    }
  }

  def render(model: BaseUnit, d: String, vendor: Vendor): Future[String] =
    AMFRenderer(transform(model, d, vendor), vendor, vendor.defaultSyntax, RenderOptions()).renderToString

  def transform(unit: BaseUnit, d: String, vendor: Vendor): BaseUnit =
    unit

  private def profileFromModel(unit: BaseUnit): ProfileName = {
    val maybeVendor = Option(unit)
      .collect({ case d: Document => d })
      .flatMap(_.encodes.annotations.find(classOf[SourceVendor]).map(_.vendor))
    maybeVendor match {
      case Some(Raml08) => RAML08Profile
      case _            => RAMLProfile
    }
  }

  val defaultTarget: Vendor = Raml10

  def target(model: BaseUnit): Vendor = model match {
    case d: EncodesModel =>
      d.encodes.annotations
        .find(classOf[SourceVendor])
        .map(_.vendor)
        .getOrElse(Raml10)
    case m: Module =>
      m.annotations
        .find(classOf[SourceVendor])
        .map(_.vendor)
        .getOrElse(Raml10)
    case _ => Raml10
  }
}

trait ModelResolutionTest extends ModelValidationTest {

  override def transform(unit: BaseUnit, d: String, vendor: Vendor): BaseUnit =
    transform(unit, CycleConfig("", "", hintFromTarget(vendor), vendor, d))

  private def profileFromVendor(vendor: Vendor): ProfileName = {
    vendor match {
      case Raml08        => RAML08Profile
      case Raml | Raml10 => RAMLProfile
      case Oas | Oas2    => OASProfile
      case Oas3          => OAS3Profile
      case _             => AMFProfile
    }
  }

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = {
    val res = config.target match {
      case Raml08        => RAML08Plugin.resolve(unit, EDITING_PIPELINE) // use edition pipeline to avoid remove declarations
      case Raml | Raml10 => RAML10Plugin.resolve(unit, EDITING_PIPELINE)
      case Oas3          => OAS30Plugin.resolve(unit, EDITING_PIPELINE)
      case Oas | Oas2    => OAS20Plugin.resolve(unit, EDITING_PIPELINE)
      case Amf           => new AmfEditingPipeline(unit).resolve()
      case target        => throw new Exception(s"Cannot resolve $target")
      //    case _ => unit
    }
    res
  }

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
