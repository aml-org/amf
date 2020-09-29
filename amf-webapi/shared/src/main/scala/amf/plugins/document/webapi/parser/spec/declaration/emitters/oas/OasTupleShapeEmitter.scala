package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.emitter.BaseEmitters.ValueEmitter
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.annotations.CollectionFormatFromItems
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.FacetsEmitter
import amf.plugins.domain.shapes.metamodel.{ArrayShapeModel, NodeShapeModel, TupleShapeModel}
import amf.plugins.domain.shapes.models.TupleShape

import scala.collection.mutable.ListBuffer

case class OasTupleShapeEmitter(shape: TupleShape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit],
                                pointer: Seq[String] = Nil,
                                schemaPath: Seq[(String, String)] = Nil,
                                isHeader: Boolean = false)(implicit spec: OasLikeSpecEmitterContext)
    extends OasAnyShapeEmitter(shape, ordering, references, isHeader = isHeader) {
  override def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter](super.emitters(): _*)
    val fs     = shape.fields

    result += spec.oasTypePropertyEmitter("array", shape)

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
