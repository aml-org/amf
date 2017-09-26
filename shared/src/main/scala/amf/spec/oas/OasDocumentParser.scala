package amf.spec.oas

import amf.common.Lazy
import amf.compiler.{ParsedReference, Root}
import amf.document.{BaseUnit, Document}
import amf.domain.Annotation.{
  DeclaredElement,
  DefaultPayload,
  EndPointBodyParameter,
  ExplicitField,
  SingleValueArray,
  _
}
import amf.domain._
import amf.domain.`abstract`.{AbstractDeclaration, ResourceType, Trait}
import amf.domain.extensions.CustomDomainProperty
import amf.metadata.document.BaseUnitModel
import amf.metadata.domain._
import amf.metadata.domain.extensions.CustomDomainPropertyModel
import amf.model.{AmfArray, AmfScalar}
import amf.parser.{YMapOps, YValueOps}
import amf.shape.Shape
import amf.spec.Declarations
import amf.spec.common.BaseSpecParser._
import amf.spec.common._
import amf.vocabulary.VocabularyMappings
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Oas 2.0 spec parser
  */
case class OasDocumentParser(root: Root) extends OasSpecParser(root) {

  def parseDocument(): Document = {

    val document = Document().adopted(root.location)

    root.document.value.foreach(value => {
      val map            = value.toMap
      val declarations   = parseDeclares(map)
      val environmentRef = ReferencesParser(map, root.references).parse()

      parseWebApi(map, Declarations(environmentRef.toMap).add(declarations))
        .map(api => document.withEncodes(api.add(SourceVendor(root.vendor))))

      document
        .adopted(root.location)

      if (declarations.nonEmpty) document.withDeclares(declarations)
      if (environmentRef.nonEmpty) document.withReferences(environmentRef.values.toSeq)
    })
    document
  }

  private def parseWebApi(map: YMap, declarations: Declarations): Option[WebApi] = {

    val api = new Lazy[WebApi](() => WebApi(map).adopted(root.location))

    map.key(
      "info",
      entry => {
        val info = entry.value.value.toMap

        info.key("title", entry => {
          val value = ValueNode(entry.value)
          api.getOrCreate.set(WebApiModel.Name, value.string(), Annotations(entry))
        })

        info.key("description", entry => {
          val value = ValueNode(entry.value)
          api.getOrCreate.set(WebApiModel.Description, value.string(), Annotations(entry))
        })

        info.key("termsOfService", entry => {
          val value = ValueNode(entry.value)
          api.getOrCreate.set(WebApiModel.TermsOfService, value.string(), Annotations(entry))
        })

        info.key("version", entry => {
          val value = ValueNode(entry.value)
          api.getOrCreate.set(WebApiModel.Version, value.string(), Annotations(entry))
        })

        info.key(
          "license",
          entry => {
            val license: License = LicenseParser(entry.value.value.toMap).parse()
            api.getOrCreate.set(WebApiModel.License, license, Annotations(entry))
          }
        )
      }
    )

    map.key("host", entry => {
      val value = ValueNode(entry.value)
      api.getOrCreate.set(WebApiModel.Host, value.string(), Annotations(entry))
    })

    map.key(
      "x-base-uri-parameters",
      entry => {
        val uriParameters =
          HeaderParametersParser(entry.value.value.toMap, api.getOrCreate.withBaseUriParameter, declarations).parse()
        api.getOrCreate.set(WebApiModel.BaseUriParameters,
                            AmfArray(uriParameters, Annotations(entry.value)),
                            Annotations(entry))
      }
    )

    map.key(
      "basePath",
      entry => {
        val value = ValueNode(entry.value)
        api.getOrCreate.set(WebApiModel.BasePath, value.string(), Annotations(entry))
      }
    )

    map.key("consumes", entry => {
      val value = ArrayNode(entry.value.value.toSequence)
      api.getOrCreate.set(WebApiModel.Accepts, value.strings(), Annotations(entry))
    })

    map.key("produces", entry => {
      val value = ArrayNode(entry.value.value.toSequence)
      api.getOrCreate.set(WebApiModel.ContentType, value.strings(), Annotations(entry))
    })

    map.key("schemes", entry => {
      val value = ArrayNode(entry.value.value.toSequence)
      api.getOrCreate.set(WebApiModel.Schemes, value.strings(), Annotations(entry))
    })

    map.key(
      "contact",
      entry => {
        val organization: Organization = OrganizationParser(entry.value.value.toMap).parse()
        api.getOrCreate.set(WebApiModel.Provider, organization, Annotations(entry))
      }
    )

    map.key(
      "externalDocs",
      entry => {
        val creativeWork: CreativeWork = CreativeWorkParser(entry.value.value.toMap).parse()
        api.getOrCreate.set(WebApiModel.Documentation, creativeWork, Annotations(entry))
      }
    )

    map.key(
      "paths",
      entry => {
        val paths = entry.value.value.toMap
        paths.regex(
          "^/.*",
          entries => {
            val endpoints = mutable.ListBuffer[EndPoint]()
            entries.foreach(EndpointParser(_, api.getOrCreate.withEndPoint, endpoints, declarations).parse())
            api.getOrCreate.set(WebApiModel.EndPoints, AmfArray(endpoints), Annotations(entry.value))
          }
        )
      }
    )

    AnnotationParser(() => api.getOrCreate, map).parse()

    api.option
  }
}

