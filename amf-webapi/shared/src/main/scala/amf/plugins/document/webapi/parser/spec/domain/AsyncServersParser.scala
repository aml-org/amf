package amf.plugins.document.webapi.parser.spec.domain
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.domain.webapi.models.{Server, WebApi}
import org.yaml.model.YMap

case class AsyncServersParser(map: YMap, api: WebApi)(implicit val ctx: AsyncWebApiContext) {

  def parse(): Seq[Server] = {
    map.entries.map { entry =>
      AsyncServerParser(api.id, entry.value.as[YMap])
        .parse()
        .withName(entry.key)
    }
  }
}
