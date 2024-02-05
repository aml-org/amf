package amf.apicontract.internal.spec.async.parser.domain.declarations

import amf.aml.internal.parse.common.DeclarationKeyCollector
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.client.scala.model.document.Document
import org.yaml.model.YMap

trait AsyncDeclarationParser extends DeclarationKeyCollector {
  def parseDeclarations(map: YMap, parent: String, document: Document)(implicit ctx: AsyncWebApiContext): Unit = {
    addDeclarationsToModel(document)
  }
}
