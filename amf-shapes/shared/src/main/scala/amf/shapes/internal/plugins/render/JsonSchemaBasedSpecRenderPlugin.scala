package amf.shapes.internal.plugins.render

import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.plugins.render.{RenderConfiguration, RenderInfo, SYAMLBasedRenderPlugin}
import amf.core.internal.remote.Spec
import amf.core.internal.render.BaseEmitters.traverse
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import amf.shapes.client.scala.render.{JsonLDObjectRender, TermNameSyntaxProvider}
import org.yaml.model.YDocument

abstract class JsonSchemaBasedSpecRenderPlugin extends SYAMLBasedRenderPlugin {

  protected def spec: Spec

  override val id: String = s"${spec.id.trim.toLowerCase()}-render-plugin"

  override def applies(element: RenderInfo): Boolean = element.unit match {
    case _: JsonLDInstanceDocument => true
    case _                         => false
  }

  override def defaultSyntax(): String = spec.mediaType

  override def priority: PluginPriority = NormalPriority

  override protected def unparseAsYDocument(
      unit: BaseUnit,
      renderConfig: RenderConfiguration,
      errorHandler: AMFErrorHandler
  ): Option[YDocument] = {

    unit match {
      case jsonLdInstanceDocument: JsonLDInstanceDocument if jsonLdInstanceDocument.encodes.nonEmpty =>
        val encoded = jsonLdInstanceDocument.encodes.head.asInstanceOf[JsonLDObject]
        val emitter = new JsonLDObjectRender(encoded, TermNameSyntaxProvider)
        val document = YDocument { b =>
          traverse(Seq(emitter), b)
        }
        Some(document)
      case _ => None
    }

  }
}
