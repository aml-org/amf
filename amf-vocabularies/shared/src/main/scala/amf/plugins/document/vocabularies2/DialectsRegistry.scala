package amf.plugins.document.vocabularies2

import amf.core.model.document.BaseUnit
import amf.plugins.document.vocabularies2.model.document.Dialect

class DialectsRegistry {

  protected var map: Map[String, Dialect] = Map()

  def knowsHeader(header: String): Boolean = map.contains(headerKey(header))


  def register(dialect: Dialect): DialectsRegistry = {
    dialect.allHeaders foreach { header => map += (header -> dialect) }
    this
  }

  def withRegisteredDialect(header:String)(k: Dialect => Option[BaseUnit]) = {
    map.get(headerKey(header)) match {
      case Some(dialect) => k(dialect)
      case _             => None
    }
  }

  protected def headerKey(header: String) = header.trim.replace(" ", "")
}
