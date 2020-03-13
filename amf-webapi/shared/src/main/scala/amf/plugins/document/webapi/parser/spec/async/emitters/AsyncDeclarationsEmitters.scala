package amf.plugins.document.webapi.parser.spec.async.emitters
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.DomainElement
import amf.core.parser.EmptyFutureDeclarations
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations

import scala.collection.mutable.ListBuffer

case class AsyncDeclarationsEmitters(declares: Seq[DomainElement], ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasLikeSpecEmitterContext)
    extends PlatformSecrets {
  val emitters: Seq[EntryEmitter] = {

    val declarations = WebApiDeclarations(declares, UnhandledParserErrorHandler, EmptyFutureDeclarations())

    val result = ListBuffer[EntryEmitter]()

    if (declarations.shapes.nonEmpty)
      result += spec.factory.declaredTypesEmitter(declarations.shapes.values.toSeq, references, ordering)

    if (declarations.securitySchemes.nonEmpty)
      result += new AsyncSecuritySchemesEmitter(declarations.securitySchemes.values.toSeq, ordering)
    result
  }
}
