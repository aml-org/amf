package amf.plugins.document.apicontract.resolution.pipelines.compatibility.raml

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.transform.stages.TransformationStep
import amf.core.client.scala.transform.stages.elements.resolution.{ElementResolutionStage, ElementStageTransformer}
import amf.core.client.scala.transform.stages.selectors.NodeShapeSelector
import amf.core.internal.annotations.ExplicitField
import amf.core.internal.metamodel.MetaModelTypeMapping
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.plugins.domain.shapes.models.NodeShape

class MakeRequiredFieldImplicitForOptionalProperties()
    extends TransformationStep()
    with MetaModelTypeMapping
    with ElementResolutionStage[NodeShape] {

  protected var m: Option[BaseUnit] = None

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    m = Some(model)
    model.transform(NodeShapeSelector, transform)(errorHandler)
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
