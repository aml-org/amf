package amf.plugins.document.webapi.validation

import amf.core.model.document.{BaseUnit, PayloadFragment}
import amf.core.model.domain.extensions.DomainExtension
import amf.core.remote.Platform
import amf.core.validation.ValidationCandidate
import amf.core.services.PayloadValidator
import amf.core.validation.{AMFValidationResult, SeverityLevels}
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.resolveAnnotation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AnnotationsCandidatesCollector(model: BaseUnit, platform: Platform) {

  def collect(): Seq[ValidationCandidate] = {
    val domainExtensionsWithTypes = findExtensionsWithTypes()
    domainExtensionsWithTypes.map { extension =>
      val extensionPayload = extension.extension
      val extensionShape   = extension.definedBy.schema
      val fragment         = PayloadFragment(extensionPayload, "application/yaml")
      ValidationCandidate(extensionShape, fragment)
    }
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

}

object AnnotationsCandidatesCollector {
  def apply(model: BaseUnit, platform: Platform): Seq[ValidationCandidate] =
    new AnnotationsCandidatesCollector(model, platform).collect()
}
