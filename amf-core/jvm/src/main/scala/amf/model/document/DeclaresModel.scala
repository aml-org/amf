package amf.model.document

import amf.core.unsafe.PlatformSecrets
import amf.core.model.document
import amf.model.domain.DomainElement

import scala.language.postfixOps
import scala.collection.JavaConverters._

trait DeclaresModel extends PlatformSecrets {

  private[amf] def element: document.DeclaresModel

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  lazy val declares: java.util.List[DomainElement] = {
    val declarations = element.declares.map { e => platform.wrap(e) }
    declarations.asJava
  }

}
