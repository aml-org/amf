package amf.document

import amf.domain.DomainElement
import amf.remote.URL

/** Units containing abstract fragments that can be referenced from other fragments */
case class Module(declares: Seq[DomainElement], references: Seq[URL], location: URL) extends DeclaresModel

trait DeclaresModel {

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  def declares(): Seq[DomainElement]
}
