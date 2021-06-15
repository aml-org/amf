package amf.plugins.document.apicontract.parser.spec.domain

import amf.core.internal.parser.YMapOps
import amf.shapes.internal.spec.contexts.WebApiContext
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.declaration.common.YMapEntryLike

import org.yaml.model.{YMap, YMapEntry, YScalar}

import scala.collection.mutable.ListBuffer

case class ExamplesByMediaTypeParser(entry: YMapEntry, parentId: String)(implicit ctx: WebApiContext) {
  def parse(): Seq[Example] = {
    val results = ListBuffer[Example]()
    entry.value
      .as[YMap]
      .regex(".*/.*")
      .map(e => results += ExampleByMediaTypeParser(e, parentId).parse())

    results
  }
}

case class ExampleByMediaTypeParser(yMapEntry: YMapEntry, parentId: String)(implicit ctx: WebApiContext) {
  def parse(): Example = {
    val example   = Example(yMapEntry)
    val mediaType = yMapEntry.key.as[YScalar].text
    example.withName(mediaType).adopted(parentId)
    example.set(ExampleModel.MediaType, mediaType)
    ExampleDataParser(YMapEntryLike(yMapEntry.value), example, ExampleOptions(strictDefault = false, quiet = true))(
      WebApiShapeParserContextAdapter(ctx))
      .parse()
  }
}
