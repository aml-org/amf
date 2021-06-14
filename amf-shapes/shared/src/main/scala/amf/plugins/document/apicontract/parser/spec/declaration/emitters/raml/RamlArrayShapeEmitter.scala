package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.annotations.ExplicitField
import amf.core.internal.render.BaseEmitters.{MapEntryEmitter, ValueEmitter}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.apicontract.contexts.emitter.raml.RamlScalarEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.RamlShapeEmitterContext
import amf.plugins.domain.shapes.metamodel.ArrayShapeModel
import amf.plugins.domain.shapes.models.ArrayShape

import scala.collection.mutable.ListBuffer

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
