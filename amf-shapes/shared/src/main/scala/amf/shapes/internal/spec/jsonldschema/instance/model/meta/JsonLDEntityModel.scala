package amf.shapes.internal.spec.jsonldschema.instance.model.meta

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.metamodel.{Field, ModelDefaultBuilder, Obj}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.spec.jsonldschema.instance.model.domain.{JsonLDElementModel, JsonLDObject}

class JsonLDEntityModel(val `type`: List[ValueType], val fields: List[Field]) extends JsonLDElementModel {
  override def modelInstance: AmfObject = new JsonLDObject(Fields(), Annotations(), this)
}
