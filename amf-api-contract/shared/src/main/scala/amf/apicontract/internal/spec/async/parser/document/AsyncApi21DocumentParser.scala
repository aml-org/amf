package amf.apicontract.internal.spec.async.parser.document

import amf.apicontract.client.scala.model.domain.api.AsyncApi
import amf.apicontract.internal.spec.async.{AsyncApi21, AsyncApi22}
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.async.parser.domain.declarations.{
  Async21DeclarationParser,
  Async22DeclarationParser,
  AsyncDeclarationParser
}
import amf.core.internal.parser.Root
import amf.core.internal.remote.Spec
import org.yaml.model.YMap

object AsyncApi21DocumentParser {
  def apply(root: Root)(implicit ctx: AsyncWebApiContext): AsyncApi21DocumentParser =
    AsyncApi21DocumentParser(root, AsyncApi21, Async21DeclarationParser())
}

case class AsyncApi21DocumentParser(root: Root, spec: Spec, declarationParser: AsyncDeclarationParser)(
    override implicit val ctx: AsyncWebApiContext
) extends AsyncApiDocumentParser(root, spec, declarationParser)(ctx) {

  override protected def parseApi(map: YMap): AsyncApi = {
    super.parseApi(map)
    // TODO: add stuff here
  }
}
