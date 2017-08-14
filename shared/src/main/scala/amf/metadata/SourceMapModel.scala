package amf.metadata
import amf.metadata.Type.Str
import amf.vocabulary.Namespace.{Document, SourceMaps}
import amf.vocabulary.{Namespace, ValueType}

/**
  * Source Map Metadata
  */
object SourceMapModel extends Obj {

  val Element = Field(Str, SourceMaps + "element")

  val Value = Field(Str, SourceMaps + "value")

  override val fields: List[Field] = Nil

  override val `type`: List[ValueType] = List(SourceMaps + "SourceMap")
}
