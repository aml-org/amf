package amf.model.document

import amf.core.unsafe.PlatformSecrets
import amf.model.domain.DomainElement

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
trait EncodesModel extends PlatformSecrets {

  /** Encoded [[DomainElement]] described in the document element. */
  private[amf] val element: amf.core.model.document.EncodesModel

  /** Encoded [[DomainElement]] described in the document element. */
  lazy val encodes: DomainElement = platform.wrap[DomainElement](element.encodes)
  def withEncodes(encodes: DomainElement) = element.withEncodes(encodes.element)
}
