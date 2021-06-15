package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.annotations.ExplicitField
import amf.core.internal.render.BaseEmitters.{MapEntryEmitter, ValueEmitter}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.domain.models.ArrayShape
import amf.shapes.internal.domain.metamodel.ArrayShapeModel
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import amf.shapes.internal.spec.contexts.emitter.raml.RamlScalarEmitter

import scala.collection.mutable.ListBuffer
import amf.core.internal.utils._

case class RamlArrayShapeEmitter(array: ArrayShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends RamlAnyShapeEmitter(array, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = array.fields

    fs.entry(ArrayShapeModel.Items)
      .foreach(_ => {
        typeEmitted = true
        result += RamlItemsShapeEmitter(array, ordering, references)
      })

    fs.entry(ArrayShapeModel.MaxItems).map(f => result += RamlScalarEmitter("maxItems", f))

    fs.entry(ArrayShapeModel.MinItems).map(f => result += RamlScalarEmitter("minItems", f))

    fs.entry(ArrayShapeModel.UniqueItems).map(f => result += RamlScalarEmitter("uniqueItems", f))

    fs.entry(ArrayShapeModel.CollectionFormat).map(f => result += ValueEmitter("collectionFormat".asRamlAnnotation, f))

    if (!typeEmitted)
      result += MapEntryEmitter("type", "array")

    result
  }

  override val typeName: Option[String] = array.annotations.find(classOf[ExplicitField]).map(_ => "array")
}
