package amf.metadata.document

import amf.metadata.Field
import amf.metadata.Type.Array
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

/**
  * Module metamodel
  */
trait ModuleModel extends BaseUnitModel {

  val Declares = Field(Array(DomainElementModel), Document + "declares")
}

object ModuleModel extends ModuleModel {

  override val `type`: List[ValueType] = List(Document + "Module") ++ BaseUnitModel.`type`

  override def fields: List[Field] = Declares :: BaseUnitModel.fields
}
