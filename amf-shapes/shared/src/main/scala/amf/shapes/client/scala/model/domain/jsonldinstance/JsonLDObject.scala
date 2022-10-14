package amf.shapes.client.scala.model.domain.jsonldinstance

import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.metamodel.Obj
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.jsonldschema.JsonLDEntityModel

class JsonLDObject(override val fields: Fields, override val annotations: Annotations, model: JsonLDEntityModel)
    extends DomainElement
    with JsonLDElement {
  override def meta: Obj = model

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId = "/object/"
}
