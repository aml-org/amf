package amf.plugins.document.webapi.validation

import amf.document.{BaseUnit, Document}
import amf.domain.Annotation.LexicalInformation
import amf.domain.Example
import amf.domain.extensions.DataNode
import amf.framework.services.RuntimeCompiler
import amf.framework.validation.{AMFValidationResult, SeverityLevels}
import amf.plugins.document.webapi.PayloadPlugin
import amf.remote.Platform
import amf.shape.Shape
import amf.unsafe.TrunkPlatform
import amf.validation._
import amf.validation.model.ParserSideValidations
import amf.vocabulary.Namespace

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
    val listSupportedResults = supportedExamples map {
      case (shape, example, mediaType) =>
        validateExample(shape, example, mediaType)
    }
    val futureResult = Future.sequence(listSupportedResults)

    // Finally we collect all the results
    futureResult map { supportedResults =>
      supportedResults.filter(_.isDefined).map(_.get) ++ unsupportedExamplesValidations
    }
  }

  protected def findExamples(): (Seq[(Shape, Example, String)], Seq[(Shape, Example, String)]) = {
    val allExamples: Seq[(Shape, Example, String)] = model.findByType((Namespace.Shapes + "Shape").iri()) flatMap {
      case shape: Shape =>
        shape.examples.map { example =>
          (shape, example, mediaType(example))
        }
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

  protected def validateExample(shape: Shape,
                                example: Example,
                                mediaType: String): Future[Option[AMFValidationResult]] = {
    val exampleValidation = Validation(platform)
    val overridePlatform = TrunkPlatform(example.value)
    try {
      RuntimeCompiler("http://amfparser.org/test_payload", overridePlatform, mediaType, PayloadPlugin.ID, exampleValidation) flatMap { payload =>
        // we are parsing using Payload hint, this MUST be a payload fragment encoding a data node
        val payloadDataNode = payload.asInstanceOf[Document].encodes.asInstanceOf[DataNode]
        PayloadValidation(platform, shape).validate(payloadDataNode) map { report =>
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
                position = example.annotations.find(classOf[LexicalInformation])
              )
            )
          }
        }
      }
    } catch {
      case e: Exception => payloadParsingException(e, example)
    }
  }

  protected def unsupportedExampleReport(example: Example, mediaType: String): AMFValidationResult = {
    new AMFValidationResult(
      message = "Cannot validate example with unsupported media type " + mediaType,
      level = SeverityLevels.WARNING,
      targetNode = example.id,
      targetProperty = Some((Namespace.Document + "value").iri()),
      ParserSideValidations.UnsupportedExampleMediaTypeErrorSpecification.id(),
      position = example.annotations.find(classOf[LexicalInformation])
    )
  }

  protected def payloadParsingException(exception: Exception, example: Example): Future[Option[AMFValidationResult]] = {
    val promise = Promise[Option[AMFValidationResult]]()
    val validationResult = new AMFValidationResult(
      message = "Payload parsing validation error: " + exception.getMessage,
      level = SeverityLevels.VIOLATION,
      targetNode = example.id,
      targetProperty = None,
      ParserSideValidations.ExampleValidationErrorSpecification.id(),
      position = example.annotations.find(classOf[LexicalInformation])
    )
    promise.success(Some(validationResult))
    promise.future
  }

  /**
    * Example is valid if value type is defined, we need to validate and
    * we support the media type
    * @param example
    * @param mediaType
    * @return
    */
  protected def validExample(example: Example, mediaType: String): Boolean = {
    Option(example.value).isDefined &&
    Option(example.strict).getOrElse(false) && (
      mediaType.indexOf("json") > -1 ||
      mediaType.indexOf("yaml") > -1
    )
  }

  protected def unsupportedExample(example: Example, mediaType: String): Boolean =
    Option(example.strict).getOrElse(false) && !validExample(example, mediaType)

  protected def mediaType(example: Example): String = {
    Option(example.mediaType) match {
      case Some(mediaType) => mediaType
      case None            => guessMediaType(example)
    }
  }

  protected def guessMediaType(example: Example): String = {
    Option(example.value) match {
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
