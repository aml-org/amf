package amf.plugins.document.vocabularies.metamodel.document

import amf.core.metamodel.Type.Array
import amf.core.metamodel.{Field, Obj}
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.metamodel.domain.ExternalModel

trait ExternalContextModel extends Obj {
  val Externals = Field(Array(ExternalModel), Namespace.Meta + "externals")
}

object ExternalContextModelFields extends ExternalContextModel {
  override def fields: List[Field] = Externals :: Nil

  override val `type`: List[ValueType] = Nil
}
