package amf.shapes.internal.spec.jsonschema.emitter

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.Fields
import amf.core.internal.render.BaseEmitters.ValueEmitter
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.domain.models.ScalarShape
import amf.shapes.internal.domain.metamodel.ScalarShapeModel.{Encoding, MediaType, Schema}
import amf.shapes.internal.spec.common.{JSONSchemaDraft201909SchemaVersion, JSONSchemaDraft7SchemaVersion, SchemaVersion}
import amf.shapes.internal.spec.oas.emitter.OasEntryShapeEmitter

trait TypeParserFactory {
  def build(key: String, shape: Shape): OasEntryShapeEmitter
}

object ContentEmitters {
  def emitters(node: ScalarShape, version: SchemaVersion, typeParserFactory: TypeParserFactory): Seq[EntryEmitter] = {
    version match {
      case JSONSchemaDraft7SchemaVersion      => draft7Emitters(node)
      case JSONSchemaDraft201909SchemaVersion => draft2019Emitters(node, typeParserFactory)
      case _                                  => Seq()
    }
  }

  private def draft7Emitters(node: Shape): Seq[EntryEmitter] =
    Seq(ContentEmitterFactory.emitterFor("contentEncoding", node, Encoding),
        ContentEmitterFactory.emitterFor("contentMediaType", node, MediaType)).flatten

  private def draft2019Emitters(node: Shape, typeParserFactory: TypeParserFactory) = {
    val optionalTypeEmitter = node.fields.entry(Schema).map { f =>
      typeParserFactory.build("contentSchema", f.element.asInstanceOf[Shape])
    }
    Seq(optionalTypeEmitter).flatten ++ draft7Emitters(node)
  }
}

object ContentEmitterFactory {
  def emitterFor(key: String, node: Shape, field: Field): Option[EntryEmitter] = emitterFor(key, node.fields, field)
  def emitterFor(key: String, fs: Fields, field: Field): Option[EntryEmitter] =
    fs.entry(field).map(f => ValueEmitter(key, f))
}
