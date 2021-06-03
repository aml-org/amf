package amf.client.render

import amf.client.convert.CoreClientConverters._
import amf.client.model.domain.DomainElement
import amf.client.resolve.ClientErrorHandler
import amf.client.resolve.ClientErrorHandlerConverter.ErrorHandlerConverter
import amf.core.emitter.YNodeDocBuilderPopulator
import amf.core.remote.Vendor
import amf.plugins.document.apicontract.parser.spec.common.emitters.{
  ApiDomainElementEmitter => InternalDomainElementEmitter
}
import org.yaml.builder.DocBuilder

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
