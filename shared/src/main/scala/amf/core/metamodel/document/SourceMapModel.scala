package amf.core.metamodel.document

import amf.core.metamodel.Type.Str
import amf.core.metamodel.{Field, Obj}
import amf.core.vocabulary.Namespace.SourceMaps
import amf.core.vocabulary.ValueType

/**
  * Source Map Metadata
  */
object SourceMapModel extends Obj {

  val Element = Field(Str, SourceMaps + "element")

  val Value = Field(Str, SourceMaps + "value")

  override val fields: List[Field] = Nil

  override val `type`: List[ValueType] = List(SourceMaps + "SourceMap")
}
