package amf.client.render

import amf.core.client.platform.errorhandling.ClientErrorHandler
import amf.core.client.platform.model.domain.DomainElement
import amf.core.internal.convert.ClientErrorHandlerConverter.ErrorHandlerConverter
import amf.core.internal.remote.Vendor
import amf.core.internal.render.YNodeDocBuilderPopulator
import amf.plugins.document.apicontract.parser.spec.common.emitters.{
  ApiDomainElementEmitter => InternalDomainElementEmitter
}
import org.yaml.builder.DocBuilder
import amf.client.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("WebApiDomainElementEmitter")
object WebApiDomainElementEmitter {

  def emitToBuilder[T](element: DomainElement,
                       emissionStructure: Vendor,
                       eh: ClientErrorHandler,
                       builder: DocBuilder[T]): Unit = {
    val node = InternalDomainElementEmitter.emit(element, emissionStructure, ErrorHandlerConverter.asInternal(eh))
    YNodeDocBuilderPopulator.populate(node, builder)
  }
}
