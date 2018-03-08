package amf.client.model.document

import amf.client.convert.CoreClientConverters._
import amf.client.model.AmfObjectWrapper
import amf.client.model.domain.DomainElement
import amf.core.model.document.{DeclaresModel => InternalDeclaresModel}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait DeclaresModel extends AmfObjectWrapper {

  override private[amf] val _internal: InternalDeclaresModel

  /** Declared DomainElements that can be re-used from other documents. */
  def declares: ClientList[DomainElement] = _internal.declares.asClient

  def withDeclaredElement(declared: DomainElement): this.type = {
    _internal.withDeclaredElement(declared)
    this
  }
}
