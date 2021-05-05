package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.annotations.ExplicitField
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.MetaModelTypeMapping
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.DomainElement
import amf.core.resolution.stages.TransformationStep
import amf.core.resolution.stages.elements.resolution.{ElementResolutionStage, ElementStageTransformer}
import amf.core.resolution.stages.selectors.NodeShapeSelector
import amf.plugins.domain.shapes.models.NodeShape

class MakeRequiredFieldImplicitForOptionalProperties()
    extends TransformationStep()
    with MetaModelTypeMapping
    with ElementResolutionStage[NodeShape] {

  protected var m: Option[BaseUnit] = None

  override def apply[T <: BaseUnit](model: T, errorHandler: ErrorHandler): T = {
    m = Some(model)
    model.transform(NodeShapeSelector, transform)(errorHandler).asInstanceOf[T]
  }

  protected def transform(element: DomainElement, isCycle: Boolean): Option[DomainElement] = {
    element match {
      case nodeShape: NodeShape => transformer.transform(nodeShape)
      case other                => Some(other)
    }
  }
  override def transformer: ElementStageTransformer[NodeShape] = ImplicitRequiredTransformer
}

object ImplicitRequiredTransformer extends ElementStageTransformer[NodeShape] {
  override def transform(node: NodeShape): Option[NodeShape] = {
    node.properties.foreach { propertyShape =>
      propertyShape.fields
        .entry(PropertyShapeModel.MinCount)
        .map(f => {
          if (f.scalar.value.asInstanceOf[Int] == 0) {
            f.value.annotations.reject(a => a.isInstanceOf[ExplicitField])
          }
        })

    }
    Some(node)
  }
}
