package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.internal.spec.common.parser.{WebApiContext, WebApiShapeParserContextAdapter}
import amf.core.internal.parser.YMapOps
import amf.shapes.client.scala.model.domain.Example
import amf.shapes.internal.domain.metamodel.ExampleModel
import amf.shapes.internal.spec.common.parser.{ExampleDataParser, ExampleOptions, YMapEntryLike}
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
    example.withName(mediaType)
    example.set(ExampleModel.MediaType, mediaType)
    ExampleDataParser(YMapEntryLike(yMapEntry.value), example, ExampleOptions(strictDefault = false, quiet = true))(
      WebApiShapeParserContextAdapter(ctx)
    )
      .parse()
  }
}
