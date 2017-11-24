package amf.framework.metamodel.document

import amf.framework.metamodel.Field
import amf.framework.metamodel.domain.DomainElementModel
import amf.framework.model.domain.AmfObject
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

/**
  * Fragment meta model.
  */
trait FragmentModel extends BaseUnitModel {

  val Encodes = Field(DomainElementModel, Document + "encodes")

  override def modelInstance: AmfObject = throw new Exception("Fragment is abstract instances cannot be created directly")

}

object FragmentModel extends FragmentModel {

  override val `type`: List[ValueType] = List(Document + "Fragment") ++ BaseUnitModel.`type`

  override def fields: List[Field] = Encodes :: BaseUnitModel.fields

}


