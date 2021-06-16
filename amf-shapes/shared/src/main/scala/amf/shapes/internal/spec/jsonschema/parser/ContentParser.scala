package amf.shapes.internal.spec.jsonschema.parser

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.shapes.internal.domain.metamodel.ScalarShapeModel
import amf.shapes.internal.domain.metamodel.ScalarShapeModel.{Encoding, MediaType}
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.parser.QuickFieldParserOps
import amf.shapes.internal.spec.common.{JSONSchemaDraft7SchemaVersion, SchemaVersion}
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import org.yaml.model.YMap

object ContentParser {
  def apply(adopt: Shape => Unit, version: SchemaVersion): ContentParser = {
    val parserList =
      if (version == JSONSchemaDraft7SchemaVersion) Seq(ContentEncodingParser, ContentMediaTypeParser)
      else Seq(ContentEncodingParser, ContentMediaTypeParser, ContentSchemaParser(adopt, version))
    new ContentParser(parserList)
  }
}

case class ContentParser(parsers: Seq[EntryParser[ScalarShape]]) {

  def parse(scalar: ScalarShape, map: YMap)(implicit ctx: ShapeParserContext): Unit =
    parsers.foreach(_.parse(scalar, map))
}

sealed trait EntryParser[T] {
  def parse(node: T, map: YMap)(implicit ctx: ShapeParserContext)
}

private[this] object ContentEncodingParser extends EntryParser[ScalarShape] with QuickFieldParserOps {
  override def parse(node: ScalarShape, map: YMap)(implicit ctx: ShapeParserContext): Unit =
    map.key("contentEncoding", Encoding in node)
}

private[this] object ContentMediaTypeParser extends EntryParser[ScalarShape] with QuickFieldParserOps {
  override def parse(node: ScalarShape, map: YMap)(implicit ctx: ShapeParserContext): Unit =
    map.key("contentMediaType", MediaType in node)
}

private[this] case class ContentSchemaParser(adopt: Shape => Unit, version: SchemaVersion)
    extends EntryParser[ScalarShape] {
  override def parse(node: ScalarShape, map: YMap)(implicit ctx: ShapeParserContext): Unit = {
    map.key("contentSchema").foreach { entry =>
      OasTypeParser(entry, adopt, version)(ctx.toOasNext).parse().foreach { s =>
        node.set(ScalarShapeModel.Schema, s, Annotations(entry))
      }
    }
  }
}
