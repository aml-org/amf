package amf.framework.metamodel.document

import amf.framework.metamodel.Field
import amf.metadata.domain.DomainElementModel
import amf.model.AmfObject
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

/**
  * Fragment meta model.
  */
trait FragmentModel extends BaseUnitModel {

  val Encodes = Field(DomainElementModel, Document + "encodes")

  override def modelInstance: AmfObject = throw new Exception("Fragment is an abstract cannot create model instance")

}

object FragmentModel extends FragmentModel {

  override val `type`: List[ValueType] = List(Document + "Fragment") ++ BaseUnitModel.`type`

  override def fields: List[Field] = Encodes :: BaseUnitModel.fields

}


