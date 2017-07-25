package amf.document

import amf.domain.{DomainElement, Fields}

/** Units containing abstract fragments that can be referenced from other fragments */
case class Module(declares: Seq[DomainElement], references: Seq[BaseUnit], location: String, fields: Fields)
    extends BaseUnit
    with DeclaresModel

trait DeclaresModel {

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  val declares: Seq[DomainElement]
}
