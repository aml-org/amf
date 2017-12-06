package amf.core.parser

import amf.core.model.document.{BaseUnit, ExternalFragment}

case class ParsedReference(unit: BaseUnit, origin: Reference) {
  def isExternalFragment: Boolean = unit.isInstanceOf[ExternalFragment]
}
