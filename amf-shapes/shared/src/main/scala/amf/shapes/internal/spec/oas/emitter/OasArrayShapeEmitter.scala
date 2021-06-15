package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.BaseEmitters.ValueEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.annotations.CollectionFormatFromItems
import amf.shapes.client.scala.domain.models.ArrayShape
import amf.shapes.internal.domain.metamodel.ArrayShapeModel.{UnevaluatedItems, UnevaluatedItemsSchema}
import amf.shapes.internal.domain.metamodel.{ArrayShapeModel, NodeShapeModel}
import amf.shapes.internal.spec.common.JSONSchemaDraft7SchemaVersion
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext
import amf.shapes.internal.spec.common.emitter.annotations.FacetsEmitter
import amf.shapes.internal.spec.jsonschema.emitter.UnevaluatedEmitter.unevaluatedItemsInfo
import amf.shapes.internal.spec.jsonschema.emitter.{UnevaluatedEmitter, UntranslatableDraft2019FieldsPresentGuard}

import scala.collection.mutable.ListBuffer

case class OasArrayShapeEmitter(shape: ArrayShape,
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

    if (spec.schemaVersion.isBiggerThanOrEqualTo(JSONSchemaDraft7SchemaVersion) && Option(shape.contains).isDefined)
      result += OasEntryShapeEmitter("contains", shape.contains, ordering, references, pointer, schemaPath)

    fs.entry(ArrayShapeModel.CollectionFormat) match { // What happens if there is an array of an array with collectionFormat?
      case Some(f) if f.value.annotations.contains(classOf[CollectionFormatFromItems]) =>
        result += OasItemsShapeEmitter(shape,
                                       ordering,
                                       references,
                                       Some(ValueEmitter("collectionFormat", f)),
                                       pointer,
                                       schemaPath)
      case Some(f) =>
        result += OasItemsShapeEmitter(shape, ordering, references, None, pointer, schemaPath) += ValueEmitter(
          "collectionFormat",
          f)
      case None =>
        result += OasItemsShapeEmitter(shape, ordering, references, None, pointer, schemaPath)
    }

    UntranslatableDraft2019FieldsPresentGuard(shape,
                                              Seq(UnevaluatedItemsSchema, UnevaluatedItems),
                                              Seq("unevaluatedItems")).evaluateOrRun { () =>
      result += new UnevaluatedEmitter(shape, unevaluatedItemsInfo, ordering, references, pointer, schemaPath)
    }

    UntranslatableDraft2019FieldsPresentGuard(shape,
                                              Seq(ArrayShapeModel.MinContains, ArrayShapeModel.MaxContains),
                                              Seq("minContains", "maxContains")).evaluateOrRun { () =>
      fs.entry(ArrayShapeModel.MinContains).map(f => result += ValueEmitter("minContains", f))
      fs.entry(ArrayShapeModel.MaxContains).map(f => result += ValueEmitter("maxContains", f))
    }

    fs.entry(NodeShapeModel.Inherits).map(f => result += OasShapeInheritsEmitter(f, ordering, references))

    result ++= FacetsEmitter(shape, ordering).emitters

    result
  }
}
