package amf.metadata.document

import amf.metadata.Field
import amf.metadata.Type.Array
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

/**
  * Fragment metamodel
  */
trait FragmentModel extends BaseUnitModel {

  val Encodes = Field(Array(DomainElementModel), Document + "encodes")

  override val `type`: List[ValueType] = List(Document + "Fragment") ++ BaseUnitModel.`type`
}

object FragmentModel extends FragmentModel
