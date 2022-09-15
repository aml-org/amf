package amf.shapes.internal.plugins.render

import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.vocabulary.NamespaceAliases
import amf.core.internal.plugins.document.graph.emitter.ApplicableMetaFieldRenderProvider
import amf.core.internal.plugins.render.AMFGraphRenderPlugin
import amf.shapes.internal.plugins.document.graph.emitter.FlattenedJsonLdInstanceEmitter
import org.yaml.builder.DocBuilder

object AMFJsonLDSchemaGraphRenderPlugin extends AMFGraphRenderPlugin {

  override def flattenEmissionFN[T]
      : (BaseUnit, DocBuilder[T], RenderOptions, NamespaceAliases, ApplicableMetaFieldRenderProvider) => Boolean =
    FlattenedJsonLdInstanceEmitter.emit

  override def mediaTypes: Seq[String] = Seq(
    "application/schemald+json"
  )
}
