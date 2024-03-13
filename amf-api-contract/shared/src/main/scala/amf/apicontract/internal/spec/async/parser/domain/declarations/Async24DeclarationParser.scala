package amf.apicontract.internal.spec.async.parser.domain.declarations

import amf.aml.internal.parse.common.DeclarationKey
import amf.aml.internal.parse.dialects.DialectAstOps.DialectYMapOps
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.async.parser.domain.{Async20ServerVariableParser, Async24ServerVariableParser}
import amf.core.client.scala.model.document.Document
import amf.core.internal.annotations.{DeclaredElement, DeclaredServerVariable}
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import org.yaml.model.YMap

object Async24DeclarationParser extends AsyncDeclarationParser {
  override def parseDeclarations(map: YMap, parent: String, document: Document)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    parseServerVariableDeclarations(map, parent)
    Async23DeclarationParser.parseDeclarations(map, parent, document)
  }

  private def parseServerVariableDeclarations(componentsMap: YMap, parent: String)(implicit ctx: AsyncWebApiContext): Unit =
    componentsMap.key(
      "serverVariables",
      entry => {
        addDeclarationKey(DeclarationKey(entry))
        entry.value.as[YMap].entries.foreach { entry =>
           val serverVariable = ctx.factory.serverVariableParser(entry, parent).parse()
          serverVariable.add(DeclaredElement())
          serverVariable.add(DeclaredServerVariable())
          ctx.declarations += serverVariable
        }
      }
    )

}
