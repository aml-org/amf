package amf.graphql.internal.spec.parser.syntax

import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.core.internal.metamodel.domain.ScalarNodeModel
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.shapes.internal.domain.metamodel.operations.ShapeOperationModel
import amf.shapes.internal.domain.metamodel.{NodeShapeModel, ScalarShapeModel, UnionShapeModel}

object Locations {
  val locationToDomain: Map[String, Seq[String]] = Map[String, Seq[String]](
    "SCALAR"                 -> Seq(ScalarShapeModel.`type`.head.iri()),
    "OBJECT"                 -> Seq(NodeShapeModel.`type`.head.iri()),
    "FIELD_DEFINITION"       -> Seq(ShapeOperationModel.`type`.head.iri(), PropertyShapeModel.`type`.head.iri()),
    "ARGUMENT_DEFINITION"    -> Seq(NodeShapeModel.`type`.head.iri()),
    "INTERFACE"              -> Seq(NodeShapeModel.`type`.head.iri()),
    "UNION"                  -> Seq(UnionShapeModel.`type`.head.iri()),
    "ENUM"                   -> Seq(ScalarShapeModel.`type`.head.iri()),
    "ENUM_VALUE"             -> Seq(ScalarNodeModel.`type`.head.iri()),
    "INPUT_OBJECT"           -> Seq(NodeShapeModel.`type`.head.iri()),
    "INPUT_FIELD_DEFINITION" -> Seq(PropertyShapeModel.`type`.head.iri()),
    "SCHEMA"                 -> Seq(WebApiModel.`type`.head.iri())
  )

}
