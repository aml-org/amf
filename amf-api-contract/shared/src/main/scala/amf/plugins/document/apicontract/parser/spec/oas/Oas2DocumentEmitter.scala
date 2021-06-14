package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.client.scala.model.document.BaseUnit
import amf.plugins.document.apicontract.contexts.emitter.oas.OasSpecEmitterContext
import org.yaml.model._

case class Oas2DocumentEmitter(document: BaseUnit)(implicit override val spec: OasSpecEmitterContext)
    extends OasDocumentEmitter(document) {
  override protected def versionEntry(b: YDocument.EntryBuilder): Unit = b.swagger = YNode(YScalar("2.0"), YType.Str)
}
