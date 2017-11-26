package amf.model.document

import amf.core.model.domain.DomainElement
import amf.core.unsafe.PlatformSecrets

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

trait DeclaresModel extends PlatformSecrets {

  private[amf] def element: amf.core.model.document.DeclaresModel

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  lazy val declares: js.Iterable[DomainElement] = {
    val declarations = element.declares.map { e => platform.wrap(e) }
    declarations.toJSArray
  }

}
