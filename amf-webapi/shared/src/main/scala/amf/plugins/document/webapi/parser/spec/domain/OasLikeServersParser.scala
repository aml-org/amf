package amf.plugins.document.webapi.parser.spec.domain
import org.yaml.model.YMap
import amf.core.metamodel.Field
import amf.plugins.document.webapi.parser.spec.common.{SpecParserOps, AnnotationParser}
import amf.core.parser.Annotations
import amf.core.model.domain.{DomainElement, AmfArray}
import amf.core.parser.YMapOps
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.parser.spec.toRaml
import amf.plugins.domain.webapi.metamodel.ServerModel
import amf.plugins.domain.webapi.models.{Server, Parameter}
import amf.validations.ParserSideValidations.ServerVariableMissingDefault

abstract class OasLikeServersParser(map: YMap, elem: DomainElement, field: Field)(implicit val ctx: OasLikeWebApiContext)
  extends SpecParserOps {
  def parse(): Unit

  protected def parseServers(key: String): Unit =
    map.key(key).foreach { entry =>
      val servers = entry.value.as[Seq[YMap]].map(OasLikeServerParser(elem.id, _).parse())

      elem.set(field, AmfArray(servers, Annotations(entry)), Annotations(entry))
    }
}

case class OasLikeServerParser(parent: String, map: YMap)(implicit val ctx: OasLikeWebApiContext)
  extends SpecParserOps {
  def parse(): Server = {
    val server = Server(map)

    map.key("url", ServerModel.Url in server)

    server.adopted(parent)

    map.key("description", ServerModel.Description in server)

    map.key("variables").foreach { entry =>
      val variables = entry.value.as[YMap].entries.map { varEntry =>
        val serverVariable =
          Raml10ParameterParser(varEntry, (p: Parameter) => p.adopted(server.id))(toRaml(ctx)).parse()
        serverVariable.withBinding("path")
        // required field is validated in parsing as there is no way to differentiate a server variable from a parameter
        requiredDefaultField(serverVariable, varEntry.value.as[YMap])
        serverVariable
      }
      server.set(ServerModel.Variables, AmfArray(variables, Annotations(entry.value)), Annotations(entry))
    }

    AnnotationParser(server, map).parse()
    ctx.closedShape(server.id, map, "server")
    server
  }

  private def requiredDefaultField(serverVar: Parameter, map: YMap): Unit =
    if (map.key("default").isEmpty)
      ctx.violation(ServerVariableMissingDefault, serverVar.id, "Server variable must define a 'default' field", map)

}