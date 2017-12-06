package amf.core.metamodel.document

import amf.core.metamodel.Field
import amf.core.model.document.ExternalFragment
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

object ExternalFragmentModel extends FragmentModel {
  override def fields: List[Field] = FragmentModel.fields

  override val `type`: List[ValueType] = List(Document + "ExternalModel") ++ FragmentModel.`type`

  override def modelInstance: AmfObject = ExternalFragment()
}
