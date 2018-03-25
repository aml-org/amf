package amf.plugins.document.webapi.validation

import amf.core.model.document.{BaseUnit, PayloadFragment}
import amf.core.model.domain.extensions.DomainExtension
import amf.core.remote.Platform
import amf.core.services.PayloadValidator
import amf.core.validation.{AMFValidationResult, SeverityLevels}
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.resolveAnnotation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AnnotationsValidation(model: BaseUnit, platform: Platform) {

  def validate(): Future[Seq[AMFValidationResult]] = {
    val domainExtensionsWithTypes = findExtensionsWithTypes()
    val listResults               = domainExtensionsWithTypes.map(validateExtension)

    // Finally we collect all the results
    Future.sequence(listResults).map(_.flatten)
  }

  protected def findExtensionsWithTypes(): Seq[DomainExtension] = {
    model
      .findBy {
        case extension: DomainExtension =>
          Option(extension.definedBy).exists(definition => {
            Option(definition.schema).isDefined && resolveAnnotation("(" + definition.name.value() + ")").isDefined
          })
        case _ => false
      }
      .map(_.asInstanceOf[DomainExtension])
  }

  protected def validateExtension(extension: DomainExtension): Future[Seq[AMFValidationResult]] = {
    val extensionPayload = extension.extension
    val extensionShape   = extension.definedBy.schema
    val fragment         = PayloadFragment(extensionPayload, "application/yaml")
    PayloadValidator.validate(extensionShape, fragment, SeverityLevels.WARNING) map { report =>
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