case class EndpointParser(entry: YMapEntry,
                          producer: String => EndPoint,
                          collector: mutable.ListBuffer[EndPoint],
                          declarations: Declarations) {

  def parse(): Unit = {

    val endpoint = producer(ValueNode(entry.key).string().value.toString).add(Annotations(entry))
    val map      = entry.value.value.toMap

    map.key("displayName", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Name, value.string(), Annotations(entry))
    })

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Description, value.string(), Annotations(entry))
    })

    var parameters = OasParameters()

    map.key(
      "parameters",
      entry => {
        parameters = ParametersParser(entry.value.value.toSequence, endpoint.id, declarations).parse()
        parameters.body.foreach(_.add(EndPointBodyParameter()))
        parameters match {
          case OasParameters(_, path, _, _) if path.nonEmpty =>
            endpoint.set(EndPointModel.UriParameters, AmfArray(path, Annotations(entry.value)), Annotations(entry))
          case _ =>
        }
      }
    )

    map.key(
      "x-type",
      entry =>
        ParametrizedDeclarationParser(entry.value.value, endpoint.withResourceType, declarations.resourceTypes).parse()
    )

    map.key(
      "x-is",
      entry => {
        entry.value.value.toSequence.values.map(value =>
          ParametrizedDeclarationParser(value, endpoint.withTrait, declarations.traits).parse())
      }
    )

    collector += endpoint

    AnnotationParser(() => endpoint, map).parse()

    map.regex(
      "get|patch|put|post|delete|options|head",
      entries => {
        val operations = mutable.ListBuffer[Operation]()
        entries.foreach(entry => {
          operations += OperationParser(entry, parameters, endpoint.withOperation, declarations).parse()
        })
        endpoint.set(EndPointModel.Operations, AmfArray(operations))
      }
    )
  }
}

case class RequestParser(map: YMap, global: OasParameters, producer: () => Request, declarations: Declarations) {
  def parse(): Option[Request] = {
    val request = new Lazy[Request](producer)

    var parameters = global

    map.key(
      "parameters",
      entry => {
        parameters =
          global.merge(ParametersParser(entry.value.value.toSequence, request.getOrCreate.id, declarations).parse())
        parameters match {
          case OasParameters(query, _, header, _) =>
            if (query.nonEmpty)
              request.getOrCreate.set(RequestModel.QueryParameters,
                                      AmfArray(query, Annotations(entry.value)),
                                      Annotations(entry))
            if (header.nonEmpty)
              request.getOrCreate.set(RequestModel.Headers,
                                      AmfArray(header, Annotations(entry.value)),
                                      Annotations(entry))
        }
      }
    )

    val payloads = mutable.ListBuffer[Payload]()
    parameters.body.foreach(payloads += _)

    map.key(
      "x-request-payloads",
      entry =>
        entry.value.value.toSequence.values.map(value =>
          payloads += PayloadParser(value.toMap, request.getOrCreate.withPayload, declarations).parse())
    )

    if (payloads.nonEmpty) request.getOrCreate.set(RequestModel.Payloads, AmfArray(payloads))

    AnnotationParser(() => request.getOrCreate, map).parse()

    request.option
  }
}

