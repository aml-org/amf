package amf.plugins.document.webapi.validation.remote
import amf.ProfileName
import amf.core.model.domain.DomainElement
import amf.core.validation.{AMFValidationReport, AMFValidationResult, SeverityLevels}
import amf.plugins.features.validation.ParserSideValidations.ExampleValidationErrorSpecification

trait ValidationProcessor {
  type Return
  def processResults(r: Seq[AMFValidationResult]): Return
  def processException(r: Throwable, element: Option[DomainElement]): Return
}

object BooleanValidationProcessor extends ValidationProcessor {

  override type Return = Boolean
  override def processResults(r: Seq[AMFValidationResult]): Return =
    !r.exists(_.level == SeverityLevels.VIOLATION)
  override def processException(r: Throwable, element: Option[DomainElement]): Return = false
}

trait ReportValidationProcessor extends ValidationProcessor {
  val profileName: ProfileName
  override type Return = AMFValidationReport
  override def processResults(r: Seq[AMFValidationResult]): AMFValidationReport = {
    AMFValidationReport(!r.exists(_.level == SeverityLevels.VIOLATION),
                        "http://test.com/payload#validations",
                        profileName,
                        r)
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
      case _ => Nil
    }
  }
}
