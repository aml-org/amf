package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.Tag
import amf.apicontract.internal.spec.common.parser.SpecParserOps
import amf.apicontract.internal.spec.oas.parser.context.OasLikeWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.DuplicatedTags
import org.yaml.model.{YMap, YMapEntry}

case class OasLikeTagsParser(parentId: String, entry: YMapEntry)(implicit val ctx: OasLikeWebApiContext)
    extends SpecParserOps {

  def parse(): Seq[Tag] = {
    val tags = entry.value.as[Seq[YMap]].map(tag => TagsParser(tag, (tag: Tag) => tag).parse())
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
        ctx.eh.violation(DuplicatedTags, tag, s"Tag with name '$name' was found duplicated", tag.annotations)
    }
  }

}
