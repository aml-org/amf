package amf.apicontract.internal.validation.plugin

import amf.core.client.scala.model.document.BaseUnit
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectInstance, Vocabulary}

trait AmlAware {

  def isAmlUnit(unit: BaseUnit) =
    unit.isInstanceOf[Dialect] || unit.isInstanceOf[DialectInstance] || unit.isInstanceOf[Vocabulary]
}
