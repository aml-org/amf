package amf.plugins.document.webapi.parser.spec.jsonschema.parser

import amf.core.parser.YMapOps
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.SpecParserOps
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel.{Encoding, MediaType}
import amf.plugins.domain.shapes.models.ScalarShape
import org.yaml.model.YMap

trait StringContentParser{

  def parse(scalar: ScalarShape)
}

case class Draft7StringContentParser(map: YMap)(implicit ctx: WebApiContext) extends StringContentParser with SpecParserOps {
  override def parse(scalar: ScalarShape) = {
    map.key("contentEncoding", Encoding in scalar)
    map.key("contentMediaType", MediaType in scalar)
  }
}
