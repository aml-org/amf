package amf.plugins.document.webapi.validation

import amf.core.annotations.DomainExtensionAnnotation
import amf.core.iterator.AmfElementStrategy
import amf.core.model.document.{BaseUnit, PayloadFragment}
import amf.core.model.domain.AmfScalar
import amf.core.model.domain.extensions.DomainExtension
import amf.core.remote.Platform
import amf.core.validation.ValidationCandidate
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.resolveAnnotation
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
      .iterator(strategy = AmfElementStrategy)
      .collect {
        case extension: DomainExtension if Option(extension.definedBy).exists(definition => {
              Option(definition.schema).isDefined && resolveAnnotation(s"(${definition.name.value()})").isDefined
            }) =>
          Seq(extension)
        case scalar: AmfScalar if scalar.annotations.find(classOf[DomainExtensionAnnotation]).isDefined =>
          scalar.annotations
            .collect[DomainExtensionAnnotation] {
              case domainAnnotation: DomainExtensionAnnotation => domainAnnotation
            }
            .map(_.extension)

      }
      .flatten
  }.toSeq

}

object AnnotationsCandidatesCollector {
  def apply(model: BaseUnit, platform: Platform): Seq[ValidationCandidate] =
    new AnnotationsCandidatesCollector(model, platform).collect()
}
