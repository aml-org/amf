package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{Shape, Linkable}
import amf.core.resolution.stages.ResolutionStage
import amf.core.resolution.stages.elements.resolution.ReferenceResolution
import amf.core.resolution.stages.selectors.{NodeShapeSelector, Selector}
import amf.plugins.domain.shapes.models.{NodeShape, ArrayShape, AnyShape}

// TODO with need to resolve additionalProperties because they are emitter and parsed with OAS emitters and parsers
class ResolveAdditionalProperties()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  override def resolve[T <: BaseUnit](model: T): T = {
    val referenceResolution = new ReferenceResolution(errorHandler)
    model.iterator().foreach {
      case node: NodeShape if Option(node.additionalPropertiesSchema).isDefined =>
        node.additionalPropertiesSchema match {
          case linkable: Linkable if linkable.isLink =>
            referenceResolution.transform(node.additionalPropertiesSchema).foreach {
              case resolvedSchema: Shape => node.withAdditionalPropertiesSchema(resolvedSchema)
              case _                     => // Nothing
            }
          case array: ArrayShape if array.items.isLink =>
            referenceResolution.transform(array.items).foreach {
              case resolvedSchema: Shape => array.withItems(resolvedSchema)
              case _                     => // Nothing
            }
          case any: AnyShape if any.and.nonEmpty =>
            val resolvedAnd = any.and.map {
              case l: Linkable if l.isLink =>
                referenceResolution
                  .transform(l)
                  .getOrElse(l)
                  .asInstanceOf[Shape] // TODO fail if cannot resolve link?
              case s => s
            }
            any.withAnd(resolvedAnd)
          case _ => // Nothing
        }
      case _ => // Nothing
    }
    model
  }

}
