package amf.plugins.document.apicontract.parser.spec.declaration

import amf.plugins.document.apicontract.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.apicontract.parser.spec.common.SpecParserOps
import amf.plugins.document.apicontract.parser.spec.domain.TagsParser
import amf.plugins.domain.apicontract.models.Tag
import amf.validations.ParserSideValidations.DuplicatedTags
import org.yaml.model.{YMap, YMapEntry, YType}

case class OasLikeTagsParser(parentId: String, entry: YMapEntry)(implicit val ctx: OasLikeWebApiContext)
    extends SpecParserOps {

  def parse(): Seq[Tag] = {
    val tags = entry.value.as[Seq[YMap]].map(tag => TagsParser(tag, (tag: Tag) => tag.adopted(parentId)).parse())
    validateDuplicated(tags, entry)
    tags
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
        ctx.eh.violation(DuplicatedTags, tag.id, s"Tag with name '$name' was found duplicated", tag.annotations)
    }
  }

}
