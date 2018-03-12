package amf.model.document

import amf.core.unsafe.PlatformSecrets
import amf.model.domain.DomainElement

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait DeclaresModel extends PlatformSecrets {

  private[amf] def element: amf.core.model.document.DeclaresModel

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  lazy val declares: js.Iterable[amf.model.domain.DomainElement] = {
    val declarations = element.declares.map { e =>
      platform.wrap[amf.model.domain.DomainElement](e)
    }
    declarations.toJSArray
  }

  def withDeclaredElement(declared: DomainElement): this.type = {
    declared.element match {
      case domainElement: DomainElement => element.withDeclaredElement(domainElement)
      case _                            => throw new Exception("Only can declare domain elements") // todo js errors?
    }
    this
  }

  def withDeclares(declares: js.Array[DomainElement]): this.type = {
    val elements: Seq[amf.core.model.domain.DomainElement] = declares.toSeq.map(e => e.element)
    element.withDeclares(elements)
    this
  }
}
