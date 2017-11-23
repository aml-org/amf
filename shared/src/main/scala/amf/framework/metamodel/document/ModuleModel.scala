package amf.framework.metamodel.document

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.Array
import amf.framework.model.document.Module
import amf.metadata.domain.DomainElementModel
import amf.model.AmfObject
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

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