case class OperationParser(entry: YMapEntry,
                           global: OasParameters,
                           producer: String => Operation,
                           declarations: Declarations) {
  def parse(): Operation = {

    val operation = producer(ValueNode(entry.key).string().value.toString).add(Annotations(entry))
    val map       = entry.value.value.toMap

    map.key("operationId", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Name, value.string(), Annotations(entry))
    })

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Description, value.string(), Annotations(entry))
    })

    map.key("deprecated", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Deprecated, value.boolean(), Annotations(entry))
    })

    map.key("summary", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Summary, value.string(), Annotations(entry))
    })

    map.key(
      "externalDocs",
      entry => {
        val creativeWork: CreativeWork = CreativeWorkParser(entry.value.value.toMap).parse()
        operation.set(OperationModel.Documentation, creativeWork, Annotations(entry))
      }
    )

    map.key(
      "schemes",
      entry => {
        val value = ArrayNode(entry.value.value.toSequence)
        operation.set(OperationModel.Schemes, value.strings(), Annotations(entry))
      }
    )

    map.key(
      "x-is",
      entry => {
        val traits = entry.value.value.toSequence.nodes.map(value => {
          ParametrizedDeclarationParser(value.value, operation.withTrait, declarations.traits).parse()
        })
        if (traits.nonEmpty) operation.setArray(DomainElementModel.Extends, traits, Annotations(entry))
      }
    )

    RequestParser(map, global, () => operation.withRequest(), declarations)
      .parse()
      .map(operation.set(OperationModel.Request, _))

    map.key(
      "responses",
      entry => {
        entry.value.value.toMap.regex(
          "default|\\d{3}",
          entries => {
            val responses = mutable.ListBuffer[Response]()
            entries.foreach(entry => {
              responses += ResponseParser(entry, operation.withResponse, declarations).parse()
            })
            operation.set(OperationModel.Responses, AmfArray(responses, Annotations(entry.value)), Annotations(entry))
          }
        )
      }
    )

    AnnotationParser(() => operation, map).parse()

    operation
  }
}

case class ParametersParser(ast: YSequence, parentId: String, declarations: Declarations) {
  def parse(): OasParameters = {
    val parameters = ast.values
      .map(value => ParameterParser(value.toMap, parentId, declarations).parse())

    OasParameters(
      parameters.filter(_.isQuery).map(_.parameter),
      parameters.filter(_.isPath).map(_.parameter),
      parameters.filter(_.isHeader).map(_.parameter),
      parameters.filter(_.isBody).map(_.payload).headOption
    )
  }
}

case class PayloadParser(map: YMap, producer: (Option[String]) => Payload, declarations: Declarations) {
  def parse(): Payload = {

    val payload = producer(
      map.key("mediaType").map(entry => ValueNode(entry.value).string().value.toString)
    ).add(Annotations(map))

    // todo set again for not lose annotations?
    map.key("mediaType",
            entry => payload.set(PayloadModel.MediaType, ValueNode(entry.value).string(), Annotations(entry)))

    map.key(
      "schema",
      entry => {
        OasTypeParser(entry, (shape) => shape.withName("schema").adopted(payload.id), declarations)
          .parse()
          .map(payload.set(PayloadModel.Schema, _, Annotations(entry)))
      }
    )

    AnnotationParser(() => payload, map).parse()

    payload
  }
}

