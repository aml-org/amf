package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.plugins.document.apicontract.contexts.emitter.oas.OasSpecEmitterContext
import amf.plugins.document.apicontract.parser.spec.common.DeclarationsEmitterWrapper
import org.yaml.model.{YDocument, YNode, YScalar, YType}

case class Oas3DocumentEmitter(document: BaseUnit)(implicit override val spec: OasSpecEmitterContext)
    extends OasDocumentEmitter(document) {

  override protected def versionEntry(b: YDocument.EntryBuilder): Unit =
    b.openapi = YNode(YScalar("3.0.0"), YType.Str) // this should not be necessary but for use the same logic

  override protected def wrapDeclarations(emitters: Seq[EntryEmitter], ordering: SpecOrdering): Seq[EntryEmitter] =
    Seq(DeclarationsEmitterWrapper(emitters, ordering))

}
