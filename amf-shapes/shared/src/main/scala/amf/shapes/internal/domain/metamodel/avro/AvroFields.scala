package amf.shapes.internal.domain.metamodel.avro

import amf.core.client.scala.vocabulary.Namespace.Shapes
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str, Int}
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

trait AvroFields {
  val AvroNamespace: Field =
    Field(
      Str,
      Shapes + "namespace",
      ModelDoc(
        ModelVocabularies.Shapes,
        "namespace",
        "(AVRO) a JSON string that qualifies the name"
      )
    )

  val Aliases: Field =
    Field(
      Array(Str),
      Shapes + "aliases",
      ModelDoc(
        ModelVocabularies.Shapes,
        "aliases",
        "(AVRO) a JSON array of strings, providing alternate names for this shape"
      )
    )

  val Size: Field =
    Field(
      Int,
      Shapes + "size",
      ModelDoc(
        ModelVocabularies.Shapes,
        "size",
        "(AVRO) an integer specifying the number of bytes per value"
      )
    )
}
