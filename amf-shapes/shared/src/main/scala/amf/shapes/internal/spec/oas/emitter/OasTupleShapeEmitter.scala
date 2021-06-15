package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.BaseEmitters.ValueEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.annotations.CollectionFormatFromItems
import amf.shapes.client.scala.domain.models.TupleShape
import amf.shapes.internal.domain.metamodel.{ArrayShapeModel, NodeShapeModel, TupleShapeModel}
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext
import amf.shapes.internal.spec.common.emitter.annotations.FacetsEmitter

import scala.collection.mutable.ListBuffer

case class OasTupleShapeEmitter(shape: TupleShape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit],
                                pointer: Seq[String] = Nil,
                                schemaPath: Seq[(String, String)] = Nil,
                                isHeader: Boolean = false)(implicit spec: OasLikeShapeEmitterContext)
    extends OasAnyShapeEmitter(shape, ordering, references, isHeader = isHeader) {
  override def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter](super.emitters(): _*)
    val fs     = shape.fields

    result += OasTypeFacetEmitter("array", shape)

    fs.entry(ArrayShapeModel.MaxItems).map(f => result += ValueEmitter("maxItems", f))

    fs.entry(ArrayShapeModel.MinItems).map(f => result += ValueEmitter("minItems", f))

    fs.entry(ArrayShapeModel.UniqueItems).map(f => result += ValueEmitter("uniqueItems", f))

    fs.entry(TupleShapeModel.ClosedItems) match {
      case Some(f) => result += ValueEmitter("additionalItems", f.negated)
      case None =>
        fs.entry(TupleShapeModel.AdditionalItemsSchema)
          .map(
            f =>
              result += OasEntryShapeEmitter("additionalItems",
                                             f.element.asInstanceOf[Shape],
                                             ordering,
                                             references,
                                             pointer,
                                             schemaPath))
    }

    fs.entry(ArrayShapeModel.CollectionFormat) match { // What happens if there is an array of an array with collectionFormat?
      case Some(f) if f.value.annotations.contains(classOf[CollectionFormatFromItems]) =>
        result += OasTupleItemsShapeEmitter(shape,
                                            ordering,
                                            references,
                                            Some(ValueEmitter("collectionFormat", f)),
                                            pointer,
                                            schemaPath)
      case Some(f) =>
        result += OasTupleItemsShapeEmitter(shape, ordering, references, None, pointer, schemaPath) += ValueEmitter(
          "collectionFormat",
          f)
      case None =>
        result += OasTupleItemsShapeEmitter(shape, ordering, references, None, pointer, schemaPath)
    }

    fs.entry(NodeShapeModel.Inherits).map(f => result += OasShapeInheritsEmitter(f, ordering, references))

    result ++= FacetsEmitter(shape, ordering).emitters

    result
  }
}
