package amf.plugins.document.webapi.validation

import amf.framework.model.document.BaseUnit
import amf.framework.validation.AMFValidationResult
import amf.plugins.domain.webapi.models.extensions.DomainExtension
import amf.remote.Platform
import amf.spec.common.WellKnownAnnotation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AnnotationsValidation(model: BaseUnit, platform: Platform) {

  def validate(): Future[Seq[AMFValidationResult]] = {
    val domainExtensionsWithTypes = findExtensionsWithTypes()
    val listResults = domainExtensionsWithTypes.map(validateExtension)

    // Finally we collect all the results
    Future.sequence(listResults).map(_.flatten)
  }

  protected def findExtensionsWithTypes(): Seq[DomainExtension] = {
    model.findBy {
      case extension: DomainExtension =>
        Option(extension.definedBy) match {
          case Some(customDomainProperty) if Option(customDomainProperty.schema).isDefined &&
            WellKnownAnnotation.normalAnnotation("(" + customDomainProperty.name + ")") => true
          case _ => false
        }
      case _ => false
    }.map(_.asInstanceOf[DomainExtension])
  }

  protected def validateExtension(extension: DomainExtension): Future[Seq[AMFValidationResult]] = {
    val extensionPayload = extension.extension
    val extensionShape = extension.definedBy.schema
    PayloadValidation(platform, extensionShape).validate(extensionPayload) map { report =>
      if (report.conforms) {
        Seq.empty
      } else {
        report.results
      }
    }
  }

}

object AnnotationsValidation {
  def apply(model: BaseUnit, platform: Platform) = new AnnotationsValidation(model, platform)
}