package amf.plugins.document.webapi.validation

import amf.core.annotations.LexicalInformation
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.remote.Platform
import amf.core.services.RuntimeValidator
import amf.core.validation.{AMFValidationResult, SeverityLevels}
import amf.core.vocabulary.Namespace
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.models.{AnyShape, Example, UnionShape}
import amf.plugins.features.validation.ParserSideValidations

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

class ExamplesValidation(model: BaseUnit, platform: Platform) {

  def validate(): Future[Seq[AMFValidationResult]] = {
    // we find all examples with strict validation, some can be supported if we know how to
    // deal with the media type of the example, if not, they will be collected in the unsupported
    // exaples list
    val (supportedExamples, unsupportedExamples) = findExamples()

    // Unsupported examples are transformed into validation WARNINGS, this can be changed with a profile
    val unsupportedExamplesValidations = unsupportedExamples.map {
      case (_, example, mediaType) => unsupportedExampleReport(example, mediaType)
    }

    // We run regular payload validation for the supported examples
    val listSupportedResults: Seq[Future[Seq[AMFValidationResult]]] = supportedExamples map {
      case (shape, example, mediaType) =>
        validateExample(shape, example, mediaType)
    }
    val futureResult: Future[Seq[AMFValidationResult]] = Future.sequence(listSupportedResults).map(_.flatten)

    // Finally we collect all the results
    futureResult map { supportedResults =>
      supportedResults ++ unsupportedExamplesValidations
    }
  }

  protected def findExamples(): (Seq[(Shape, Example, String)], Seq[(Shape, Example, String)]) = {
    val allExamples: Seq[(Shape, Example, String)] = model.findByType((Namespace.Shapes + "Shape").iri()) flatMap {
      case shape: AnyShape =>
        shape.examples.collect({
          case example: Example if example.fields.entry(ExampleModel.StructuredValue).isDefined =>
            (shape, example, mediaType(example))
        })
      case _ => Nil
    }
    val supportedExamples = allExamples.filter {
      case (_, example, mediaType) =>
        validExample(example, mediaType)
    }
    val unsupportedExamples = allExamples.filter {
      case (_, example, mediaType) =>
        unsupportedExample(example, mediaType)
    }
    (supportedExamples, unsupportedExamples)
  }

  protected def validateExample(shape: Shape, example: Example, mediaType: String): Future[Seq[AMFValidationResult]] = {
    RuntimeValidator.reset()

    try {
      shape match {
        case union: UnionShape =>
          val partial: Seq[Future[Option[AMFValidationResult]]] = union.anyOf.map { s =>
            validateShape(s, example)
          }
          Future
            .sequence(partial)
            .map(_.flatten)
            .map(result => if (result.lengthCompare(union.anyOf.size) == 0) result else Nil)
        case _ =>
          validateShape(shape, example).map(_.toSeq)
      }

    } catch {
      case e: Exception => payloadParsingException(e, example)
    }
  }

  private def validateShape(shape: Shape, example: Example): Future[Option[AMFValidationResult]] = {
    PayloadValidation(platform, shape).validate(example.structuredValue) map { report =>
      if (report.conforms) {
        None
      } else {
        Some(
          new AMFValidationResult(
            message = report.toString, // TODO: provide a better description here
            level = SeverityLevels.VIOLATION,
            targetNode = example.id,
            targetProperty = Some((Namespace.Document + "value").iri()),
            ParserSideValidations.ExampleValidationErrorSpecification.id(),
            position = example.annotations.find(classOf[LexicalInformation]),
            source = example
          )
        )
      }
    }
  }

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

  protected def mediaType(example: Example): String = {
    example.mediaType.option() match {
      case Some(mediaType) => mediaType
      case None            => guessMediaType(example)
    }
  }

  protected def guessMediaType(example: Example): String = {
    example.value.option() match {
      case Some(value) =>
        if (isXml(value)) "application/xml"
        else if (isJson(value)) "application/json"
        else "text/vnd.yaml" // by default, we will try to parse it as YAML
      case None => "*/*"
    }
  }

  protected def isXml(value: String) = value.startsWith("<")

  protected def isJson(value: String) = value.startsWith("{") || value.startsWith("[")
}

object ExamplesValidation {
  def apply(model: BaseUnit, platform: Platform) = new ExamplesValidation(model, platform)
}
