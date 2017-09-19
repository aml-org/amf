package amf.document

import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.document.BaseUnitModel._

/** Units containing abstract fragments that can be referenced from other fragments */
case class Module(declares: Seq[DomainElement], references: Seq[BaseUnit], location: String, fields: Fields)
    extends BaseUnit
    with DeclaresModel {

  override val annotations: Annotations = Annotations()

  override def adopted(parent: String): this.type = withId(parent)

  /** Returns the usage comment for de element */
  override def usage: String = fields(Usage) //temp
}

trait DeclaresModel {

  /** Declared [[amf.domain.DomainElement]]s that can be re-used from other documents. */
  def declares: Seq[DomainElement]
}
