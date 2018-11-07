package amf.core.metamodel.document

import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.metamodel.{Field, Obj}
import amf.core.vocabulary.Namespace.SourceMaps
import amf.core.vocabulary.ValueType

/**
  * Source Map Metadata
  *
  * SourceMaps include tags with syntax specific information obtained when parsing a particular specification syntax like RAML or OpenAPI.
  * It can be used to re-generate the document from the RDF model with a similar syntax
  */
object SourceMapModel extends Obj {

  val Element = Field(
    Str,
    SourceMaps + "element",
    ModelDoc(ModelVocabularies.AmlDoc, "element", "Label indicating the type of source map information"))

  val Value =
    Field(Str, SourceMaps + "value", ModelDoc(ModelVocabularies.AmlDoc, "value", "Value for the source map."))

  override val fields: List[Field] = Nil

  override val `type`: List[ValueType] = List(SourceMaps + "SourceMap")

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.AmlDoc,
    "Source Map",
    "SourceMaps include tags with syntax specific information obtained when parsing a particular specification syntax like RAML or OpenAPI.\nIt can be used to re-generate the document from the RDF model with a similar syntax"
  )
}
