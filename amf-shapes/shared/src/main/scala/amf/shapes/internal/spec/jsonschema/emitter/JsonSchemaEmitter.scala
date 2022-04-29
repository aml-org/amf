package amf.shapes.internal.spec.jsonschema.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.internal.plugins.render.{EmptyRenderConfiguration, RenderConfiguration}
import amf.core.internal.render.BaseEmitters.traverse
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.SpecOrdering.Lexical
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

object JsonSchemaEmitter {
  def apply(
      root: Shape,
      declarations: Seq[DomainElement],
      renderConfig: RenderConfiguration,
      errorHandler: AMFErrorHandler
  ) =
    new JsonSchemaEmitter(root, declarations, Lexical, renderConfig, errorHandler)

  def apply(root: Shape, declarations: Seq[DomainElement], options: RenderOptions, errorHandler: AMFErrorHandler) = {
    val renderConfig = EmptyRenderConfiguration(errorHandler, options)
    new JsonSchemaEmitter(root, declarations, Lexical, renderConfig, errorHandler)
  }
}

// TODO improve JsonSchemaEmitter interface
case class JsonSchemaEmitter(
    root: Shape,
    declarations: Seq[DomainElement],
    ordering: SpecOrdering,
    renderConfig: RenderConfiguration,
    errorHandler: AMFErrorHandler
) {

  private val options: RenderOptions = renderConfig.renderOptions

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
      JsonSchemaShapeEmitterContext(errorHandler, schemaVersion, renderConfig)
    else new InlineJsonSchemaShapeEmitterContext(errorHandler, schemaVersion, renderConfig)
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
    val shapes = declarations.collect({ case s: Shape =>
      s
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
