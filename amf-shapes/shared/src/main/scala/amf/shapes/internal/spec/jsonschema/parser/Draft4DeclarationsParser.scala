package amf.shapes.internal.spec.jsonschema.parser

import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.parser.{ShapeParserContext, TypeDeclarationParser}
import org.yaml.model.YMap

object Draft4DeclarationsParser {
  def parseTypeDeclarations(map: YMap)(implicit ctx: ShapeParserContext): List[AnyShape] =
    TypeDeclarationParser.parseTypeDeclarations(map, "definitions", None)
}