case class ResponseParser(entry: YMapEntry, producer: String => Response, declarations: Declarations) {
  def parse(): Response = {

    val map = entry.value.value.toMap

    val node     = ValueNode(entry.key)
    val response = producer(node.string().value.toString).add(Annotations(entry))

    if (response.name == "default") {
      response.set(ResponseModel.StatusCode, "200")
    } else {
      response.set(ResponseModel.StatusCode, node.string())
    }

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      response.set(ResponseModel.Description, value.string(), Annotations(entry))
    })

    map.key(
      "headers",
      entry => {
        val parameters: Seq[Parameter] =
          HeaderParametersParser(entry.value.value.toMap, response.withHeader, declarations).parse()
        response.set(RequestModel.Headers, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
      }
    )

    val payloads = mutable.ListBuffer[Payload]()

    defaultPayload(map, response.id).foreach(payloads += _)

    map.key(
      "x-response-payloads",
      entry =>
        entry.value.value.toSequence.values.map(value =>
          payloads += PayloadParser(value.toMap, response.withPayload, declarations).parse())
    )

    if (payloads.nonEmpty)
      response.set(ResponseModel.Payloads, AmfArray(payloads))

    AnnotationParser(() => response, map).parse()

    response
  }

  private def defaultPayload(entries: YMap, parentId: String): Option[Payload] = {
    val payload = Payload().add(DefaultPayload())

    entries.key("x-media-type",
                entry => payload.set(PayloadModel.MediaType, ValueNode(entry.value).string(), Annotations(entry)))
    // TODO add parent id to payload?
    payload.adopted(parentId)

    entries.key(
      "schema",
      entry =>
        OasTypeParser(entry, (shape) => shape.withName("default").adopted(payload.id), declarations)
          .parse()
          .map(payload.set(PayloadModel.Schema, _, Annotations(entry)))
    )

    if (payload.fields.nonEmpty) Some(payload) else None
  }
}

case class ParameterParser(map: YMap, parentId: String, declarations: Declarations) {
  def parse(): OasParameter = {
    val parameter = OasParameter(map)

    parameter.parameter.set(ParameterModel.Required, value = false)

    map.key("name", entry => {
      val value = ValueNode(entry.value)
      parameter.parameter.set(ParameterModel.Name, value.string(), Annotations(entry))
    })

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      parameter.parameter.set(ParameterModel.Description, value.string(), Annotations(entry))
    })

    map.key(
      "required",
      entry => {
        val value = ValueNode(entry.value)
        parameter.parameter.set(ParameterModel.Required, value.boolean(), Annotations(entry) += ExplicitField())
      }
    )

    map.key("in", entry => {
      val value = ValueNode(entry.value)
      parameter.parameter.set(ParameterModel.Binding, value.string(), Annotations(entry))
    })

    // TODO generate parameter with parent id or adopt
    if (parameter.isBody) {
      parameter.payload.adopted(parentId)
      map.key(
        "schema",
        entry => {
          OasTypeParser(entry, (shape) => shape.withName("schema").adopted(parameter.payload.id), declarations)
            .parse()
            .map(parameter.payload.set(PayloadModel.Schema, _, Annotations(entry)))
        }
      )

      map.key("x-media-type", entry => {
        val value = ValueNode(entry.value)
        parameter.payload.set(PayloadModel.MediaType, value.string(), Annotations(entry))
      })

    } else {
      // type
      parameter.parameter.adopted(parentId)
      OasTypeParser(map, "", map, shape => shape.withName("schema").adopted(parameter.parameter.id), declarations)
        .parse()
        .map(parameter.parameter.set(ParameterModel.Schema, _, Annotations(map)))
    }

    AnnotationParser(() => parameter.parameter, map).parse()

    parameter
  }
}

