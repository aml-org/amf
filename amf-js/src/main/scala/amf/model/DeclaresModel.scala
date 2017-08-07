package amf.model

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait DeclaresModel {

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  val declares: js.Iterable[amf.domain.DomainElement]
}
