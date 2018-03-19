package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.parser.{Annotations, _}
import amf.core.remote.{Oas, Raml08, Raml10}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel
import amf.plugins.domain.shapes.models.CreativeWork
import org.yaml.model.{YMap, YNode}

object OasCreativeWorkParser {
  def parse(node: YNode)(implicit ctx: WebApiContext): CreativeWork = OasCreativeWorkParser(node.as[YMap]).parse()
}

/**
  *
  */
case class OasCreativeWorkParser(map: YMap)(implicit val ctx: WebApiContext) extends SpecParserOps {
  def parse(): CreativeWork = {

    val creativeWork = CreativeWork(map)

    map.key("url", CreativeWorkModel.Url in creativeWork)
    map.key("description", CreativeWorkModel.Description in creativeWork)
    map.key("x-title", CreativeWorkModel.Title in creativeWork)

    AnnotationParser(creativeWork, map).parse()

    creativeWork
  }
}

case class RamlCreativeWorkParser(map: YMap)(implicit val ctx: WebApiContext) extends SpecParserOps {
  def parse(): CreativeWork = {

    val documentation = CreativeWork(Annotations(map))

    map.key("title", (CreativeWorkModel.Title in documentation).allowingAnnotations)
    map.key("content", (CreativeWorkModel.Description in documentation).allowingAnnotations)

    val url = ctx.vendor match {
      case Oas             => "url"
      case Raml08 | Raml10 => "(url)"
      case other           => throw new Exception(s"Unexpected vendor '$other'")
    }

    map.key(url, CreativeWorkModel.Url in documentation)

    AnnotationParser(documentation, map).parse()

    documentation
  }
}
