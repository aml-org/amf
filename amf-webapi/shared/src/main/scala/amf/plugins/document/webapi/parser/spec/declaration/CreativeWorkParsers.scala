package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, ValueNode}
import amf.plugins.domain.webapi.metamodel.CreativeWorkModel
import amf.plugins.domain.webapi.models.CreativeWork
import org.yaml.model.YMap

/**
  *
  */
case class OasCreativeWorkParser(map: YMap)(
  implicit val ctx: WebApiContext) {
  def parse(): CreativeWork = {

    val creativeWork = CreativeWork(map)

    map.key("url", entry => {
      val value = ValueNode(entry.value)
      creativeWork.set(CreativeWorkModel.Url, value.string(), Annotations(entry))
    })

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      creativeWork.set(CreativeWorkModel.Description, value.string(), Annotations(entry))
    })

    map.key("x-title", entry => {
      val value = ValueNode(entry.value)
      creativeWork.set(CreativeWorkModel.Title, value.string(), Annotations(entry))
    })

    AnnotationParser(() => creativeWork, map).parse()

    creativeWork
  }
}

case class RamlCreativeWorkParser(map: YMap, withExtention: Boolean)(
  implicit val ctx: ParserContext) {
  def parse(): CreativeWork = {

    val documentation = CreativeWork(Annotations(map))

    map.key("title", entry => {
      val value = ValueNode(entry.value)
      documentation.set(CreativeWorkModel.Title, value.string(), Annotations(entry))
    })

    map.key("content", entry => {
      val value = ValueNode(entry.value)
      documentation.set(CreativeWorkModel.Description, value.string(), Annotations(entry))
    })

    if (withExtention)
      map.key("(url)", entry => {
        val value = ValueNode(entry.value)
        documentation.set(CreativeWorkModel.Url, value.string(), Annotations(entry))
      })
    else
      map.key("url", entry => {
        val value = ValueNode(entry.value)
        documentation.set(CreativeWorkModel.Url, value.string(), Annotations(entry))
      })
    documentation
  }
}
