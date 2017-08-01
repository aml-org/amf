package amf.metadata
import amf.metadata.Type.Str
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

/**
  * Source Map Metadata
  */
object SourceMapModel extends Obj {

  val Element = Field(Str, Document + "element")

  val Value = Field(Str, Document + "value")

  override val fields: List[Field] = Nil

  override val `type`: List[ValueType] = List(Document + "SourceMap")
}
