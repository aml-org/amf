package amf.shapes.internal.validation.payload.collector

import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{AmfElement, DataNode, ScalarNode, Shape}
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.remote.Mimes.`application/json`
import amf.core.internal.utils.MediaTypeMatcher
import amf.core.internal.validation.ValidationCandidate
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape, ScalarShape}

object DiscriminatorValuesCollector extends ValidationCandidateCollector {

  override def collect(element: AmfElement): Seq[ValidationCandidate] = {
    element match {
      case node: NodeShape if hasDiscriminator(node) && hasDiscriminatorValue(node) => discriminatorValueCandidate(node)
      case _                                                                        => Nil
    }
  }
  private def discriminatorValueCandidate(node: NodeShape): Seq[ValidationCandidate] = {
    findDiscriminatorProperty(node) match {
      case Some(prop) =>
        val schema = prop.range
        val value  = node.discriminatorValue.value()
        ValidationCandidate(schema, PayloadFragment(value, `application/json`)) :: Nil
      case None => Nil
    }
  }

  private def hasDiscriminator(node: NodeShape): Boolean      = node.discriminator.nonEmpty
  private def hasDiscriminatorValue(node: NodeShape): Boolean = node.discriminatorValue.nonEmpty

  private def findDiscriminatorProperty(node: NodeShape): Option[PropertyShape] = {
    val discriminatorPropertyName = node.discriminator.value()
    node.properties.find(_.name.value() == discriminatorPropertyName)
  }
}
