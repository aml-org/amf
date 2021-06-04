package amf.plugins.render

import amf.client.remod.amfcore.config.RenderOptions
import amf.client.remod.amfcore.plugins.render.{AMFRenderPlugin, RenderConfiguration}
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.Vendor
import org.yaml.builder.{DocBuilder, YDocumentBuilder}
import org.yaml.model.YDocument

trait ApiRenderPlugin extends AMFRenderPlugin {

  def vendor: Vendor

  override val id: String = vendor.name

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
