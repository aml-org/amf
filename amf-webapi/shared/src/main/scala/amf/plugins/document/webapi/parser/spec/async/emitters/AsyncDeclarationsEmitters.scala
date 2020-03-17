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

    if (declarations.securitySchemes.nonEmpty)
      result += new AsyncSecuritySchemesEmitter(declarations.securitySchemes.values.toSeq, ordering)

    if (declarations.messageTraits.nonEmpty)
      result += AsyncMessageDeclarationsEmitter(declarations.messageTraits.values.toList, isTrait = true, ordering)

    if (declarations.messages.nonEmpty)
      result += AsyncMessageDeclarationsEmitter(declarations.messages.values.toList, isTrait = false, ordering)

    if (declarations.operationTraits.nonEmpty)
      result += AsyncOperationTraitsDeclarationEmitter(declarations.operationTraits.values.toList, ordering)

    if (declarations.shapes.nonEmpty)
      result += spec.factory.declaredTypesEmitter(declarations.shapes.values.toSeq, references, ordering)

    if (declarations.parameters.nonEmpty)
      result += new AsyncApiParametersEmitter(declarations.parameters.values.toSeq, ordering)

    result
  }
}
