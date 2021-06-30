package amf.apicontract.client.platform.render

import amf.core.client.platform.errorhandling.ClientErrorHandler
import amf.core.client.platform.model.domain.DomainElement
import amf.core.internal.convert.ClientErrorHandlerConverter.ErrorHandlerConverter
import amf.core.internal.remote.Vendor
import amf.core.internal.render.YNodeDocBuilderPopulator
import org.yaml.builder.DocBuilder
import amf.apicontract.client.scala.render.{ApiDomainElementEmitter => InternalApiDomainElementEmitter}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("ApiDomainElementEmitter")
object ApiDomainElementEmitter {

  def emitToBuilder[T](element: DomainElement,
                       mediaType: String,
                       eh: ClientErrorHandler,
                       builder: DocBuilder[T]): Unit = {
    val node = InternalApiDomainElementEmitter.emit(element, mediaType, ErrorHandlerConverter.asInternal(eh))
    YNodeDocBuilderPopulator.populate(node, builder)
  }
}
