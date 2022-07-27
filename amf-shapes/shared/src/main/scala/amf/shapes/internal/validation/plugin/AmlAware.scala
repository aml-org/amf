package amf.shapes.internal.validation.plugin

import amf.aml.client.scala.model.document.{Dialect, DialectInstance, Vocabulary}
import amf.core.client.scala.model.document.BaseUnit

trait AmlAware {

  def isAmlUnit(unit: BaseUnit) =
    unit.isInstanceOf[Dialect] || unit.isInstanceOf[DialectInstance] || unit.isInstanceOf[Vocabulary]
}
