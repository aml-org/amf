package amf.core.metamodel.document

import amf.core.metamodel.Field
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

/**
  * Fragment meta model.
  *
  * A Module is a parsing Unit that declares DomainElements that can be referenced from the DomainElements in other parsing Units.
  * It main purpose is to expose the declared references so they can be re-used
  */
trait FragmentModel extends BaseUnitModel {

  /**
    * The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains.
    */
  val Encodes = Field(DomainElementModel, Document + "encodes")

  override def modelInstance: AmfObject = throw new Exception("Fragment is abstract instances cannot be created directly")

}

object FragmentModel extends FragmentModel {

  override val `type`: List[ValueType] = List(Document + "Fragment") ++ BaseUnitModel.`type`

  override def fields: List[Field] = Encodes :: BaseUnitModel.fields

}


