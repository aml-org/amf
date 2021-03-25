package amf.plugins.document.webapi.validation

import amf.core.annotations.DomainExtensionAnnotation
import amf.core.traversal.iterator.AmfElementStrategy
import amf.core.model.document.{BaseUnit, PayloadFragment}
import amf.core.model.domain.AmfScalar
import amf.core.model.domain.extensions.{DomainExtension, Extension, ShapeExtension}
import amf.core.remote.Platform
import amf.core.validation.ValidationCandidate
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.resolveAnnotation
class ExtensionsCandidatesCollector(model: BaseUnit) {

  def collect(): Seq[ValidationCandidate] = {
    val domainExtensionsWithTypes = findExtensionsWithTypes()
    domainExtensionsWithTypes.map { extension =>
      val extensionPayload = extension.extension
      val extensionShape   = extension.obtainSchema
      val fragment         = PayloadFragment(extensionPayload, "application/yaml")
      ValidationCandidate(extensionShape, fragment)
    }
  }

  protected def findExtensionsWithTypes(): Seq[Extension] = {
    model
      .iterator(strategy = AmfElementStrategy)
      .collect {
        case extension: DomainExtension if Option(extension.definedBy).exists(definition => {
              Option(definition.schema).isDefined && resolveAnnotation(s"(${definition.name.value()})").isDefined
            }) =>
          Seq(extension)
        case shapeExtension: ShapeExtension
            if Option(shapeExtension.definedBy).isDefined && Option(shapeExtension.obtainSchema).isDefined =>
          Seq(shapeExtension)
        case scalar: AmfScalar if scalar.annotations.contains(classOf[DomainExtensionAnnotation]) =>
          scalar.annotations
            .collect[DomainExtension] {
              case domainAnnotation: DomainExtensionAnnotation => domainAnnotation.extension
            }
      }
      .flatten
  }.toSeq

}

object ExtensionsCandidatesCollector {
  def apply(model: BaseUnit): Seq[ValidationCandidate] =
    new ExtensionsCandidatesCollector(model).collect()
}
