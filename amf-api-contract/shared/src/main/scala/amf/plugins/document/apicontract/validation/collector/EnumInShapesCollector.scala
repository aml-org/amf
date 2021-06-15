package amf.plugins.document.apicontract.validation.collector

import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.{AmfElement, DataNode, ScalarNode, Shape}
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.utils.MediaTypeMatcher
import amf.core.internal.validation.ValidationCandidate

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
    shallowCopy.fields.removeField(ShapeModel.Values) // remove enum values from shape as is in not necessary when validating each enum value.
    enums.map(v => ValidationCandidate(shallowCopy, PayloadFragment(v, defaultMediaTypeFor(v))))
  }

  private def defaultMediaTypeFor(dataNode: DataNode): String = dataNode match {
    case s: ScalarNode if s.value.option().exists(_.isXml) => "application/xml"
    case _                                                 => "application/json"
  }

}
