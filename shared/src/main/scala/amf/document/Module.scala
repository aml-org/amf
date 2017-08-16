package amf.document

import amf.domain.{Annotations, DomainElement, Fields}

/** Units containing abstract fragments that can be referenced from other fragments */
case class Module(declares: Seq[DomainElement], references: Seq[BaseUnit], location: String, fields: Fields)
    extends BaseUnit
    with DeclaresModel {

  override val annotations: Annotations = Annotations()
}

trait DeclaresModel {

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  def declares: Seq[DomainElement]
}
