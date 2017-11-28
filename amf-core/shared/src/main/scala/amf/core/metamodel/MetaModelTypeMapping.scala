package amf.core.metamodel

import amf.core.model.document.BaseUnit
import amf.core.model.domain.DomainElement

trait MetaModelTypeMapping {

  /** Metadata Type references. */
  protected def metaModel(instance: Any): Obj = instance match {
    case baseUnit: BaseUnit           => baseUnit.meta
    case domainElement: DomainElement => domainElement.meta
    case _                            => throw new Exception(s"Missing metadata mapping for $instance")
  }

}
