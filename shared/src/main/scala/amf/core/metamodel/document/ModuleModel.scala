package amf.core.metamodel.document

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.document.Module
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

/**
  * Module metamodel
  */
trait ModuleModel extends BaseUnitModel {

  val Declares = Field(Array(DomainElementModel), Document + "declares")

  override def modelInstance: AmfObject = Module()
}

object ModuleModel extends ModuleModel {

  override val `type`: List[ValueType] = List(Document + "Module") ++ BaseUnitModel.`type`

  override def fields: List[Field] = Declares :: BaseUnitModel.fields
}
