package amf.dialects

import amf.core.model.document.BaseUnit
import amf.core.remote.{Oas20, Raml10, Raml08}
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectInstanceUnit}

object WebApiDialectsRegistry {

  def dialectFor(bu: BaseUnit): Option[Dialect] = {
    bu match {
      case di: DialectInstanceUnit => AMLPlugin.registry.dialectFor(di)
      case _ =>
        bu.sourceVendor match {
          case Some(Oas20)  => Some(OAS20Dialect())
          case Some(Raml10) => Some(RAML10Dialect())
          case Some(Raml08) => Some(RAML08Dialect())
          case _            => None
        }
    }
  }
}
