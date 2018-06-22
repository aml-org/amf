package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.metamodel.domain.{ExternalSourceElementModel, ShapeModel}
import amf.core.vocabulary.Namespace.{Document, Schema, Shacl, Shapes}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.models.AnyShape

trait AnyShapeModel extends ShapeModel with ExternalSourceElementModel {

  val Documentation = Field(CreativeWorkModel, Schema + "documentation")

  val XMLSerialization = Field(XMLSerializerModel, Shapes + "xmlSerialization")

  val Examples = Field(Array(ExampleModel), Document + "examples")

  override def fields: List[Field] =
    ShapeModel.fields ++ ExternalSourceElementModel.fields ++ List(Documentation, XMLSerialization, Examples)

  override val `type`: List[ValueType] =
    List(Shapes + "AnyShape", Shacl + "Shape", Shapes + "Shape")

  override def modelInstance = AnyShape()
}

object AnyShapeModel extends AnyShapeModel
