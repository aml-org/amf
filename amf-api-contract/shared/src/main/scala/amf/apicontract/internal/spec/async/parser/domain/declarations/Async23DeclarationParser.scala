package amf.apicontract.internal.spec.async.parser.domain.declarations

import amf.aml.internal.parse.common.DeclarationKey
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.async.parser.domain.{Async23EndpointParser, Async23ServerParser}
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.parser.YMapOps
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import amf.core.client.scala.model.document.Document
import org.yaml.model.YMap

object Async23DeclarationParser extends AsyncDeclarationParser {
  override def parseDeclarations(map: YMap, parent: String, document: Document)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {

    parseServerDeclarations(map, parent)
    parseChannelDeclarations(map, parent)

    Async20DeclarationParser().parseDeclarations(map, parent, document)
  }

  private def parseServerDeclarations(componentsMap: YMap, parent: String)(implicit ctx: AsyncWebApiContext): Unit =
    componentsMap.key(
      "servers",
      e => {
        addDeclarationKey(DeclarationKey(e))
        e.value.as[YMap].entries.foreach { entry =>
          val server = new Async23ServerParser(parent, YMapEntryLike(entry)).parse()
          server.add(DeclaredElement())
          ctx.declarations += server
        }
      }
    )

  private def parseChannelDeclarations(componentsMap: YMap, parent: String)(implicit ctx: AsyncWebApiContext): Unit =
    componentsMap.key(
      "channels",
      e => {
        addDeclarationKey(DeclarationKey(e))
        e.value.as[YMap].entries.foreach { entry =>
          val channel = new Async23EndpointParser(entry, parent, Nil).parse()
          channel.foreach(c => {
            c.add(DeclaredElement())
            ctx.declarations += c
          })
        }
      }
    )
}
