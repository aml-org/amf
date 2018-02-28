package amf.plugins.document.vocabularies2.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}

object DialectDomainElementModel extends DomainElementModel {

  override def fields: List[Field] = DomainElementModel.fields
  override val `type`: List[ValueType] = Namespace.Meta + "DialectDomainElement" :: DomainElementModel.`type`

  override val dynamic = true
  override def modelInstance: AmfObject = throw new Exception("DialectDomainElement is an abstract class and it cannot be isntantiated directly")
}
