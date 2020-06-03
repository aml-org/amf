package amf.plugins.document.webapi.parser.spec.domain

import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.domain.shapes.models.Example
import org.yaml.model.{YMap, YMapEntry, YScalar}
import amf.core.parser.YMapOps
import amf.plugins.domain.shapes.metamodel.ExampleModel

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
    val example = Example(yMapEntry)
    example.adopted(parentId)
    example.set(ExampleModel.MediaType, yMapEntry.key.as[YScalar].text)
    ExampleDataParser(yMapEntry.value, example, ExampleOptions(strictDefault = false, quiet = true)).parse()
  }
}
