package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.model.domain.AmfArray
import amf.core.parser.Annotations
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.parser.spec.common.SpecParserOps
import amf.plugins.document.webapi.parser.spec.domain.TagsParser
import amf.plugins.domain.webapi.metamodel.WebApiModel
import amf.plugins.domain.webapi.models.{WebApi, Tag}
import amf.validations.ParserSideValidations.DuplicatedTags
import org.yaml.model.{YType, YMap, YMapEntry}

case class OasLikeTagsParser(entry: YMapEntry, api: WebApi)(implicit val ctx: OasLikeWebApiContext)
    extends SpecParserOps {

  def parse(): Unit = {
    entry.value.tagType match {
      case YType.Seq =>
        val tags = entry.value.as[Seq[YMap]].map(tag => TagsParser(tag, (tag: Tag) => tag.adopted(api.id)).parse())
        validateDuplicated(tags, entry)
        api.set(WebApiModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
      case _ => // ignore
    }
  }

  private def validateDuplicated(tags: Seq[Tag], entry: YMapEntry): Unit = {
    val groupedByName = tags
      .flatMap { tag =>
        tag.name.option().map(_ -> tag)
      }
      .groupBy { case (name, _) => name }
    val namesWithTag = groupedByName.collect { case (_, ys) if ys.lengthCompare(1) > 0 => ys.tail }.flatten
    namesWithTag.foreach {
      case (name, tag) =>
        ctx.violation(DuplicatedTags, tag.id, s"Tag with name '$name' was found duplicated", tag.annotations)
    }
  }

}