case class OasParameters(query: Seq[Parameter] = Nil,
                         path: Seq[Parameter] = Nil,
                         header: Seq[Parameter] = Nil,
                         body: Option[Payload] = None) {
  def merge(inner: OasParameters): OasParameters = {
    OasParameters(merge(query, inner.query),
                  merge(path, inner.path),
                  merge(header, inner.header),
                  merge(body, inner.body))
  }

  private def merge(global: Option[Payload], inner: Option[Payload]): Option[Payload] =
    inner.map(_.add(DefaultPayload())).orElse(global.map(_.copy()))

  private def merge(global: Seq[Parameter], inner: Seq[Parameter]): Seq[Parameter] = {
    val globalMap = global.map(p => p.name -> p.copy().add(Annotation.EndPointParameter())).toMap
    val innerMap  = inner.map(p => p.name  -> p.copy()).toMap

    (globalMap ++ innerMap).values.toSeq
  }
}

case class OasParameter(ast: YMap) {
  val parameter = Parameter(ast)
  val payload   = Payload(ast)

  def isBody: Boolean   = parameter.isBody
  def isQuery: Boolean  = parameter.isQuery
  def isPath: Boolean   = parameter.isPath
  def isHeader: Boolean = parameter.isHeader
}

case class HeaderParametersParser(map: YMap, producer: String => Parameter, declarations: Declarations) {
  def parse(): Seq[Parameter] = {
    map.entries
      .map(entry => HeaderParameterParser(entry, producer, declarations).parse())
  }
}

case class HeaderParameterParser(entry: YMapEntry, producer: String => Parameter, declarations: Declarations) {
  def parse(): Parameter = {

    val name      = entry.key.value.toScalar.text
    val parameter = producer(name).add(Annotations(entry))

    parameter
      .set(ParameterModel.Required, !name.endsWith("?"))
      .set(ParameterModel.Name, ValueNode(entry.key).string())

    val map = entry.value.value.toMap

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      parameter.set(ParameterModel.Description, value.string(), Annotations(entry))
    })

    map.key("required", entry => {
      val value = ValueNode(entry.value)
      parameter.set(ParameterModel.Required, value.boolean(), Annotations(entry) += ExplicitField())
    })

    map.key(
      "type",
      _ => {
        OasTypeParser(entry, (shape) => shape.withName("schema").adopted(parameter.id), declarations)
          .parse()
          .map(parameter.set(ParameterModel.Schema, _, Annotations(entry)))
      }
    )

    AnnotationParser(() => parameter, map).parse()

    parameter
  }
}

case class AnnotationTypesParser(node: YMapEntry, adopt: (CustomDomainProperty) => Unit, declarations: Declarations) {
  def parse(): CustomDomainProperty = {
    val custom         = CustomDomainProperty(node)
    val annotationName = node.key.value.toScalar.text
    custom.withName(annotationName)
    adopt(custom)

    val map = node.value.value.toMap

    map.key(
      "allowedTargets",
      entry => {
        val annotations = Annotations(entry)
        val targets: AmfArray = entry.value.value match {
          case _: YScalar =>
            annotations += SingleValueArray()
            AmfArray(Seq(ValueNode(entry.value).string()))
          case sequence: YSequence =>
            ArrayNode(sequence).strings()
        }

        val targetUris = targets.values.map({
          case s: AmfScalar =>
            VocabularyMappings.ramlToUri.get(s.toString) match {
              case Some(uri) => AmfScalar(uri, s.annotations)
              case None      => s
            }
          case nodeType => AmfScalar(nodeType.toString, nodeType.annotations)
        })

        custom.set(CustomDomainPropertyModel.Domain, AmfArray(targetUris), annotations)
      }
    )

    map.key("displayName", entry => {
      val value = ValueNode(entry.value)
      custom.set(CustomDomainPropertyModel.DisplayName, value.string(), Annotations(entry))
    })

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      custom.set(CustomDomainPropertyModel.Description, value.string(), Annotations(entry))
    })

    map.key(
      "schema",
      entry => {
        OasTypeParser(entry, shape => shape.adopted(custom.id), declarations)
          .parse()
          .foreach({ shape =>
            custom.set(CustomDomainPropertyModel.Schema, shape, Annotations(entry))
          })
      }
    )

    AnnotationParser(() => custom, map).parse()

    custom
  }
}

