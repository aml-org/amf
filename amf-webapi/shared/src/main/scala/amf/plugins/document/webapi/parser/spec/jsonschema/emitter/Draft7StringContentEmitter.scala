package amf.plugins.document.webapi.parser.spec.jsonschema.emitter

import amf.core.emitter.BaseEmitters.ValueEmitter
import amf.core.emitter.EntryEmitter
import amf.core.metamodel.Field
import amf.core.model.domain.Shape
import amf.core.parser.Fields
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasEntryShapeEmitter
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft201909SchemaVersion, JSONSchemaDraft7SchemaVersion, SchemaVersion}
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel.{Encoding, MediaType, Schema}
import amf.plugins.domain.shapes.models.ScalarShape

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
    val optionalTypeEmitter = node.fields.entry(Schema).map{ f =>
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
