package amf.javaparser.org.raml

import amf._
import amf.client.parse.DefaultParserErrorHandler
import amf.core.annotations.SourceVendor
import amf.core.emitter.RenderOptions
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.{BaseUnit, Document, EncodesModel, Module}
import amf.core.remote._
import amf.core.resolution.pipelines.TransformationPipeline.EDITING_PIPELINE
import amf.core.resolution.pipelines.TransformationPipelineRunner
import amf.core.services.RuntimeResolver
import amf.core.validation.AMFValidationReport
import amf.emit.AMFRenderer
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.document.webapi.resolution.pipelines.AmfEditingPipeline
import amf.plugins.features.validation.CoreValidations.UnresolvedReference
import amf.validations.ShapePayloadValidations.ExampleValidationErrorSpecification

import scala.concurrent.Future

// TODO remove default Raml hints and vendors
trait ModelValidationTest extends DirectoryTest {

  def hint: Hint = Raml10YamlHint

  override def ignorableExtension: String = ".ignore"

  override def runDirectory(d: String): Future[(String, Boolean)] = {
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(s"file://${d + inputFileName}", platform, hint, eh = DefaultParserErrorHandler.withRun())
        .build()
      report <- { validation.validate(model, profileFromModel(model)) }
      output <- { renderOutput(d, model, report) }
    } yield {
      // we only need to use the platform if there are errors in examples, this is what causes differences due to
      // the different JSON-Schema libraries used in JS and the JVM
      val usePlatform = !report.conforms && report.results.exists(result =>
        result.validationId == ExampleValidationErrorSpecification.id || result.validationId == UnresolvedReference.id)
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
      case Some(Raml08)     => Raml08Profile
      case Some(Oas20)      => Oas20Profile
      case Some(Oas30)      => Oas30Profile
      case Some(AsyncApi20) => Async20Profile
      case _                => Raml10Profile
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
      case Raml08 => Raml08Profile
      case Raml10 => Raml10Profile
      case Oas20  => Oas20Profile
      case Oas30  => Oas30Profile
      case _      => AmfProfile
    }
  }

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = {
    val res = config.target match {
      case Raml08 | Raml10 | Oas20 | Oas30 =>
        RuntimeResolver.resolve(config.target.name, unit, EDITING_PIPELINE, unit.errorHandler())
      case Amf    => TransformationPipelineRunner(UnhandledErrorHandler).run(unit, AmfEditingPipeline())
      case target => throw new Exception(s"Cannot resolve $target")
      //    case _ => unit
    }
    res
  }

  private def hintFromTarget(t: Vendor) = t match {
    case Raml10 => Raml10YamlHint
    case Raml08 => Raml08YamlHint
    case Oas20  => Oas20JsonHint
    case Oas30  => Oas30JsonHint
    case _      => AmfJsonHint
  }
}
