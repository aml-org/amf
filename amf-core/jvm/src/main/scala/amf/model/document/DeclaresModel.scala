package amf.model.document

import amf.core.model.document
import amf.core.unsafe.PlatformSecrets
import amf.model.domain.DomainElement

import scala.collection.JavaConverters._
import scala.language.postfixOps

trait DeclaresModel extends PlatformSecrets {

  private[amf] def element: document.DeclaresModel

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  lazy val declares: java.util.List[DomainElement] = {
    val declarations = element.declares.map { e =>
      platform.wrap[DomainElement](e)
    }
    declarations.asJava
  }

  def withDeclaredElement(declared: DomainElement): this.type = {
    element.withDeclaredElement(declared.element)
    this
  }

  def withDeclares(declares: java.util.List[DomainElement]): this.type = {
    val elements: Seq[amf.core.model.domain.DomainElement] = declares.asScala.map(e => e.element)
    element.withDeclares(elements)
    this
  }
}
