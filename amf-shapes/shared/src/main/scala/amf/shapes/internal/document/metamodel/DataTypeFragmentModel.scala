package amf.shapes.internal.document.metamodel

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.Shapes
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.document.FragmentModel
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.document.DataTypeFragment

object DataTypeFragmentModel extends FragmentModel {

  override val fields: List[Field] = FragmentModel.fields

  override val `type`: List[ValueType] = List(Shapes + "DataTypeFragment") ++ FragmentModel.`type`

  override def modelInstance: AmfObject = DataTypeFragment()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "DataTypeFragment",
    "Fragment encoding a RAML data type"
  )
}
