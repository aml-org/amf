package amf.plugins.document.apicontract.parser.spec.async.emitters
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.parse.document.EmptyFutureDeclarations
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.unsafe.PlatformSecrets
import amf.plugins.document.apicontract.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.apicontract.parser.spec.WebApiDeclarations

import scala.collection.mutable.ListBuffer

case class AsyncDeclarationsEmitters(declares: Seq[DomainElement], ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasLikeSpecEmitterContext)
    extends PlatformSecrets {
  val emitters: Seq[EntryEmitter] = {

    val declarations = WebApiDeclarations(declares, UnhandledErrorHandler, EmptyFutureDeclarations())

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

    if (declarations.correlationIds.nonEmpty)
      result += AsyncCorrelationIdDeclarationsEmitter(declarations.correlationIds.values.toSeq, ordering)

    if (declarations.serverBindings.nonEmpty)
      result += AsyncApiBindingsDeclarationEmitter("serverBindings",
                                                   declarations.serverBindings.values.toSeq,
                                                   ordering)

    if (declarations.messageBindings.nonEmpty)
      result += AsyncApiBindingsDeclarationEmitter("messageBindings",
                                                   declarations.messageBindings.values.toSeq,
                                                   ordering)

    if (declarations.channelBindings.nonEmpty)
      result += AsyncApiBindingsDeclarationEmitter("channelBindings",
                                                   declarations.channelBindings.values.toSeq,
                                                   ordering)

    if (declarations.operationBindings.nonEmpty)
      result += AsyncApiBindingsDeclarationEmitter("operationBindings",
                                                   declarations.operationBindings.values.toSeq,
                                                   ordering)

    result
  }
}
