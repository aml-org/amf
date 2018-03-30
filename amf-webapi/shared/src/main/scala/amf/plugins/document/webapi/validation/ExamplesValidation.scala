package amf.plugins.document.webapi.validation

import amf.core.annotations.LexicalInformation
import amf.core.model.document.{BaseUnit, PayloadFragment}
import amf.core.model.domain.Shape
import amf.core.parser.ParserContext
import amf.core.remote.Platform
import amf.core.services.{IgnoreValidationsMerger, PayloadValidator, RuntimeValidator}
import amf.core.validation.{AMFValidationReport, AMFValidationResult, SeverityLevels}
import amf.core.vocabulary.Namespace
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.models.{AnyShape, Example, UnionShape}
import amf.plugins.features.validation.ParserSideValidations

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

class ExamplesValidation(model: BaseUnit, platform: Platform) {

  def validate(): Future[Seq[AMFValidationResult]] = {
    // we find all examples with strict validation
    val examples = findExamples()

    // We run regular payload validation for the supported examples
    val results = examples map {
      case (shape, example) => validateExample(shape, example)
    }

    val futureResult: Future[Seq[AMFValidationResult]] = Future.sequence(results).map(_.flatten)
    futureResult
  }

  protected def findExamples(): Seq[(Shape, Example)] = {
    val allExamples: Seq[(Shape, Example)] = model.findByType((Namespace.Shapes + "Shape").iri()) flatMap {
      case shape: AnyShape =>
        shape.examples.collect({
          case example: Example
              if example.fields
                .entry(ExampleModel.StructuredValue)
                .isDefined && example.strict.option().getOrElse(true) =>
            (shape, example)
        })
      case _ => Nil
    }
    allExamples
  }

  protected def validateExample(shape: Shape, example: Example): Future[Seq[AMFValidationResult]] = {
    RuntimeValidator.nestedValidation(IgnoreValidationsMerger) {
      try {
        validateShape(shape, example).map(_.toSeq)
      } catch {
        case e: Exception => payloadParsingException(e, example)
      }
    }
  }

  private def validateShape(shape: Shape, example: Example): Future[Option[AMFValidationResult]] = {

    val fragment = PayloadFragment(
      example.structuredValue,
      example.mediaType.option().getOrElse(PayloadValidator.guessMediaType(example.value.value())))
    fragment.withRaw(example.value.value())
    PayloadValidator.validate(shape, fragment, SeverityLevels.WARNING) map { report =>
      if (report.results.isEmpty) { // before was conforms, now we have to considerer warnings for not supported plugin
        None
      } else {
        val severity = findSeverity(report)
        Some(
          new AMFValidationResult(
            message = report.toString, // TODO: provide a better description here
            severity,
            targetNode = example.id,
            targetProperty = Some((Namespace.Document + "value").iri()),
            if (severity == SeverityLevels.VIOLATION) ParserSideValidations.ExampleValidationErrorSpecification.id()
            else ParserSideValidations.UnsupportedExampleMediaTypeErrorSpecification.id(),
            position = example.annotations.find(classOf[LexicalInformation]),
            source = example
          )
        )
      }
    }
  }

  private def findSeverity(report: AMFValidationReport): String =
    if (report.results.exists(_.level == SeverityLevels.VIOLATION)) SeverityLevels.VIOLATION
    else SeverityLevels.WARNING

  protected def unsupportedExampleReport(example: Example, mediaType: String): AMFValidationResult = {
    new AMFValidationResult(
      message = "Cannot validate example with unsupported media type " + mediaType,
      level = SeverityLevels.WARNING,
      targetNode = example.id,
      targetProperty = Some((Namespace.Document + "value").iri()),
      ParserSideValidations.UnsupportedExampleMediaTypeErrorSpecification.id(),
      position = example.annotations.find(classOf[LexicalInformation]),
      source = example
    )
  }

  protected def payloadParsingException(exception: Exception, example: Example): Future[Seq[AMFValidationResult]] = {
    val promise = Promise[Seq[AMFValidationResult]]()
    val validationResult = new AMFValidationResult(
      message = "Payload parsing validation error: " + exception.getMessage,
      level = SeverityLevels.VIOLATION,
      targetNode = example.id,
      targetProperty = None,
      ParserSideValidations.ExampleValidationErrorSpecification.id(),
      position = example.annotations.find(classOf[LexicalInformation]),
      source = example
    )
    promise.success(Seq(validationResult))
    promise.future
  }

  /**
    * Example is valid if value type is defined, we need to validate and
    * we support the media type
    *
    * @param example
    * @param mediaType
    * @return
    */
  protected def validExample(example: Example, mediaType: String): Boolean = {
    example.value.option().isDefined &&
    example.strict.option().getOrElse(false) && (
      mediaType.indexOf("json") > -1 ||
      mediaType.indexOf("yaml") > -1
    )
  }

  protected def unsupportedExample(example: Example, mediaType: String): Boolean =
    example.strict.option().getOrElse(false) && !validExample(example, mediaType)

}

object ExamplesValidation {
  def apply(model: BaseUnit, platform: Platform) = new ExamplesValidation(model, platform)
}
