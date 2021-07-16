package amf.shapes.internal.validation.jsonschema

import amf.core.client.common.validation.{ProfileName, SeverityLevels}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.shapes.internal.validation.definitions.ShapePayloadValidations.ExampleValidationErrorSpecification

trait ValidationProcessor {
  type Return
  def processResults(r: Seq[AMFValidationResult]): Return
  def processException(r: Throwable, element: Option[DomainElement]): Return
  def keepResults(r: Seq[AMFValidationResult]): Unit
}

object BooleanValidationProcessor extends ValidationProcessor {

  override type Return = Boolean
  override def processResults(r: Seq[AMFValidationResult]): Return =
    !r.exists(_.severityLevel == SeverityLevels.VIOLATION)
  override def processException(r: Throwable, element: Option[DomainElement]): Return = false
  override def keepResults(r: Seq[AMFValidationResult]): Unit                         = Unit
}

trait ReportValidationProcessor extends ValidationProcessor {
  val profileName: ProfileName
  protected var intermediateResults: Seq[AMFValidationResult]
  override type Return = AMFValidationReport
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
          ))
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
          ))
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
          ))
    }
  }
}
