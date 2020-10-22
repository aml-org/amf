package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{BasePathLexicalInformation, HostLexicalInformation, SynthesizedField}
import amf.core.metamodel.Field
import amf.core.model.DataType
import amf.core.model.domain.{AmfArray, AmfScalar, DomainElement}
import amf.core.parser.{Annotations, _}
import amf.core.utils.{AmfStrings, TemplateUri}
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{RamlScalarNode, SpecParserOps, YMapEntryLike}
import amf.plugins.document.webapi.parser.spec.oas.Oas3Syntax
import amf.plugins.document.webapi.parser.spec.{toOas, toRaml}
import amf.plugins.domain.webapi.metamodel.ServerModel
import amf.plugins.domain.webapi.metamodel.api.WebApiModel
import amf.plugins.domain.webapi.models.api.{Api, WebApi}
import amf.plugins.domain.webapi.models.{Parameter, Server}
import amf.validations.ParserSideValidations._
import org.yaml.model.{YMap, YMapEntry, YType}

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

        val variables = TemplateUri.variables(value)
        checkForUndefinedVersion(entry, variables)
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
      entry.value
        .as[Seq[YMap]]
        .map(m => new OasLikeServerParser(api.id, YMapEntryLike(m))(toOas(ctx)).parse())
        .foreach { server =>
          api.add(WebApiModel.Servers, server)
        }
    }
  }

  private def parseBaseUriParameters(server: Server, orderedVariables: Seq[String]): Unit = {
    val maybeEntry = map.key("baseUriParameters")
    maybeEntry match {
      case Some(entry) =>
        val parameters = parseExplicitParameters(entry, server)
        checkIfVersionParameterIsDefined(orderedVariables, parameters, entry)
        val flatten: Seq[Parameter] = getOrCreateVariableParams(orderedVariables, parameters, server)
        val (_, unused)             = parameters.partition(flatten.contains(_))
        val finalParams             = flatten ++ unused
        server.set(ServerModel.Variables, AmfArray(finalParams, Annotations(entry.value)), Annotations(entry))
        unused.foreach { p =>
          ctx.eh.warning(UnusedBaseUriParameter,
                         p.id,
                         None,
                         s"Unused base uri parameter ${p.name.value()}",
                         p.position(),
                         p.location())
        }
      case None if orderedVariables.nonEmpty =>
        server.set(ServerModel.Variables, AmfArray(orderedVariables.map(buildParamFromVar(_, server.id))))
      case _ => // ignore
    }

  }

  private def getOrCreateVariableParams(orderedVariables: Seq[String], parameters: Seq[Parameter], server: Server) = {
    orderedVariables.map(v =>
      parameters.find(_.name.value().equals(v)) match {
        case Some(p) => p
        case _       => buildParamFromVar(v, server.id)

    })
  }

  private def parseExplicitParameters(entry: YMapEntry, server: Server) = {
    entry.value.tagType match {
      case YType.Map =>
        RamlParametersParser(entry.value.as[YMap], (p: Parameter) => p.adopted(server.id))
          .parse()
          .map(_.synthesizedBinding("path"))
      case YType.Null => Nil
      case _ =>
        ctx.eh.violation(InvalidBaseUriParametersType, "", "Invalid node for baseUriParameters", entry.value)
        Nil
    }
  }

  private def buildParamFromVar(v: String, serverId: String) = {
    val param = Parameter().withName(v).synthesizedBinding("path").withRequired(true)
    param.adopted(serverId)
    param.withScalarSchema(v).withDataType(DataType.String)
    param.annotations += SynthesizedField()
    param
  }

  private def checkForUndefinedVersion(entry: YMapEntry, variables: Seq[String]): Unit = {
    val webapiHasVersion = map.key("version").isDefined
    if (variables.contains("version") && !webapiHasVersion) {
      ctx.eh.warning(ImplicitVersionParameterWithoutApiVersion,
                     api.id,
                     "'baseUri' defines 'version' variable without the API defining one",
                     entry)
    }
  }

  private def checkIfVersionParameterIsDefined(orderedVariables: Seq[String],
                                               parameters: Seq[Parameter],
                                               entry: YMapEntry): Unit = {
    val apiHasVersion          = api.version.option().isDefined
    val versionParameterExists = parameters.exists(_.name.option().exists(name => name.equals("version")))
    if (orderedVariables.contains("version") && versionParameterExists && apiHasVersion) {
      ctx.eh.warning(InvalidVersionBaseUriParameterDefinition,
                     api.id,
                     "'version' baseUriParameter can't be defined if present in baseUri as variable",
                     entry)
    }
  }

}

case class Oas3ServersParser(map: YMap, elem: DomainElement, field: Field)(implicit override val ctx: OasWebApiContext)
    extends OasServersParser(map, elem, field) {

  override def parse(): Unit = if (ctx.syntax == Oas3Syntax) parseServers("servers")
}

case class Oas2ServersParser(map: YMap, api: Api)(implicit override val ctx: OasWebApiContext)
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
              .map(_.synthesizedBinding("path"))

          server.set(ServerModel.Variables, AmfArray(uriParameters, Annotations(entry.value)), Annotations(entry))
        }
      )

      api.set(WebApiModel.Servers, AmfArray(Seq(server.add(SynthesizedField())), Annotations()))
    }

    parseServers("servers".asOasExtension)
  }

  def baseUriExists(map: YMap): Boolean = map.key("host").orElse(map.key("basePath")).isDefined
}
