package amf.spec.raml

import amf.common.Lazy
import amf.compiler.{ParsedReference, Root}
import amf.document.{BaseUnit, Document}
import amf.domain.Annotation._
import amf.domain._
import amf.domain.extensions.CustomDomainProperty
import amf.metadata.document.BaseUnitModel
import amf.metadata.domain.EndPointModel.Path
import amf.metadata.domain.OperationModel.Method
import amf.metadata.domain._
import amf.metadata.domain.extensions.CustomDomainPropertyModel
import amf.model.{AmfArray, AmfElement, AmfScalar}
import amf.parser.{YMapOps, YValueOps}
import amf.shape.Shape
import amf.spec.common.BaseSpecParser._
import amf.spec.common._
import amf.spec.{BaseUriSplitter, Declarations}
import amf.vocabulary.VocabularyMappings
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Raml 1.0 spec parser
  */
case class RamlDocumentParser(override val root: Root) extends RamlSpecParser(root) {

  def parseDocument(): Document = {

    val document = Document().adopted(root.location)

    root.document.value.foreach(value => {
      val map = value.toMap

      val declarations  = parseDeclares(map)
      val environmentRef = ReferencesParser(map, root.references).parse()

      val api = parseWebApi(map, Declarations(declarations, environmentRef.toMap)).add(SourceVendor(root.vendor))

      document
        .withEncodes(api)

      if (declarations.nonEmpty) document.withDeclares(declarations)
      if (environmentRef.nonEmpty) document.withReferences(environmentRef.values.toSeq)
    })
    document
  }

  private def parseWebApi(map: YMap, declarations: Declarations): WebApi = {

    val api = WebApi(map).adopted(root.location)

    map.key("title", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Name, value.string(), Annotations(entry))
    })

    map.key(
      "baseUriParameters",
      entry => {
        val parameters: Seq[Parameter] =
          ParametersParser(entry.value.value.toMap, api.withBaseUriParameter, declarations)
            .parse()
            .map(_.withBinding("path"))
        api.set(WebApiModel.BaseUriParameters, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Description, value.string(), Annotations(entry))
    })

    map.key(
      "mediaType",
      entry => {
        val annotations = Annotations(entry)
        val value: AmfElement = entry.value.value match {
          case _: YScalar =>
            annotations += SingleValueArray()
            AmfArray(Seq(ValueNode(entry.value).string()))
          case _: YSequence =>
            ArrayNode(entry.value.value.toSequence).strings()
        }

        api.set(WebApiModel.ContentType, value, annotations)
        api.set(WebApiModel.Accepts, value, annotations)
      }
    )

    map.key("version", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Version, value.string(), Annotations(entry))
    })

    map.key("(termsOfService)", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.TermsOfService, value.string(), Annotations(entry))
    })

    map.key("protocols", entry => {
      entry.value.value match {
        case _: YScalar =>
          api.set(WebApiModel.Schemes, AmfArray(Seq(ValueNode(entry.value).string())), Annotations(entry))
        case _: YSequence =>
          val value = ArrayNode(entry.value.value.toSequence)
          api.set(WebApiModel.Schemes, value.strings(), Annotations(entry))
      }
    })

    map.key(
      "(contact)",
      entry => {
        val organization: Organization = OrganizationParser(entry.value.value.toMap).parse()
        api.set(WebApiModel.Provider, organization, Annotations(entry))
      }
    )

    map.key(
      "(externalDocs)",
      entry => {
        val creativeWork: CreativeWork = CreativeWorkParser(entry.value.value.toMap).parse()
        api.set(WebApiModel.Documentation, creativeWork, Annotations(entry))
      }
    )

    map.key("(license)", entry => {
      val license: License = LicenseParser(entry.value.value.toMap).parse()
      api.set(WebApiModel.License, license, Annotations(entry))
    })

    map.regex(
      "^/.*",
      entries => {
        val endpoints = mutable.ListBuffer[EndPoint]()
        entries.foreach(entry => EndpointParser(entry, api.withEndPoint, None, endpoints, declarations).parse())
        api.set(WebApiModel.EndPoints, AmfArray(endpoints))
      }
    )

    map.key(
      "baseUri",
      entry => {
        val value = ValueNode(entry.value)
        val uri   = BaseUriSplitter(value.string().value.toString)

        if (api.schemes.isEmpty && uri.protocol.nonEmpty) {
          api.set(WebApiModel.Schemes,
                  AmfArray(Seq(AmfScalar(uri.protocol)), Annotations(entry.value) += SynthesizedField()),
                  Annotations(entry))
        }

        if (uri.domain.nonEmpty) {
          api.set(WebApiModel.Host,
                  AmfScalar(uri.domain, Annotations(entry.value) += SynthesizedField()),
                  Annotations(entry))
        }

        if (uri.path.nonEmpty) {
          api.set(WebApiModel.BasePath,
                  AmfScalar(uri.path, Annotations(entry.value) += SynthesizedField()),
                  Annotations(entry))
        }
      }
    )

    AnnotationParser(api, map).parse()

    api
  }
}

