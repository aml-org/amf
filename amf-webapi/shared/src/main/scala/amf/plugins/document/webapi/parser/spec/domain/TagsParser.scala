package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser._
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.OasLikeCreativeWorkParser
import amf.plugins.domain.webapi.metamodel.TagModel
import amf.plugins.domain.webapi.models.Tag
import amf.validations.ParserSideValidations
import org.yaml.model.{YMap, YNode, YScalar, YSequence}

import scala.collection.mutable

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
    map.key("externalDocs", TagModel.Documentation in tag using (OasLikeCreativeWorkParser.parse(_, tag.id)))

    AnnotationParser(tag, map).parse()

    ctx.closedShape(tag.id, map, "tag")

    tag
  }
}

case class StringTagsParser(seq: YSequence, parentId: String)(implicit ctx: WebApiContext) {

  def parse(): Seq[Tag] = {
    val tags = mutable.ListBuffer[Tag]()
    seq.nodes.foreach { node =>
      node.value match {
        case scalar: YScalar =>
          val tag = Tag(Annotations(scalar)).withName(scalar.text)
          tag.adopted(parentId)
          tags += tag
        case _ =>
          ctx.violation(ParserSideValidations.InvalidTagType, parentId, s"Tag value must be of type string", node)
      }
    }
    tags
  }
}
