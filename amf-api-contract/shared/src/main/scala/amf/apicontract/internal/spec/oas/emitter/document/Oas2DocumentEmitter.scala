package amf.apicontract.internal.spec.oas.emitter.document

import amf.apicontract.internal.spec.oas.emitter.context.OasSpecEmitterContext
import amf.core.client.scala.model.document.BaseUnit
import org.yaml.model.{YDocument, YNode, YScalar, YType}

case class Oas2DocumentEmitter(document: BaseUnit)(implicit override val spec: OasSpecEmitterContext)
    extends OasDocumentEmitter(document) {
  override protected def versionEntry(b: YDocument.EntryBuilder): Unit = b.swagger = YNode(YScalar("2.0"), YType.Str)
}
