package amf.apicontract.internal.validation.payload.collector

import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.{AmfElement, AmfScalar}
import amf.core.client.scala.model.domain.extensions.{DomainExtension, Extension, ShapeExtension}
import amf.core.internal.annotations.DomainExtensionAnnotation
import amf.core.internal.validation.ValidationCandidate
import amf.apicontract.internal.spec.common.parser.WellKnownAnnotation.resolveAnnotation

object ExtensionsCollector extends ValidationCandidateCollector {
  override def collect(element: AmfElement): Seq[ValidationCandidate] = {
    val domainExtensionsWithTypes = findExtensionsWithTypes(element)
    domainExtensionsWithTypes.map { extension =>
      val extensionPayload = extension.extension
      val extensionShape   = extension.obtainSchema
      val fragment         = PayloadFragment(extensionPayload, "application/yaml")
      ValidationCandidate(extensionShape, fragment)
    }
  }

  protected def findExtensionsWithTypes(element: AmfElement): Seq[Extension] =
    element match {
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
      case _ => Nil
    }
}
