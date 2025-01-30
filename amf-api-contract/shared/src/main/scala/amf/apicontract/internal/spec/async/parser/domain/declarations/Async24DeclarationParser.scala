package amf.apicontract.internal.spec.async.parser.domain.declarations

import amf.aml.internal.parse.common.DeclarationKey
import amf.aml.internal.parse.dialects.DialectAstOps.DialectYMapOps
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.client.scala.model.document.Document
import amf.core.internal.annotations.{DeclaredElement, DeclaredServerVariable}
import org.yaml.model.YMap

object Async24DeclarationParser extends AsyncDeclarationParser {

  override def parseDeclarations(map: YMap, parent: String, document: Document)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    this.parseOnlyDeclarations(map, parent, document)
    addDeclarationsToModel(document)
  }

  override def parseOnlyDeclarations(map: YMap, parent: String, document: Document)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    parseServerVariableDeclarations(map, parent)
    Async23DeclarationParser.parseOnlyDeclarations(map, parent, document)
  }

  private def parseServerVariableDeclarations(componentsMap: YMap, parent: String)(implicit
      ctx: AsyncWebApiContext
  ): Unit =
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
