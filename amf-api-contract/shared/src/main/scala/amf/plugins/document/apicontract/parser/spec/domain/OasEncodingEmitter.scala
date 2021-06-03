package amf.plugins.document.apicontract.parser.spec.domain

import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.apicontract.contexts.SpecEmitterContext
import amf.plugins.domain.apicontract.models.Encoding
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import amf.core.emitter.BaseEmitters._
import amf.plugins.domain.apicontract.metamodel.EncodingModel

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
