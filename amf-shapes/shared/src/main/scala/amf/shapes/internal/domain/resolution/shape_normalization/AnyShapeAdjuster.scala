package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.Field
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.domain.metamodel._

/*
 * Search unique and identifiers fields to guess if tha any shape really its something else
 *   This is for that border cases when resolving a resource type (or trait) or simply that resolved inherits from any shape
 *
 *   Is there any cases that i'm missing?
 */

private[shape_normalization] case class AnyShapeAdjuster(any: AnyShape) {

  sealed implicit class AnyShapeConverter2(val any: AnyShape) {

    def toNodeShape: NodeShape = {
      NodeShape(any.fields, any.annotations).withId(any.id)
    }

    def toArrayShape: ArrayShape = {
      ArrayShape(any.fields, any.annotations).withId(any.id)
    }

    def toFileShape: FileShape = {
      FileShape(any.fields, any.annotations).withId(any.id)
    }

    def toScalarShape: ScalarShape = {
      ScalarShape(any.fields, any.annotations).withId(any.id)
    }

    def toSchemaShape: SchemaShape = {
      SchemaShape(any.fields, any.annotations).withId(any.id)
    }

    def toUnionShape: UnionShape = {
      UnionShape(any.fields, any.annotations).withId(any.id)
    }
  }

  def adjust: AnyShape = {
    if (checkModelFields(NodeShapeModel.specificFields))
      any.toNodeShape
    else if (checkModelFields(ArrayShapeModel.specificFields))
      any.toArrayShape
    else if (checkModelFields(FileShapeModel.specificFields))
      any.toFileShape
    else if (checkModelFields(ScalarShapeModel.specificFields))
      any.toScalarShape
    else if (checkModelFields(SchemaShapeModel.specificFields))
      any.toSchemaShape
    else if (checkModelFields(UnionShapeModel.specificFields))
      any.toUnionShape
    else any
  }

  private def checkModelFields(specificFields: List[Field]): Boolean = specificFields.exists(f => any.fields.exists(f))

}

object AnyShapeAdjuster {
  def apply(any: AnyShape): AnyShape = {
    /*
     * This is horrible but necessary. In ShapeInheritanceResolver.normalizeAction every shape goes through `any: AnyShape`
     * match branch. This causes some TupleShapes to be converted into ArrayShapes. Regardless of the invalid conversions,
     * we are still duplicating every shape (e.g. a NodeShape will produce a new identical NodeShape) thus increasing our
     * memory footprint. TODO remove AnyShapeAdjuster
     * */
    if (!any.isConcreteShape) {
      new AnyShapeAdjuster(any).adjust
    } else {
      any
    }
  }

  def apply(s: Shape): Shape = {
    s match {
      case a: AnyShape => apply(a)
      case shape       => shape
    }
  }
}
