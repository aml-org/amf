package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{BasePathLexicalInformation, HostLexicalInformation, SynthesizedField}
import amf.core.metamodel.Field
import amf.core.model.DataType
import amf.core.model.domain.{DomainElement, AmfArray, AmfScalar}
import amf.core.parser.{Annotations, _}
import amf.core.utils.{AmfStrings, TemplateUri}
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps, RamlScalarNode}
import amf.plugins.document.webapi.parser.spec.oas.Oas3Syntax
import amf.plugins.document.webapi.parser.spec.{toRaml, toOas}
import amf.plugins.domain.webapi.metamodel.{WebApiModel, ServerModel}
import amf.plugins.domain.webapi.models.{Parameter, Server, WebApi}
import amf.validations.ParserSideValidations._
import org.yaml.model.{YType, YMap}

case class RamlServersParser(map: YMap, api: WebApi)(implicit val ctx: RamlWebApiContext) extends SpecParserOps {
  def parse(): Unit = {
    map.key("baseUri") match {
      case Some(entry) =>
        val node   = RamlScalarNode(entry.value)
        val value  = node.text().toString
        val server = api.withServer(value)

        (ServerModel.Url in server).allowingAnnotations(entry)

        checkBalancedParams(value, entry.value, server.id, ServerModel.Url.value.iri(), ctx)
        if (!TemplateUri.isValid(value))
          ctx.eh.violation(InvalidServerPath, api.id, TemplateUri.invalidMsg(value), entry.value)

        map.key("serverDescription".asRamlAnnotation, ServerModel.Description in server)

        parseBaseUriParameters(server, TemplateUri.variables(value))

        api.set(WebApiModel.Servers,
                AmfArray(Seq(server.add(SynthesizedField())), Annotations(entry.value)),
                Annotations(entry))
      case None =>
        map
          .key("baseUriParameters")
          .foreach { entry =>
            ctx.eh.violation(ParametersWithoutBaseUri,
                             api.id,
                             "'baseUri' not defined and 'baseUriParameters' defined.",
                             entry)

            val server = Server().adopted(api.id)
            parseBaseUriParameters(server, Nil)

            api.set(WebApiModel.Servers,
                    AmfArray(Seq(server.add(SynthesizedField())), Annotations(entry.value)),
                    Annotations(entry))
          }
    }

    map.key("servers".asRamlAnnotation).foreach { entry =>
      entry.value.as[Seq[YMap]].map(OasServerParser(api.id, _)(toOas(ctx)).parse()).foreach { server =>
        api.add(WebApiModel.Servers, server)
      }
    }
  }

  private def parseBaseUriParameters(server: Server, orderedVariables: Seq[String]): Unit = {
    val maybeEntry = map.key("baseUriParameters")
    maybeEntry match {
      case Some(entry) =>
        entry.value.tagType match {
          case YType.Map =>
            val parameters =
              RamlParametersParser(entry.value.as[YMap], (p: Parameter) => p.adopted(server.id))
                .parse()
                .map(_.withBinding("path"))

            val flatten: Seq[Parameter] = orderedVariables.map(v =>
              parameters.find(_.name.value().equals(v)) match {
                case Some(p) => p
                case _       => buildParamFromVar(v, server.id)

            })
            val (_, unused) = parameters.partition(flatten.contains(_))
            val finalParams = flatten ++ unused
            server.set(ServerModel.Variables, AmfArray(finalParams, Annotations(entry.value)), Annotations(entry))
            unused.foreach { p =>
              ctx.eh.warning(UnusedBaseUriParameter,
                             p.id,
                             None,
                             s"Unused base uri parameter ${p.name.value()}",
                             p.position(),
                             p.location())
            }
          case YType.Null =>
          case _ =>
            ctx.eh.violation(InvalidBaseUriParametersType, "", "Invalid node for baseUriParameters", entry.value)
        }
      case None =>
        if (orderedVariables.nonEmpty)
          server.set(ServerModel.Variables,
                     AmfArray(orderedVariables.map(buildParamFromVar(_, server.id)), Annotations()),
                     Annotations())
    }

  }

  private def buildParamFromVar(v: String, serverId: String) = {
    val param = Parameter().withName(v).withBinding("path").withRequired(true)
    param.adopted(serverId)
    param.withScalarSchema(v).withDataType(DataType.String)
    param.annotations += SynthesizedField()
    param
  }
}

abstract class OasServersParser(map: YMap, elem: DomainElement, field: Field)(implicit val ctx: OasWebApiContext)
    extends SpecParserOps {
  def parse(): Unit

  protected def parseServers(key: String): Unit =
    map.key(key).foreach { entry =>
      val servers = entry.value.as[Seq[YMap]].map(OasServerParser(elem.id, _).parse())

      elem.set(field, AmfArray(servers, Annotations(entry)), Annotations(entry))
    }
}

case class Oas3ServersParser(map: YMap, elem: DomainElement, field: Field)(implicit override val ctx: OasWebApiContext)
    extends OasServersParser(map, elem, field) {

  override def parse(): Unit = if (ctx.syntax == Oas3Syntax) parseServers("servers")
}

case class Oas2ServersParser(map: YMap, api: WebApi)(implicit override val ctx: OasWebApiContext)
    extends OasServersParser(map, api, WebApiModel.Servers) {
  override def parse(): Unit = {
    if (baseUriExists(map)) {
      var host     = ""
      var basePath = ""

      val annotations = Annotations()

      map.key("basePath").foreach { entry =>
        annotations += BasePathLexicalInformation(Range(entry.range))
        basePath = entry.value.as[String]

        if (!basePath.startsWith("/")) {
          ctx.eh.violation(InvalidBasePath, api.id, "'basePath' property must start with '/'", entry.value)
          basePath = "/" + basePath
        }
      }
      map.key("host").foreach { entry =>
        annotations += HostLexicalInformation(Range(entry.range))
        host = entry.value.as[String]
      }

      val server = Server().set(ServerModel.Url, AmfScalar(host + basePath), annotations)

      map.key("serverDescription".asOasExtension, ServerModel.Description in server)

      map.key(
        "baseUriParameters".asOasExtension,
        entry => {
          val uriParameters =
            RamlParametersParser(entry.value.as[YMap], (p: Parameter) => p.adopted(server.id))(toRaml(ctx))
              .parse()
              .map(_.withBinding("path"))

          server.set(ServerModel.Variables, AmfArray(uriParameters, Annotations(entry.value)), Annotations(entry))
        }
      )

      api.set(WebApiModel.Servers, AmfArray(Seq(server.add(SynthesizedField())), Annotations()))
    }

    parseServers("servers".asOasExtension)
  }

  def baseUriExists(map: YMap): Boolean = map.key("host").orElse(map.key("basePath")).isDefined
}

private case class OasServerParser(parent: String, map: YMap)(implicit val ctx: OasWebApiContext)
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
      ctx.eh.violation(ServerVariableMissingDefault,
                       serverVar.id,
                       "Server variable must define a 'default' field",
                       map)

}
