package amf.javaparser.org.raml

import _root_.org.mulesoft.common.io.{Fs, SyncFile}
import _root_.org.scalatest.compatible.Assertion
import amf._
import amf.client.parse.DefaultParserErrorHandler
import amf.core.annotations.SourceVendor
import amf.core.emitter.RenderOptions
import amf.core.model.document.{BaseUnit, Document, EncodesModel, Module}
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline.EDITING_PIPELINE
import amf.core.validation.AMFValidationReport
import amf.emit.AMFRenderer
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.document.webapi.resolution.pipelines.AmfEditingPipeline
import amf.plugins.document.webapi.{Oas20Plugin, Oas30Plugin, Raml08Plugin, Raml10Plugin}
import amf.resolution.ResolutionTest
import amf.validations.PayloadValidations.ExampleValidationErrorSpecification

import scala.concurrent.Future

trait ModelValidationTest extends DirectoryTest {

  override def ignorableExtention: String = ".ignore"

  override def runDirectory(d: String): Future[(String, Boolean)] = {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(s"file://${d + inputFileName}",
                           platform,
                           RamlYamlHint,
                           eh = DefaultParserErrorHandler.withRun()).build()
      report <- { validation.validate(model, profileFromModel(model)) }
      output <- { renderOutput(d, model, report) }
    } yield {
      // we only need to use the platform if there are errors in examples, this is what causes differences due to
      // the different JSON-Schema libraries used in JS and the JVM
      val usePlatform = !report.conforms && report.results.exists(
        _.validationId == ExampleValidationErrorSpecification.id)
      (output, usePlatform)
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
    AMFRenderer(transform(model, d, vendor), vendor, RenderOptions()).renderToString

  def transform(unit: BaseUnit, d: String, vendor: Vendor): BaseUnit =
    unit

  private def profileFromModel(unit: BaseUnit): ProfileName = {
    val maybeVendor = Option(unit)
      .collect({ case d: Document => d })
      .flatMap(_.encodes.annotations.find(classOf[SourceVendor]).map(_.vendor))
    maybeVendor match {
      case Some(Raml08) => Raml08Profile
      case _            => RamlProfile
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
    transform(unit, CycleConfig("", "", hintFromTarget(vendor), vendor, d, None, None))

  private def profileFromVendor(vendor: Vendor): ProfileName = {
    vendor match {
      case Raml08        => Raml08Profile
      case Raml | Raml10 => RamlProfile
      case Oas | Oas20   => OasProfile
      case Oas30         => Oas30Profile
      case _             => AmfProfile
    }
  }

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = {
    val res = config.target match {
      case Raml08 =>
        Raml08Plugin.resolve(unit, unit.errorHandler(), EDITING_PIPELINE) // use edition pipeline to avoid remove declarations
      case Raml | Raml10 => Raml10Plugin.resolve(unit, unit.errorHandler(), EDITING_PIPELINE)
      case Oas30         => Oas30Plugin.resolve(unit, unit.errorHandler(), EDITING_PIPELINE)
      case Oas | Oas20   => Oas20Plugin.resolve(unit, unit.errorHandler(), EDITING_PIPELINE)
      case Amf           => AmfEditingPipeline.unhandled.resolve(unit)
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

  def runDirectory(d: String): Future[(String, Boolean)]

  case class directoryResult(outputFile: String, goldenFile: String)

  private def validDir(files: List[SyncFile]): Boolean = {
    val fileNames = files.map(_.name)
    files.nonEmpty && fileNames.contains(inputFileName) && (fileNames.contains(outputFileName) || fileNames.contains(
      outputFileName concat ignorableExtention) || fileNames.contains(outputFileName + s".${platform.name}"))
  }

  private def testFunction(d: String): Future[Assertion] = {
    runDirectory(d).flatMap {
      case (t, usePlatform) =>
        val finalOutputFileName = if (usePlatform) outputFileName + s".${platform.name}" else outputFileName
        writeTemporaryFile(finalOutputFileName)(t)
          .flatMap(assertDifferences(_, s"${d + finalOutputFileName}"))
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
