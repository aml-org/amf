package amf.apicontract.internal.spec.async.parser.domain.declarations

import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.client.scala.model.document.Document
import org.yaml.model.YMap

trait AsyncDeclarationParser {
  def parseDeclarations(map: YMap, parent: String, document: Document)(implicit ctx: AsyncWebApiContext): Unit
}
