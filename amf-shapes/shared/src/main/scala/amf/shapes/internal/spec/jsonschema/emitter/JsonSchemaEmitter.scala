package amf.shapes.internal.spec.jsonschema.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.internal.render.BaseEmitters.traverse
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.emitter.{
  InlineJsonSchemaShapeEmitterContext,
  JsonSchemaShapeEmitterContext,
  OasLikeShapeEmitterContext
}
import amf.shapes.internal.spec.common.{
  JSONSchemaDraft4SchemaVersion,
  JSONSchemaUnspecifiedVersion,
  JSONSchemaVersion,
  SchemaVersion
}
import amf.shapes.internal.spec.oas.emitter
import org.yaml.model.YDocument
import org.yaml.model.YDocument.EntryBuilder

// TODO improve JsonSchemaEmitter interface
case class JsonSchemaEmitter(root: Shape,
                             declarations: Seq[DomainElement],
                             ordering: SpecOrdering = SpecOrdering.Lexical,
                             options: RenderOptions,
                             errorHandler: AMFErrorHandler) {

  def emitDocument(): YDocument = {
    val schemaVersion = SchemaVersion.fromClientOptions(options.schemaVersion)
    val context       = createContextWith(schemaVersion)
    val emitters      = Seq(JsonSchemaEntry(schemaVersion), jsonSchemaRefEntry(context)) ++ sortedTypeEntries(context)
    YDocument(b => {
      b.obj { b =>
        traverse(emitters, b)
      }
    })
  }

  private def createContextWith(schemaVersion: JSONSchemaVersion) = {
    if (options.isWithCompactedEmission)
      JsonSchemaShapeEmitterContext(errorHandler, schemaVersion, options)
    else new InlineJsonSchemaShapeEmitterContext(errorHandler, schemaVersion, options)
  }

  private def jsonSchemaRefEntry(ctx: OasLikeShapeEmitterContext) = new EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val name =
        if (options.isWithCompactedEmission) ctx.definitionsQueue.normalizeName(root.name.option())
        else root.name.value()
      val prefix = s"#${ctx.schemasDeclarationsPath}"
      b.entry("$ref", s"$prefix$name")
    }

    override def position(): Position = Position.ZERO
  }

  private def sortedTypeEntries(ctx: OasLikeShapeEmitterContext) = {
    val shapes = declarations.collect({
      case s: Shape => s
    })
    ordering.sorted(emitter.OasDeclaredShapesEmitter(shapes, SpecOrdering.Lexical, Seq())(ctx).toSeq)
  }

}

case class JsonSchemaEntry(version: JSONSchemaVersion) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val schemaUri = version match {
      case JSONSchemaUnspecifiedVersion => JSONSchemaDraft4SchemaVersion.url
      case _                            => version.url
    }
    b.entry("$schema", schemaUri)
  }

  override def position(): Position = Position.ZERO
}
