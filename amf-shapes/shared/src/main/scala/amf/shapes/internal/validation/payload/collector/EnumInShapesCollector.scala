package amf.shapes.internal.validation.payload.collector

import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.{AmfElement, DataNode, ScalarNode, Shape}
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.remote.Mimes.`application/json`
import amf.core.internal.utils.MediaTypeMatcher
import amf.core.internal.validation.ValidationCandidate
import amf.shapes.client.scala.model.domain.{AnyShape, ScalarShape}

object EnumInShapesCollector extends ValidationCandidateCollector {

  override def collect(element: AmfElement): Seq[ValidationCandidate] = {
    element match {
      case shape: AnyShape if shape.values.nonEmpty => shapeEnumCandidates(shape)
      case _                                        => Nil
    }
  }

  private def shapeEnumCandidates(shape: Shape): Seq[ValidationCandidate] = {
    val enums       = shape.values
    val shallowCopy = shape.copyShape()
    shallowCopy.fields.removeField(
      ShapeModel.Values
    ) // remove enum values from shape as is in not necessary when validating each enum value.
    enums.map({ value =>
      ValidationCandidate(shallowCopy, PayloadFragment(value, defaultMediaTypeFor(value, shape)))
    })
  }

  private def defaultMediaTypeFor(dataNode: DataNode, shape: Shape): String = dataNode match {
    case s: ScalarNode if s.value.option().isDefined => s.value.value().guessMediaType(shape.isInstanceOf[ScalarShape])
    case _                                           => `application/json`
  }

}
