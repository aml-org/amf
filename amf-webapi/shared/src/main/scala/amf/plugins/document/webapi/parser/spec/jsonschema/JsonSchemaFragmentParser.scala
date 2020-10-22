package amf.plugins.document.webapi.parser.spec.jsonschema

import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.model.document.{EncodesModel, Fragment}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YNode

class JsonSchemaFragmentParser {
  def parse(inputFragment: Fragment, pointer: Option[String])(
    implicit ctx: OasLikeWebApiContext): Option[AnyShape] = {

    val encoded: YNode = new AstFinder().getYNode(inputFragment, ctx)
    val doc: Root      = new AstFinder().getRoot(inputFragment, pointer, encoded)

    new JsonSchemaParser().parse(doc, ctx, new ParsingOptions()).flatMap { parsed =>
      parsed match {
        case encoded: EncodesModel if encoded.encodes.isInstanceOf[AnyShape] =>
          Some(encoded.encodes.asInstanceOf[AnyShape])
        case _ => None
      }
    }
  }
}
