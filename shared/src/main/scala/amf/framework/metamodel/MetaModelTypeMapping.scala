package amf.framework.metamodel

import amf.framework.metamodel.Obj
import amf.framework.model.document.BaseUnit
import amf.framework.model.domain.DomainElement

trait MetaModelTypeMapping {

  /** Metadata Type references. */
  protected def metaModel(instance: Any): Obj = instance match {
    case baseUnit: BaseUnit           => baseUnit.meta
    case domainElement: DomainElement => domainElement.meta
    case _                            => throw new Exception(s"Missing metadata mapping for $instance")
  }

}
