package amf.framework.metamodel.document

import amf.framework.metamodel.Type.Str
import amf.framework.metamodel.{Field, Obj}
import amf.vocabulary.Namespace.SourceMaps
import amf.vocabulary.ValueType

/**
  * Source Map Metadata
  */
object SourceMapModel extends Obj {

  val Element = Field(Str, SourceMaps + "element")

  val Value = Field(Str, SourceMaps + "value")

  override val fields: List[Field] = Nil

  override val `type`: List[ValueType] = List(SourceMaps + "SourceMap")
}
