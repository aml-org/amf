package amf.shapes.internal.domain.metamodel.grpc

import amf.core.client.scala.vocabulary.Namespace.Shapes
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Array
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

trait GrpcFields {
  val ReservedValues: Field =
    Field(
      Array(ReservedModel),
      Shapes + "reservedValues",
      ModelDoc(
        ModelVocabularies.Shapes,
        "reservedValues",
        "(GRPC) field name or values to be reserved"
      )
    )
}

object GrpcFields extends GrpcFields {
  val fields = Seq(ReservedValues)
}
