package amf.plugins.document.webapi.parser.spec.jsonschema

import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.model.document.{EncodesModel, Fragment}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.domain.shapes.models.AnyShape

class JsonSchemaFragmentParser {
  def parse(inputFragment: Fragment, pointer: Option[String])(
    implicit ctx: OasLikeWebApiContext): Option[AnyShape] = {

    val doc: Root = AstFinder.createRootFrom(inputFragment, pointer, ctx.eh)
    val parsingResult = new JsonSchemaParser().parse(doc, ctx, new ParsingOptions())

    parsingResult.collect {
      case encoded: EncodesModel if encoded.encodes.isInstanceOf[AnyShape] => encoded.encodes.asInstanceOf[AnyShape]
    }
  }
}
