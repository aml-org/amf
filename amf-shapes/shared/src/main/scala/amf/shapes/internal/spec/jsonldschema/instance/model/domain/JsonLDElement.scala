package amf.shapes.internal.spec.jsonldschema.instance.model.domain

import amf.core.client.scala.model.domain.{AmfElement, AmfObject, DomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.Annotations

import scala.collection.mutable

trait JsonLDElement extends DomainElement {}

trait JsonLDElementModel extends DomainElementModel {}

object JsonLDElementModel extends JsonLDElementModel {
  override def modelInstance: AmfObject = throw new Exception("JsonLDElement is an abstract class")

  override def fields: List[Field] = Nil
}
