package amf.javaparser.org.raml

import amf.apicontract.client.scala.{AMFConfiguration, WebAPIConfiguration}
import amf.apicontract.internal.transformation.AmfEditingPipeline
import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation._
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document, EncodesModel, Module}
import amf.core.client.scala.transform.TransformationPipelineRunner
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.annotations.SourceVendor
import amf.core.internal.remote.{Raml10YamlHint, _}
import amf.core.internal.validation.CoreValidations.UnresolvedReference
import amf.emit.AMFRenderer
import amf.shapes.internal.validation.definitions.ShapePayloadValidations.ExampleValidationErrorSpecification
import amf.testing.ConfigProvider.configFor
import amf.testing.{ConfigProvider, HintProvider}

import scala.concurrent.Future

// TODO remove default Raml hints and vendors
trait ModelValidationTest extends DirectoryTest {

  def hint: Hint = Raml10YamlHint

  override def ignorableExtension: String = ".ignore"

  override def runDirectory(d: String): Future[(String, Boolean)] = {
    val configuration = WebAPIConfiguration.WebAPI()
    for {
      client      <- Future.successful(configuration.baseUnitClient())
      parseResult <- client.parse(s"file://${d + inputFileName}")
      report      <- configFor(parseResult.rootSpec).baseUnitClient().validate(parseResult.baseUnit)
      unifiedReport <- {
        val parseReport = AMFValidationReport.unknownProfile(parseResult)
        val r =
          if (!parseResult.conforms) parseReport
          else parseReport.merge(report)
        Future.successful(r)
      }
    } yield {
      val output = renderOutput(d, parseResult.baseUnit, unifiedReport, configuration)
      // we only need to use the platform if there are errors in examples, this is what causes differences due to
      // the different JSON-Schema libraries used in JS and the JVM
      val usePlatform = !unifiedReport.conforms && unifiedReport.results.exists(result =>
        result.validationId == ExampleValidationErrorSpecification.id || result.validationId == UnresolvedReference.id)
      (output, usePlatform)
    }
  }

  private def renderOutput(d: String,
                           model: BaseUnit,
                           report: AMFValidationReport,
                           amfConfig: AMFConfiguration): String = {
    if (report.conforms) {
      val vendor = HintProvider.defaultHintFor(target(model))
      render(model, d, vendor, amfConfig)
    } else {
      val ordered = report.results.sorted
      report.copy(results = ordered).toString
    }
  }

  def render(model: BaseUnit, d: String, vendor: Hint, amfConfig: AMFConfiguration): String =
    AMFRenderer(transform(model, d, vendor, amfConfig), vendor, RenderOptions()).renderToString

  def transform(unit: BaseUnit, d: String, vendor: Hint, amfConfig: AMFConfiguration): BaseUnit =
    unit

  private def profileFromModel(unit: BaseUnit): ProfileName = {
    val maybeVendor = Option(unit)
      .collect({ case d: Document => d })
      .flatMap(_.encodes.annotations.find(classOf[SourceVendor]).map(_.vendor))
    maybeVendor match {
      case Some(Raml08)     => Raml08Profile
      case Some(Oas20)      => Oas20Profile
      case Some(Oas30)      => Oas30Profile
      case Some(AsyncApi20) => Async20Profile
      case _                => Raml10Profile
    }
  }

  val defaultTarget: SpecId = Raml10

  def target(model: BaseUnit): SpecId = model match {
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

  override def transform(unit: BaseUnit, d: String, target: Hint, amfConfig: AMFConfiguration): BaseUnit =
    transform(unit, CycleConfig("", "", target, target, d, None, None), amfConfig)

  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = {
    val res = config.renderTarget.vendor match {
      case Raml08 | Raml10 | Oas20 | Oas30 =>
        configFor(config.renderTarget.vendor).baseUnitClient().transform(unit, PipelineId.Editing).baseUnit
      case Amf    => TransformationPipelineRunner(UnhandledErrorHandler).run(unit, AmfEditingPipeline())
      case target => throw new Exception(s"Cannot resolve $target")
    }
    res
  }
}
