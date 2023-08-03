package amf.apicontract.internal.spec.common

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.parser.domain.{Declarations, FutureDeclarations, SearchScope}

class ExtensionWebApiDeclarations(
    parentDeclarations: RamlWebApiDeclarations,
    override val alias: Option[String],
    override val errorHandler: AMFErrorHandler,
    override val futureDeclarations: FutureDeclarations
) extends RamlWebApiDeclarations(alias, errorHandler, futureDeclarations) {

  override def findForType(
      key: String,
      map: Declarations => Map[String, DomainElement],
      scope: SearchScope.Scope
  ): Option[DomainElement] = {
    super.findForType(key, map, scope) match {
      case Some(x) => Some(x)
      case None    => parentDeclarations.findForType(key, map, scope)
    }
  }
}
