package amf.plugins.document.apicontract.validation.plugins

import amf.core.model.document.BaseUnit
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectInstance, Vocabulary}

trait AmlAware {

  def isAmlUnit(unit: BaseUnit) =
    unit.isInstanceOf[Dialect] || unit.isInstanceOf[DialectInstance] || unit.isInstanceOf[Vocabulary]
}
