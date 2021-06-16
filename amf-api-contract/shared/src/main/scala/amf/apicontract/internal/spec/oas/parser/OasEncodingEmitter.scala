package amf.apicontract.internal.spec.oas.parser

import amf.apicontract.client.scala.model.domain.Encoding
import amf.apicontract.internal.metamodel.domain.EncodingModel
import amf.apicontract.internal.spec.common.emitter.{RamlParametersEmitter, SpecEmitterContext}
import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, sourceOr, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable

case class OasEncodingsEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  private def encodings(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit]): Seq[EntryEmitter] = {
    val result = f.array.values.map(e => OasEncodingEmitter(e.asInstanceOf[Encoding], ordering, references))
    ordering.sorted(result)
  }

  override def emit(b: EntryBuilder): Unit = {
    val emitters = encodings(f, ordering, references)
    sourceOr(
      f.value.annotations,
      b.entry(
        key,
        _.obj(traverse(emitters, _))
      )
    )
  }

  override def position(): Position = pos(f.value.annotations)

}

case class OasEncodingEmitter(encoding: Encoding, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val fs = encoding.fields

    sourceOr(
      encoding.annotations,
      b.entry(
        encoding.propertyName.value(),
        _.obj { b =>
          val result = mutable.ListBuffer[EntryEmitter]()

          //contentType
          fs.entry(EncodingModel.ContentType)
            .map(f => {
              result += ValueEmitter("contentType", f)
            })

          //headers
          fs.entry(EncodingModel.Headers)
            .map(f => result += RamlParametersEmitter("headers", f, ordering, references)(spec))

          //style
          fs.entry(EncodingModel.Style)
            .map(f => {
              result += ValueEmitter("style", f)
            })

          //explode
          fs.entry(EncodingModel.Explode)
            .map(f => {
              result += ValueEmitter("explode", f)
            })

          //allowReserved
          fs.entry(EncodingModel.AllowReserved)
            .map(f => {
              result += ValueEmitter("allowReserved", f)
            })

          traverse(ordering.sorted(result), b)
        }
      )
    )
  }

  override def position(): Position = pos(encoding.annotations)

}
