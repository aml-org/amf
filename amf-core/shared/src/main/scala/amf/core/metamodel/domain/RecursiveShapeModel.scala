package amf.core.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Iri
import amf.core.model.domain.{AmfObject, RecursiveShape}
import amf.core.vocabulary.{Namespace, ValueType}

/**
  * Recursion on a Shape structure, used when expanding a shape and finding the canonical representation of that shape.
  */
object RecursiveShapeModel extends ShapeModel {

  /**
    * Link to the base of the recursion for a recursive shape
    */
  val FixPoint = Field(Iri, Namespace.Shapes + "fixPoint")

  override def fields: List[Field] = List(FixPoint) ++ ShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] =
    List(Namespace.Shapes + "RecursiveShape", Namespace.Shacl + "Shape", Namespace.Shapes + "Shape")

  override def modelInstance: AmfObject = RecursiveShape()
}
