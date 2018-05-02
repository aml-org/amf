package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser._
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.OasCreativeWorkParser
import amf.plugins.domain.webapi.metamodel.TagModel
import amf.plugins.domain.webapi.models.Tag
import org.yaml.model.{YMap, YNode}

/**
  *
  */
object TagsParser {
  def apply(map: YNode, adopt: Tag => Tag)(implicit ctx: WebApiContext): TagsParser =
    new TagsParser(map, adopt)(toOas(ctx))
}

class TagsParser(node: YNode, adopt: Tag => Tag)(implicit ctx: WebApiContext) extends SpecParserOps {
  def parse(): Tag = {
    val map = node.as[YMap]
    val tag = Tag(node)

    map.key("name", TagModel.Name in tag)
    adopt(tag)
    map.key("description", TagModel.Description in tag)
    map.key("externalDocs", TagModel.Documentation in tag using OasCreativeWorkParser.parse)

    AnnotationParser(tag, map).parse()

    ctx.closedShape(tag.id, map, "tag")

    tag
  }
}
