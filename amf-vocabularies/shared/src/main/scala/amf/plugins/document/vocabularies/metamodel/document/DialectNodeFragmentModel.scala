package amf.plugins.document.vocabularies.metamodel.document

import amf.core.metamodel.Field
import amf.core.metamodel.document.FragmentModel
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.ValueType
import amf.core.vocabulary.Namespace.Document
import amf.plugins.document.vocabularies.model.document.DialectFragment

object DialectNodeFragmentModel extends FragmentModel {
  override def fields: List[Field] = FragmentModel.fields

  override val `type`: List[ValueType] = List(Document + "DialectNode") ++ FragmentModel.`type`

  override def modelInstance: AmfObject = DialectFragment()
}