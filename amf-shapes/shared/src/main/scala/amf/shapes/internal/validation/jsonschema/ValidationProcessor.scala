package amf.shapes.internal.validation.jsonschema

import amf.core.client.common.validation.{ProfileName, SeverityLevels}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.shapes.internal.validation.definitions.ShapePayloadValidations.ExampleValidationErrorSpecification

trait ValidationProcessor {
  def processResults(r: Seq[AMFValidationResult]): AMFValidationReport
  def processException(r: Throwable, element: Option[DomainElement]): AMFValidationReport
  def keepResults(r: Seq[AMFValidationResult]): Unit
}

trait ReportValidationProcessor extends ValidationProcessor {
  val profileName: ProfileName
  protected var intermediateResults: Seq[AMFValidationResult]
  override def processResults(r: Seq[AMFValidationResult]): AMFValidationReport = {
    val results = r ++ intermediateResults
    AMFValidationReport("http://test.com/payload#validations", profileName, results)
  }

  protected def processCommonException(r: Throwable, element: Option[DomainElement]): Seq[AMFValidationResult] = {
    r match {
      case e: UnknownDiscriminator =>
        Seq(
          AMFValidationResult(
            message = "Unknown discriminator value",
            level = SeverityLevels.VIOLATION,
            targetNode = element.map(_.id).getOrElse(""),
            targetProperty = None,
            validationId = ExampleValidationErrorSpecification.id,
            position = element.flatMap(_.position()),
            location = element.flatMap(_.location()),
            source = e
          )
        )
      case e: InvalidJsonObject =>
        Seq(
          AMFValidationResult(
            message = "Unsupported chars in string value (probably a binary file)",
            level = SeverityLevels.VIOLATION,
            targetNode = element.map(_.id).getOrElse(""),
            targetProperty = None,
            validationId = ExampleValidationErrorSpecification.id,
            position = element.flatMap(_.position()),
            location = element.flatMap(_.location()),
            source = e
          )
        )
      case other =>
        Seq(
          AMFValidationResult(
            message = "Unknown exception thrown in validation",
            level = SeverityLevels.VIOLATION,
            targetNode = element.map(_.id).getOrElse(""),
            targetProperty = None,
            validationId = ExampleValidationErrorSpecification.id,
            position = element.flatMap(_.position()),
            location = element.flatMap(_.location()),
            source = other
          )
        )
    }
  }
}
