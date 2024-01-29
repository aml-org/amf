package amf.apicontract.internal.spec.async.parser.domain.declarations

import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import org.yaml.model.YMap

trait AsyncDeclarationParser {
  def parseDeclarations(map: YMap, parent: String)(implicit ctx: AsyncWebApiContext): Unit
}
