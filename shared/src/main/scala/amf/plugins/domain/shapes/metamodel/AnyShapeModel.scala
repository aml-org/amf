package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.DomainElementModel
import amf.plugins.domain.shapes.models.AnyShape
import amf.core.vocabulary.{Namespace, ValueType}

object AnyShapeModel extends ShapeModel {
  val Name = Field(Str, Namespace.Shacl + "name")

  val Documentation = Field(CreativeWorkModel, Schema + "documentation")

  val XMLSerialization = Field(XMLSerializerModel, Shapes + "xmlSerialization")

  val Examples = Field(Array(ExampleModel), Document + "examples")

  override def fields: List[Field] =Shapemodel.fields ++ List(Name,
                                                              Documentation,
                                                              XMLSerialization,
                                                              Examples)

  override val `type`: List[ValueType] =
    List(Namespace.Shapes + "AnyShape", Namespace.Shacl + "Shape", Namespace.Shapes + "Shape")

  override def modelInstance = AnyShape()
}
