package amf.document

import java.net.URL

import amf.domain.DomainElement

/** Units containing abstract fragments that can be referenced from other fragments */
case class Module(declares: List[DomainElement], references: List[URL], location: URL) extends DeclaresModel

trait DeclaresModel {

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  def declares(): List[DomainElement]
}
