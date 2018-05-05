package amf.plugins.document.webapi.parser.spec.oas

import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.contexts.OasSpecEmitterContext
import org.yaml.model.{YDocument, YNode, YScalar, YType}

case class Oas3DocumentEmitter(document: BaseUnit)(implicit override val spec: OasSpecEmitterContext)
    extends OasDocumentEmitter(document) {
  override protected def versionEntry(b: YDocument.EntryBuilder): Unit =
    b.openapi = YNode(YScalar("3.0.0"), YType.Str) // this should not be necessary but for use the same logic
}
