package amf.shapes.client.scala.model.domain.jsonldinstance

import amf.core.client.scala.model.domain.{AmfElement, AmfObject, DomainElement}
import amf.core.client.scala.vocabulary.Namespace.Document
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.DomainElementModel

trait JsonLDElement extends AmfElement {}

trait JsonLDElementModel extends DomainElementModel {}

object JsonLDElementModel extends JsonLDElementModel {
  override def modelInstance: AmfObject = throw new Exception("JsonLDElement is an abstract class")

  override def fields: List[Field] = Nil

  override val `type`: List[ValueType] = (Document + "JsonLDElement") :: DomainElementModel.`type`

}
