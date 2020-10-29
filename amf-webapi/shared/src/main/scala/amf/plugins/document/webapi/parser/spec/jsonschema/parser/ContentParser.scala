package amf.plugins.document.webapi.parser.spec.jsonschema.parser

import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, YMapOps}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.SpecParserOps
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft7SchemaVersion, OasTypeParser, SchemaVersion}
import amf.plugins.document.webapi.parser.spec.toOas
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel.{Encoding, MediaType}
import amf.plugins.domain.shapes.models.ScalarShape
import org.yaml.model.YMap

object ContentParser {
  def apply(adopt: Shape => Unit, version: SchemaVersion): ContentParser = {
    val parserList = if (version == JSONSchemaDraft7SchemaVersion) Seq(ContentEncodingParser, ContentMediaTypeParser)
                    else Seq(ContentEncodingParser, ContentMediaTypeParser, ContentSchemaParser(adopt, version))
    new ContentParser(parserList)
  }
}

case class ContentParser(parsers: Seq[EntryParser[ScalarShape]]){

  def parse(scalar: ScalarShape, map: YMap)(implicit ctx: WebApiContext): Unit = parsers.foreach(_.parse(scalar, map))
}

sealed trait EntryParser[T] {
  def parse(node: T, map: YMap)(implicit ctx: WebApiContext)
}

private[this] object ContentEncodingParser extends EntryParser[ScalarShape] with SpecParserOps {
  override def parse(node: ScalarShape, map: YMap)(implicit ctx: WebApiContext): Unit = map.key("contentEncoding", Encoding in node)
}

private[this] object ContentMediaTypeParser extends EntryParser[ScalarShape] with SpecParserOps {
  override def parse(node: ScalarShape, map: YMap)(implicit ctx: WebApiContext): Unit = map.key("contentMediaType", MediaType in node)
}

private[this] case class ContentSchemaParser(adopt: Shape => Unit, version: SchemaVersion) extends EntryParser[ScalarShape]{
  override def parse(node: ScalarShape, map: YMap)(implicit ctx: WebApiContext): Unit = {
    map.key("contentSchema").foreach { entry =>
      OasTypeParser(entry, adopt, version)(toOas(ctx)).parse().foreach { s =>
        node.set(ScalarShapeModel.Schema, s, Annotations(entry))
      }
    }
  }
}