package amf.apicontract.internal.spec.common.parser

import amf.apicontract.client.scala.model.domain.api.{Api, WebApi}
import amf.apicontract.client.scala.model.domain.{Parameter, Server}
import amf.apicontract.internal.metamodel.domain.ServerModel
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.spec.oas.parser.context.{Oas3Syntax, OasWebApiContext}
import amf.apicontract.internal.spec.oas.parser.domain.{OasLikeServerParser, OasServersParser}
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.spec.spec.{toOas, toRaml}
import amf.apicontract.internal.validation.definitions.ParserSideValidations._
import amf.core.client.common.position.Range
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, DomainElement}
import amf.core.internal.annotations.{BasePathLexicalInformation, HostLexicalInformation, SynthesizedField}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.utils.{AmfStrings, TemplateUri}
import amf.shapes.internal.spec.common.parser.{RamlScalarNode, YMapEntryLike}
import org.yaml.model.{YMap, YMapEntry, YType}

case class RamlServersParser(map: YMap, api: WebApi)(implicit val ctx: RamlWebApiContext) extends SpecParserOps {

  def parse(): Unit = {
    map.key("baseUri") match {
      case Some(entry) =>
        val node   = RamlScalarNode(entry.value)
        val value  = node.text().toString
        val server = api.withServer(value)

        (ServerModel.Url in server).allowingAnnotations(entry)

        checkBalancedParams(value, entry.value, server, ServerModel.Url.value.iri(), ctx)
        if (!TemplateUri.isValid(value))
          ctx.eh.violation(InvalidServerPath, api, TemplateUri.invalidMsg(value), entry.value.location)

        map.key("serverDescription".asRamlAnnotation, ServerModel.Description in server)

        val variables = TemplateUri.variables(value)
        checkForUndefinedVersion(entry, variables)
        parseBaseUriParameters(server, TemplateUri.variables(value))

        api.setWithoutId(
          WebApiModel.Servers,
          AmfArray(Seq(server.add(SynthesizedField())), Annotations(entry.value)),
          Annotations(entry)
        )
      case None =>
        map
          .key("baseUriParameters")
          .foreach { entry =>
            ctx.eh.violation(
              ParametersWithoutBaseUri,
              api,
              "'baseUri' not defined and 'baseUriParameters' defined.",
              entry.location
            )

            val server = Server()
            parseBaseUriParameters(server, Nil)

            api.setWithoutId(
              WebApiModel.Servers,
              AmfArray(Seq(server.add(SynthesizedField())), Annotations(entry.value)),
              Annotations(entry)
            )
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
        server.setWithoutId(ServerModel.Variables, AmfArray(finalParams, Annotations(entry.value)), Annotations(entry))
        unused.foreach { p =>
          ctx.eh.warning(
            UnusedBaseUriParameter,
            p,
            None,
            s"Unused base uri parameter ${p.name.value()}",
            p.position(),
            p.location()
          )
        }
      case None if orderedVariables.nonEmpty =>
        server.setWithoutId(ServerModel.Variables, AmfArray(orderedVariables.map(buildParamFromVar(_, server.id))))
      case _ => // ignore
    }

  }

  private def getOrCreateVariableParams(orderedVariables: Seq[String], parameters: Seq[Parameter], server: Server) = {
    orderedVariables.map(v =>
      parameters.find(_.name.value().equals(v)) match {
        case Some(p) => p
        case _       => buildParamFromVar(v, server.id)

      }
    )
  }

  private def parseExplicitParameters(entry: YMapEntry, server: Server) = {
    entry.value.tagType match {
      case YType.Map =>
        RamlParametersParser(entry.value.as[YMap], (p: Parameter) => Unit, binding = "path")
          .parse()
      case YType.Null => Nil
      case _ =>
        ctx.eh.violation(InvalidBaseUriParametersType, "", "Invalid node for baseUriParameters", entry.value.location)
        Nil
    }
  }

  private def buildParamFromVar(v: String, serverId: String) = {
    val param = Parameter().withName(v).syntheticBinding("path").withRequired(true)
    param.withScalarSchema(v).withDataType(DataType.String)
    param.annotations += SynthesizedField()
    param
  }

  private def checkForUndefinedVersion(entry: YMapEntry, variables: Seq[String]): Unit = {
    val webapiHasVersion = map.key("version").isDefined
    if (variables.contains("version") && !webapiHasVersion) {
      ctx.eh.warning(
        ImplicitVersionParameterWithoutApiVersion,
        api,
        "'baseUri' defines 'version' variable without the API defining one",
        entry.location
      )
    }
  }

  private def checkIfVersionParameterIsDefined(
      orderedVariables: Seq[String],
      parameters: Seq[Parameter],
      entry: YMapEntry
  ): Unit = {
    val apiHasVersion          = api.version.option().isDefined
    val versionParameterExists = parameters.exists(_.name.option().exists(name => name.equals("version")))
    if (orderedVariables.contains("version") && versionParameterExists && apiHasVersion) {
      ctx.eh.warning(
        InvalidVersionBaseUriParameterDefinition,
        api,
        "'version' baseUriParameter can't be defined if present in baseUri as variable",
        entry.location
      )
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

      val annotations = Annotations.synthesized()

      map.key("basePath").foreach { entry =>
        annotations += BasePathLexicalInformation(Range(entry.range))
        basePath = entry.value.as[String]

        if (!basePath.startsWith("/")) {
          ctx.eh.violation(InvalidBasePath, api, "'basePath' property must start with '/'", entry.value.location)
          basePath = "/" + basePath
        }
      }
      map.key("host").foreach { entry =>
        annotations += HostLexicalInformation(Range(entry.range))
        host = entry.value.as[String]
      }

      val server = Server(Annotations.virtual()).set(ServerModel.Url, AmfScalar(host + basePath), annotations)

      map.key("serverDescription".asOasExtension, ServerModel.Description in server)

      map.key(
        "baseUriParameters".asOasExtension,
        entry => {
          val uriParameters =
            RamlParametersParser(entry.value.as[YMap], (p: Parameter) => Unit, binding = "path")(toRaml(ctx)).parse()

          server.set(ServerModel.Variables, AmfArray(uriParameters, Annotations(entry.value)), Annotations(entry))
        }
      )

      api.set(WebApiModel.Servers, AmfArray(Seq(server), Annotations.inferred()), Annotations.inferred())
    }

    parseServers("servers".asOasExtension)
  }

  def baseUriExists(map: YMap): Boolean = map.key("host").orElse(map.key("basePath")).isDefined
}
