package amf.plugins.domain.shapes.resolution.stages.shape_normalization

import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models._

/*
 * Search unique and identifiers fields to guess if tha any shape really its something else
 * If your anyShape has:
 *   NodeShapeModel.properties: return a NodeShape
 *   ArrayShapeModel.items: return an ArrayShape
 *   FileShapeModel.FileTypes: return a FileShape
 *   ScalarShapeModel.DataType: return a ScalarShape
 *   SchemaShapeModel.MediaType: return a SchemaShape
 *   UnionShapeModel.AnyOf: return a UnionShape
 *
 *   This is for that border cases when resolving a resource type (or trait) or simply that resolved inherits from any shape
 *
 *   Is there any cases that i'm missing?
 */

private[shape_normalization] case class AnyShapeAdjuster(any: AnyShape) {

  sealed implicit class AnyShapeConverter(val any: AnyShape) {

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
    if (any.fields.exists(NodeShapeModel.Properties))
      any.toNodeShape
    else if (any.fields.exists(ArrayShapeModel.Items))
      any.toArrayShape
    else if (any.fields.exists(FileShapeModel.FileTypes))
      any.toFileShape
    else if (any.fields.exists(ScalarShapeModel.DataType))
      any.toScalarShape
    else if (any.fields.exists(SchemaShapeModel.MediaType))
      any.toSchemaShape
    else if (any.fields.exists(UnionShapeModel.AnyOf))
      any.toUnionShape
    else any
  }

}

object AnyShapeAdjuster {
  def apply(any: AnyShape): AnyShape = new AnyShapeAdjuster(any).adjust
}