class OasSpecParser(root: Root) {

  protected def parseDeclares(map: YMap): Seq[DomainElement] = {
    val types = parseTypeDeclarations(map, root.location + "#/declarations")

    types ++
      parseAnnotationTypeDeclarations(map, root.location + "#/declarations", types) ++
      parseResourceTypeDeclarations(map, root.location + "#/declarations") ++
      parseTraitDeclarations(map, root.location + "#/declarations")
  }

  def parseTraitDeclarations(map: YMap, customProperties: String): Seq[AbstractDeclaration] = {
    val traits = ListBuffer[AbstractDeclaration]()

    map.key(
      "x-traits",
      e => {
        e.value.value.toMap.entries.map(traitEntry =>
          traits += AbstractDeclarationParser(Trait(traitEntry), customProperties, traitEntry).parse())
      }
    )

    traits
  }

  def parseResourceTypeDeclarations(map: YMap, customProperties: String): Seq[AbstractDeclaration] = {
    val resourceTypes = ListBuffer[AbstractDeclaration]()

    map.key(
      "x-resourceTypes",
      e => {
        e.value.value.toMap.entries.map(resourceEntry =>
          resourceTypes += AbstractDeclarationParser(ResourceType(resourceEntry), customProperties, resourceEntry)
            .parse())
      }
    )

    resourceTypes
  }

  def parseTypeDeclarations(map: YMap, typesPrefix: String): Seq[Shape] = {
    val types = ListBuffer[Shape]()

    map.key(
      "definitions",
      entry => {

        entry.value.value.toMap.entries.foreach(e => {
          val typeName = e.key.value.toScalar.text
          OasTypeParser(e, shape => shape.withName(typeName).adopted(typesPrefix), Declarations(types))
            .parse() match {
            case Some(shape) =>
              types += shape.add(DeclaredElement())
            case None => throw new Exception(s"Error parsing shape at $typeName")
          }
        })
      }
    )
    types
  }

  def parseAnnotationTypeDeclarations(map: YMap,
                                      customProperties: String,
                                      types: Seq[Shape]): Seq[CustomDomainProperty] = {
    val customDomainProperties = ListBuffer[CustomDomainProperty]()

    map.key(
      "x-annotationTypes",
      e => {
        e.value.value.toMap.entries.map(entry => {
          val typeName = entry.key.value.toScalar.text
          val customProperty = AnnotationTypesParser(entry,
                                                     customProperty =>
                                                       customProperty
                                                         .withName(typeName)
                                                         .adopted(customProperties),
                                                     Declarations(types ++ customDomainProperties)).parse()
          customDomainProperties += customProperty.add(DeclaredElement())
        })
      }
    )

    customDomainProperties
  }

  // producer? whe lose id?
  case class ReferencesParser(map: YMap, rootReferences: Seq[ParsedReference]) {
    def parse(): mutable.Map[String, BaseUnit] = {

      val references = mutable.Map[String, BaseUnit]()

      map.key(
        "x-uses",
        entry =>
          entry.value.value.toMap.entries.foreach(e => {
            target(e.value.value.toScalar.text).foreach(bu => {
              val alias = e.key.value.toScalar.text
              references += alias -> bu
            })
          })
      )

      references
    }

    private def target(originalUrl: String): Option[BaseUnit] =
      rootReferences.find(r => r.parsedUrl.equals(originalUrl)).map(_.baseUnit)
  }

  case class UsageParser(map: YMap, baseUnit: BaseUnit) {
    def parse(): Unit = {
      map.key("x-usage", entry => {
        val value = ValueNode(entry.value)
        baseUnit.set(BaseUnitModel.Usage, value.string(), Annotations(entry))
      })
    }
  }

}
