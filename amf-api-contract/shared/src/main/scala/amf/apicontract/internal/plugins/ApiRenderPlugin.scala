package amf.apicontract.internal.plugins

import amf.core.internal.remote.Mimes._
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.plugins.render.{AMFRenderPlugin, RenderConfiguration}
import amf.core.internal.remote.Spec
import org.yaml.builder.{DocBuilder, YDocumentBuilder}
import org.yaml.model.YDocument

trait ApiRenderPlugin extends AMFRenderPlugin {

  def vendor: Spec

  override val id: String = vendor.id

  override def mediaTypes: Seq[String] = Seq(`application/json`, `application/yaml`)

  protected def unparseAsYDocument(unit: BaseUnit,
                                   renderOptions: RenderOptions,
                                   errorHandler: AMFErrorHandler): Option[YDocument]

  override def emit[T](unit: BaseUnit, builder: DocBuilder[T], renderConfiguration: RenderConfiguration): Boolean = {
    builder match {
      case sb: YDocumentBuilder =>
        unparseAsYDocument(unit, renderConfiguration.renderOptions, renderConfiguration.errorHandler) exists { doc =>
          sb.document = doc
          true
        }
      case _ => false
    }
  }
}
