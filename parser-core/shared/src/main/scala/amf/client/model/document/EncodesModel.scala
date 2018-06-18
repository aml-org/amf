package amf.client.model.document

import amf.client.convert.CoreClientConverters._
import amf.client.model.AmfObjectWrapper
import amf.client.model.domain.DomainElement
import amf.core.model.document.{EncodesModel => InternalEncodesModel}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait EncodesModel extends AmfObjectWrapper {

  override private[amf] val _internal: InternalEncodesModel

  /** Encoded [[DomainElement]] described in the document element. */
  def encodes: DomainElement = _internal.encodes

  def withEncodes(encoded: DomainElement): this.type = {
    _internal.withEncodes(encoded)
    this
  }
}
