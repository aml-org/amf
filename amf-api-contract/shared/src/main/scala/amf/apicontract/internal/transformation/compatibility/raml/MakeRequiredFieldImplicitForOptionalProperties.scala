package amf.apicontract.internal.transformation.compatibility.raml

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.transform.stages.elements.resolution.{ElementResolutionStage, ElementStageTransformer}
import amf.core.internal.transform.stages.selectors.NodeShapeSelector
import amf.core.internal.annotations.ExplicitField
import amf.core.internal.metamodel.MetaModelTypeMapping
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.shapes.client.scala.model.domain.NodeShape

class MakeRequiredFieldImplicitForOptionalProperties()
    extends TransformationStep()
    with MetaModelTypeMapping
    with ElementResolutionStage[NodeShape] {

  protected var m: Option[BaseUnit] = None

  override def transform(model: BaseUnit,
                         errorHandler: AMFErrorHandler,
                         configuration: AMFGraphConfiguration): BaseUnit = {
    m = Some(model)
    model.transform(NodeShapeSelector, transform(_, _, configuration))(errorHandler)
  }

  protected def transform(element: DomainElement,
                          isCycle: Boolean,
                          configuration: AMFGraphConfiguration): Option[DomainElement] = {
    element match {
      case nodeShape: NodeShape => transformer.transform(nodeShape, configuration)
      case other                => Some(other)
    }
  }
  override def transformer: ElementStageTransformer[NodeShape] = ImplicitRequiredTransformer
}

object ImplicitRequiredTransformer extends ElementStageTransformer[NodeShape] {
  override def transform(node: NodeShape, configuration: AMFGraphConfiguration): Option[NodeShape] = {
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