case class EndpointParser(entry: YMapEntry,
                          producer: String => EndPoint,
                          parent: Option[EndPoint],
                          collector: mutable.ListBuffer[EndPoint],
                          declarations: Declarations) {
  def parse(): Unit = {

    val path = parent.map(_.path).getOrElse("") + entry.key.value.toScalar.text

    val endpoint = producer(path).add(Annotations(entry))
    parent.map(p => endpoint.add(ParentEndPoint(p)))

    val map = entry.value.value.toMap

    endpoint.set(Path, AmfScalar(path, Annotations(entry.key)))

    map.key("displayName", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Name, value.string(), Annotations(entry))
    })

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Description, value.string(), Annotations(entry))
    })

    map.key(
      "uriParameters",
      entry => {
        val parameters: Seq[Parameter] =
          ParametersParser(entry.value.value.toMap, endpoint.withParameter, declarations)
            .parse()
            .map(_.withBinding("path"))
        endpoint.set(EndPointModel.UriParameters, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.regex(
      "get|patch|put|post|delete|options|head",
      entries => {
        val operations = mutable.ListBuffer[Operation]()
        entries.foreach(entry => {
          operations += OperationParser(entry, endpoint.withOperation, declarations).parse()
        })
        endpoint.set(EndPointModel.Operations, AmfArray(operations))
      }
    )

    collector += endpoint

    AnnotationParser(endpoint, map).parse()

    map.regex(
      "^/.*",
      entries => {
        entries.foreach(EndpointParser(_, producer, Some(endpoint), collector, declarations).parse())
      }
    )
  }
}

case class RequestParser(map: YMap, producer: () => Request, declarations: Declarations) {

  def parse(): Option[Request] = {
    val request = new Lazy[Request](producer)
    map.key(
      "queryParameters",
      entry => {

        val parameters: Seq[Parameter] =
          ParametersParser(entry.value.value.toMap, request.getOrCreate.withQueryParameter, declarations)
            .parse()
            .map(_.withBinding("query"))
        request.getOrCreate.set(RequestModel.QueryParameters,
                                AmfArray(parameters, Annotations(entry.value)),
                                Annotations(entry))
      }
    )

    map.key(
      "headers",
      entry => {
        val parameters: Seq[Parameter] =
          ParametersParser(entry.value.value.toMap, request.getOrCreate.withHeader, declarations)
            .parse()
            .map(_.withBinding("header"))
        request.getOrCreate.set(RequestModel.Headers,
                                AmfArray(parameters, Annotations(entry.value)),
                                Annotations(entry))
      }
    )

    map.key(
      "body",
      entry => {
        val payloads = mutable.ListBuffer[Payload]()

        RamlTypeParser(entry, shape => shape.withName("default").adopted(request.getOrCreate.id), declarations)
          .parse()
          .foreach(payloads += request.getOrCreate.withPayload(None).withSchema(_)) // todo

        entry.value.value.toMap
          .regex(
            ".*/.*",
            entries => {
              entries.foreach(entry => {
                payloads += PayloadParser(entry, producer = request.getOrCreate.withPayload, declarations).parse()
              })
            }
          )
        if (payloads.nonEmpty)
          request.getOrCreate
            .set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
      }
    )

    request.option.foreach { req =>
      AnnotationParser(req, map).parse()
    }

    request.option
  }
}

case class OperationParser(entry: YMapEntry, producer: (String) => Operation, declarations: Declarations) {

  def parse(): Operation = {

    val method = entry.key.value.toScalar.text

    val operation = producer(method).add(Annotations(entry))

    val map = entry.value.value.toMap

    operation.set(Method, ValueNode(entry.key).string())

    map.key("displayName", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Name, value.string(), Annotations(entry))
    })

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Description, value.string(), Annotations(entry))
    })

    map.key("(deprecated)", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Deprecated, value.boolean(), Annotations(entry))
    })

    map.key("(summary)", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Summary, value.string(), Annotations(entry))
    })

    map.key(
      "(externalDocs)",
      entry => {
        val creativeWork: CreativeWork = CreativeWorkParser(entry.value.value.toMap).parse()
        operation.set(OperationModel.Documentation, creativeWork, Annotations(entry))
      }
    )

    map.key(
      "protocols",
      entry => {
        val value = ArrayNode(entry.value.value.toSequence)
        operation.set(OperationModel.Schemes, value.strings(), Annotations(entry))
      }
    )

    RequestParser(map, () => operation.withRequest(), declarations)
      .parse()
      .map(operation.set(OperationModel.Request, _))

    map.key(
      "responses",
      entry => {
        entry.value.value.toMap.regex(
          "\\d{3}",
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

    AnnotationParser(operation, map).parse()

    operation
  }
}

case class ParametersParser(map: YMap, producer: String => Parameter, declarations: Declarations) {
  def parse(): Seq[Parameter] =
    map.entries
      .map(entry => ParameterParser(entry, producer, declarations).parse())
}

// todo review!
case class PayloadParser(entry: YMapEntry, producer: (Option[String]) => Payload, declarations: Declarations) {
  def parse(): Payload = {

    val payload = producer(Some(ValueNode(entry.key).string().value.toString)).add(Annotations(entry))

    entry.value.value match {
      case map: YMap =>
        // TODO
        // Should we clean the annotations here so they are not parsed again in the shape?
        AnnotationParser(payload, map).parse()
      case _ =>
    }

    entry.value.tag.tagType match {
      case YType.Null =>
      case _ =>
        RamlTypeParser(entry, shape => shape.withName("schema").adopted(payload.id), declarations)
          .parse()
          .foreach(payload.withSchema)

    }
    payload
  }
}

case class ResponseParser(entry: YMapEntry, producer: (String) => Response, declarations: Declarations) {
  def parse(): Response = {

    val node = ValueNode(entry.key)

    val response = producer(node.string().value.toString).add(Annotations(entry))
    val map      = entry.value.value.toMap

    response.set(ResponseModel.StatusCode, node.string())

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      response.set(ResponseModel.Description, value.string(), Annotations(entry))
    })

    map.key(
      "headers",
      entry => {
        val parameters: Seq[Parameter] =
          ParametersParser(entry.value.value.toMap, response.withHeader, declarations).parse()
        response.set(RequestModel.Headers, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key(
      "body",
      entry => {
        val payloads = mutable.ListBuffer[Payload]()

        val payload = Payload()
        payload.adopted(response.id) // TODO review

        RamlTypeParser(entry, shape => shape.withName("default").adopted(payload.id), declarations)
          .parse()
          .foreach(payloads += payload.withSchema(_))

        entry.value.value match {
          case map: YMap =>
            map.regex(
              ".*/.*",
              entries => {
                entries.foreach(entry => {
                  payloads += PayloadParser(entry, response.withPayload, declarations).parse()
                })
              }
            )
          case _ =>
        }
        if (payloads.nonEmpty)
          response.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
      }
    )

    AnnotationParser(response, map).parse()

    response
  }
}

case class ParameterParser(entry: YMapEntry, producer: String => Parameter, declarations: Declarations) {
  def parse(): Parameter = {

    val name      = entry.key.value.toScalar.text
    val parameter = producer(name).add(Annotations(entry)) // TODO parameter id is using a name that is not final.
    val map       = entry.value.value.toMap

    map.key("required", entry => {
      val value = ValueNode(entry.value)
      parameter.set(ParameterModel.Required, value.boolean(), Annotations(entry) += ExplicitField())
    })

    if (parameter.fields.entry(ParameterModel.Required).isEmpty) {
      val required = !name.endsWith("?")

      parameter.set(ParameterModel.Required, required)
      parameter.set(ParameterModel.Name, if (required) name else name.stripSuffix("?"))
    }

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      parameter.set(ParameterModel.Description, value.string(), Annotations(entry))
    })

    RamlTypeParser(entry, shape => shape.withName("schema").adopted(parameter.id), declarations)
      .parse()
      .foreach(parameter.set(ParameterModel.Schema, _, Annotations(entry)))

    AnnotationParser(parameter, map).parse()

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
      "type",
      entry => {
        RamlTypeParser(entry, shape => shape.adopted(custom.id), declarations)
          .parse()
          .foreach({ shape =>
            custom.set(CustomDomainPropertyModel.Schema, shape, Annotations(entry))
          })
      }
    )

    AnnotationParser(custom, map).parse()

    custom
  }
}

