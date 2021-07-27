package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.Tag
import amf.apicontract.internal.metamodel.domain.TagModel
import amf.apicontract.internal.spec.common.parser.{SpecParserOps, WebApiContext, WebApiShapeParserContextAdapter}
import amf.apicontract.internal.spec.spec.toOas
import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.internal.spec.common.parser.{AnnotationParser, OasLikeCreativeWorkParser}
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
    map.key("externalDocs",
            TagModel.Documentation in tag using (OasLikeCreativeWorkParser.parse(_, tag.id)(
              WebApiShapeParserContextAdapter(ctx))))

    AnnotationParser(tag, map)(WebApiShapeParserContextAdapter(ctx)).parse()

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
          ctx.eh.violation(ParserSideValidations.InvalidTagType, parentId, s"Tag value must be of type string", node.location)
      }
    }
    tags
  }
}