class RamlSpecParser(val root: Root) {

  protected def parseDeclares(map: YMap): Seq[DomainElement] =
    parseTypeDeclarations(map, root.location + "#/declarations") ++
      parseAnnotationTypeDeclarations(map, root.location + "#/declarations")

  // producer? whe lose id?
  case class ReferencesParser(map: YMap, rootReferences:Seq[ParsedReference]) {
    def parse(): mutable.Map[String, BaseUnit] = {

      val references = mutable.Map[String, BaseUnit]()

      map.key("uses", entry =>
        entry.value.value.toMap.entries.foreach(e => {
          target(e.value.value.toScalar.text).foreach(bu =>{
            val alias = e.key.value.toScalar.text
            references += alias -> bu
          })
        })
      )
      references
    }

    private def target(originalUrl: String): Option[BaseUnit] = rootReferences.find(r => r.parsedUrl.equals(originalUrl)).map(_.baseUnit)
  }

  def parseTypeDeclarations(map: YMap, typesPrefix: String): Seq[Shape] = {
    val types = ListBuffer[Shape]()

    map.key(
      "types",
      entry => {

        entry.value.value.toMap.entries.flatMap(entry => {
          val typeName = entry.key.value.toScalar.text
          RamlTypeParser(entry, shape => shape.withName(typeName).adopted(typesPrefix), Declarations(types))
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

  def parseAnnotationTypeDeclarations(map: YMap, customProperties: String): Seq[CustomDomainProperty] = {
    val customDomainProperties = ListBuffer[CustomDomainProperty]()

    map.key(
      "annotationTypes",
      e => {
        e.value.value.toMap.entries.map(entry => {
          val typeName = entry.key.value.toScalar.text
          val customProperty = AnnotationTypesParser(entry,
                                                     customProperty =>
                                                       customProperty
                                                         .withName(typeName)
                                                         .adopted(customProperties),
                                                     Declarations(customDomainProperties)).parse()
          customDomainProperties += customProperty.add(DeclaredElement())
        })
      }
    )

    customDomainProperties
  }

  case class UsageParser(map: YMap, baseUnit: BaseUnit) {
    def parse(): Unit = {
      map.key("usage", entry => {
        val value = ValueNode(entry.value)
        baseUnit.set(BaseUnitModel.Usage, value.string(), Annotations(entry))
      })
    }
  }
}
