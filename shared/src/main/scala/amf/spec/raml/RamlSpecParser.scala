package amf.spec.raml

import amf.common.AMFToken._
import amf.common.core.Strings
import amf.common.{AMFAST, Lazy}
import amf.compiler.Root
import amf.document.Document
import amf.domain.Annotation._
import amf.domain._
import amf.domain.extensions.CustomDomainProperty
import amf.domain.extensions.{ArrayNode => DataArrayNode, ObjectNode => DataObjectNode, ScalarNode => DataScalarNode, _}
import amf.metadata.domain.EndPointModel.Path
import amf.metadata.domain.OperationModel.Method
import amf.metadata.domain._
import amf.metadata.domain.extensions.CustomDomainPropertyModel
import amf.model.{AmfArray, AmfElement, AmfScalar}
import amf.shape.Shape
import amf.spec.{BaseUriSplitter, Declarations}
import amf.vocabulary.{Namespace, VocabularyMappings}

import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

/**
  * Raml 1.0 spec parser
  */
case class RamlSpecParser(root: Root) {

  def parseDocument(): Document = {

    val entries = Entries(root.ast.last)

    val declarations = parseDeclares(entries)

    val api = parseWebApi(entries, Declarations(declarations)).add(SourceVendor(root.vendor))

    Document()
      .adopted(root.location)
      .withEncodes(api)
      .withDeclares(declarations)
  }

  private def parseDeclares(entries: Entries) =
    parseTypeDeclarations(entries, root.location + "#/declarations") ++
      parseAnnotationTypeDeclarations(entries, root.location + "#/declarations")

  def parseTypeDeclarations(entries: Entries, typesPrefix: String): Seq[Shape] = {
    val types = ListBuffer[Shape]()

    entries.key(
      "types",
      entry => {
        Entries(entry.value).entries.values.flatMap(entry => {
          val typeName = entry.key.content.unquote
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

  def parseAnnotationTypeDeclarations(entries: Entries, customProperties: String): Seq[CustomDomainProperty] = {
    val customDomainProperties = ListBuffer[CustomDomainProperty]()

    entries.key(
      "annotationTypes",
      e => {
        Entries(e.value).entries.values.map(entry => {
          val typeName = entry.key.content.unquote
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

  private def parseWebApi(entries: Entries, declarations: Declarations): WebApi = {

    val api = WebApi(root.ast).adopted(root.location)

    entries.key("title", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Name, value.string(), entry.annotations())
    })

    entries.key(
      "baseUriParameters",
      entry => {
        val parameters: Seq[Parameter] =
          ParametersParser(entry.value, api.withBaseUriParameter, declarations).parse().map(_.withBinding("path"))
        api.set(WebApiModel.BaseUriParameters, AmfArray(parameters, Annotations(entry.value)), entry.annotations())
      }
    )

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Description, value.string(), entry.annotations())
    })

    entries.key(
      "mediaType",
      entry => {
        val annotations = entry.annotations()
        val value: AmfElement = entry.value.`type` match {
          case StringToken =>
            annotations += SingleValueArray()
            AmfArray(Seq(ValueNode(entry.value).string()))
          case _ =>
            ArrayNode(entry.value).strings()
        }

        api.set(WebApiModel.ContentType, value, annotations)
        api.set(WebApiModel.Accepts, value, annotations)
      }
    )

    entries.key("version", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Version, value.string(), entry.annotations())
    })

    entries.key("(termsOfService)", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.TermsOfService, value.string(), entry.annotations())
    })

    entries.key("protocols", entry => {
      val value = ArrayNode(entry.value)
      api.set(WebApiModel.Schemes, value.strings(), entry.annotations())
    })

    entries.key(
      "(contact)",
      entry => {
        val organization: Organization = OrganizationParser(entry.value).parse()
        api.set(WebApiModel.Provider, organization, entry.annotations())
      }
    )

    entries.key(
      "(externalDocs)",
      entry => {
        val creativeWork: CreativeWork = CreativeWorkParser(entry.value).parse()
        api.set(WebApiModel.Documentation, creativeWork, entry.annotations())
      }
    )

    entries.key("(license)", entry => {
      val license: License = LicenseParser(entry.value).parse()
      api.set(WebApiModel.License, license, entry.annotations())
    })

    entries.regex(
      "^/.*",
      entries => {
        val endpoints = mutable.ListBuffer[EndPoint]()
        entries.foreach(entry => EndpointParser(entry, api.withEndPoint, None, endpoints, declarations).parse())
        api.set(WebApiModel.EndPoints, AmfArray(endpoints))
      }
    )

    entries.key(
      "baseUri",
      entry => {
        val value = ValueNode(entry.value)
        val uri   = BaseUriSplitter(value.string().value.toString)

        if (api.schemes.isEmpty && uri.protocol.nonEmpty) {
          api.set(WebApiModel.Schemes,
                  AmfArray(Seq(AmfScalar(uri.protocol)), Annotations(entry.value) += SynthesizedField()),
                  entry.annotations())
        }

        if (uri.domain.nonEmpty) {
          api.set(WebApiModel.Host,
                  AmfScalar(uri.domain, Annotations(entry.value) += SynthesizedField()),
                  entry.annotations())
        }

        if (uri.path.nonEmpty) {
          api.set(WebApiModel.BasePath,
                  AmfScalar(uri.path, Annotations(entry.value) += SynthesizedField()),
                  entry.annotations())
        }
      }
    )

    entries.key(
      "types",
      entry => {
        val types = RamlTypeParser(entry, shape => shape.adopted(api.id), declarations).parse()
        println(types)
      }
    )

    AnnotationParser(api, entries).parse()

    api
  }
}

case class EndpointParser(entry: EntryNode,
                          producer: String => EndPoint,
                          parent: Option[EndPoint],
                          collector: mutable.ListBuffer[EndPoint],
                          declarations: Declarations) {
  def parse(): Unit = {

    val path = parent.map(_.path).getOrElse("") + entry.key.content.unquote

    val endpoint = producer(path).add(Annotations(entry.ast))
    parent.map(p => endpoint.add(ParentEndPoint(p)))
    val entries = Entries(entry.value)

    endpoint.set(Path, AmfScalar(path, Annotations(entry.key)))

    entries.key("displayName", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Name, value.string(), entry.annotations())
    })

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Description, value.string(), entry.annotations())
    })

    entries.key(
      "uriParameters",
      entry => {
        val parameters: Seq[Parameter] =
          ParametersParser(entry.value, endpoint.withParameter, declarations).parse().map(_.withBinding("path"))
        endpoint.set(EndPointModel.UriParameters, AmfArray(parameters, Annotations(entry.value)), entry.annotations())
      }
    )

    entries.regex(
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

    AnnotationParser(endpoint, entries).parse()

    entries.regex(
      "^/.*",
      entries => {
        entries.foreach(EndpointParser(_, producer, Some(endpoint), collector, declarations).parse())
      }
    )
  }
}

case class RequestParser(entries: Entries, producer: () => Request, declarations: Declarations) {

  def parse(): Option[Request] = {
    val request = new Lazy[Request](producer)
    entries.key(
      "queryParameters",
      entry => {

        val parameters: Seq[Parameter] =
          ParametersParser(entry.value, request.getOrCreate.withQueryParameter, declarations)
            .parse()
            .map(_.withBinding("query"))
        request.getOrCreate.set(RequestModel.QueryParameters,
                                AmfArray(parameters, Annotations(entry.value)),
                                entry.annotations())
      }
    )

    entries.key(
      "headers",
      entry => {
        val parameters: Seq[Parameter] =
          ParametersParser(entry.value, request.getOrCreate.withHeader, declarations)
            .parse()
            .map(_.withBinding("header"))
        request.getOrCreate.set(RequestModel.Headers,
                                AmfArray(parameters, Annotations(entry.value)),
                                entry.annotations())
      }
    )

    entries.key(
      "body",
      entry => {
        val payloads = mutable.ListBuffer[Payload]()

        RamlTypeParser(entry, shape => shape.withName("default").adopted(request.getOrCreate.id), declarations)
          .parse()
          .foreach(payloads += request.getOrCreate.withPayload(None).withSchema(_)) // todo

        Entries(entry.value)
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
            .set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), entry.annotations())
      }
    )

    request.option.foreach { req => AnnotationParser(req, entries).parse() }

    request.option
  }
}

case class OperationParser(entry: EntryNode, producer: (String) => Operation, declarations: Declarations) {

  def parse(): Operation = {

    val method = entry.key.content.unquote

    val operation = producer(method).add(Annotations(entry.ast))

    val entries = Entries(entry.value)

    operation.set(Method, ValueNode(entry.key).string())

    entries.key("displayName", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Name, value.string(), entry.annotations())
    })

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Description, value.string(), entry.annotations())
    })

    entries.key("(deprecated)", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Deprecated, value.boolean(), entry.annotations())
    })

    entries.key("(summary)", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Summary, value.string(), entry.annotations())
    })

    entries.key(
      "(externalDocs)",
      entry => {
        val creativeWork: CreativeWork = CreativeWorkParser(entry.value).parse()
        operation.set(OperationModel.Documentation, creativeWork, entry.annotations())
      }
    )

    entries.key(
      "protocols",
      entry => {
        val value = ArrayNode(entry.value)
        operation.set(OperationModel.Schemes, value.strings(), entry.annotations())
      }
    )

    RequestParser(entries, () => operation.withRequest(), declarations)
      .parse()
      .map(operation.set(OperationModel.Request, _))

    entries.key(
      "responses",
      entry => {
        Entries(entry.value).regex(
          "\\d{3}",
          entries => {
            val responses = mutable.ListBuffer[Response]()
            entries.foreach(entry => {
              responses += ResponseParser(entry, operation.withResponse, declarations).parse()
            })
            operation.set(OperationModel.Responses, AmfArray(responses, Annotations(entry.value)), entry.annotations())
          }
        )
      }
    )

    AnnotationParser(operation, entries).parse()

    operation
  }
}

case class ParametersParser(ast: AMFAST, producer: String => Parameter, declarations: Declarations) {
  def parse(): Seq[Parameter] = {
    Entries(ast).entries.values
      .map(entry => ParameterParser(entry, producer, declarations).parse())
      .toSeq
  }
}

case class PayloadParser(entry: EntryNode, producer: (Option[String]) => Payload, declarations: Declarations) {
  def parse(): Payload = {

    val payload = producer(Some(ValueNode(entry.key).string().value.toString)).add(Annotations(entry.ast))

    if (entry.value != null && entry.value.`type` == MapToken) {
      // TODO
      // Should we clean the annotations here so they are not parsed again in the shape?
      AnnotationParser(payload, Entries(entry.value)).parse()
    }

    Option(entry.value).foreach(
      _ =>
        RamlTypeParser(entry, shape => shape.withName("schema").adopted(payload.id), declarations)
          .parse()
          .foreach(payload.withSchema))



    payload
  }
}

case class ResponseParser(entry: EntryNode, producer: (String) => Response, declarations: Declarations) {
  def parse(): Response = {

    val node = ValueNode(entry.key)

    val response = producer(node.string().value.toString).add(Annotations(entry.ast))
    val entries  = Entries(entry.value)

    response.set(ResponseModel.StatusCode, node.string())

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      response.set(ResponseModel.Description, value.string(), entry.annotations())
    })

    entries.key(
      "headers",
      entry => {
        val parameters: Seq[Parameter] = ParametersParser(entry.value, response.withHeader, declarations).parse()
        response.set(RequestModel.Headers, AmfArray(parameters, Annotations(entry.value)), entry.annotations())
      }
    )

    entries.key(
      "body",
      entry => {
        val payloads = mutable.ListBuffer[Payload]()

        val payload = Payload()
        payload.adopted(response.id) // TODO review

        RamlTypeParser(entry, shape => shape.withName("default").adopted(payload.id), declarations)
          .parse()
          .foreach(payloads += payload.withSchema(_))

        Entries(entry.value).regex(
          ".*/.*",
          entries => {
            entries.foreach(entry => { payloads += PayloadParser(entry, response.withPayload, declarations).parse() })
          }
        )
        if (payloads.nonEmpty)
          response.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), entry.annotations())
      }
    )

    AnnotationParser(response, Entries(entry.value)).parse()

    response
  }
}

case class ParameterParser(entry: EntryNode, producer: String => Parameter, declarations: Declarations) {
  def parse(): Parameter = {

    val name      = entry.key.content.unquote
    val parameter = producer(name).add(Annotations(entry.ast)) // TODO parameter id is using a name that is not final.
    val entries   = Entries(entry.value)

    entries.key("required", entry => {
      val value = ValueNode(entry.value)
      parameter.set(ParameterModel.Required, value.boolean(), entry.annotations() += ExplicitField())
    })

    if (parameter.fields.entry(ParameterModel.Required).isEmpty) {
      val required = !name.endsWith("?")

      parameter.set(ParameterModel.Required, required)
      parameter.set(ParameterModel.Name, if (required) name else name.stripSuffix("?"))
    }

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      parameter.set(ParameterModel.Description, value.string(), entry.annotations())
    })

    RamlTypeParser(entry, shape => shape.withName("schema").adopted(parameter.id), declarations)
      .parse()
      .foreach(parameter.set(ParameterModel.Schema, _, entry.annotations()))

    AnnotationParser(parameter, entries).parse()

    parameter
  }
}

case class LicenseParser(ast: AMFAST) {
  def parse(): License = {
    val license = License(ast)
    val entries = Entries(ast)

    entries.key("url", entry => {
      val value = ValueNode(entry.value)
      license.set(LicenseModel.Url, value.string(), entry.annotations())
    })

    entries.key("name", entry => {
      val value = ValueNode(entry.value)
      license.set(LicenseModel.Name, value.string(), entry.annotations())
    })

    AnnotationParser(license, entries).parse()

    license
  }
}

case class CreativeWorkParser(ast: AMFAST) {
  def parse(): CreativeWork = {
    val creativeWork = CreativeWork(ast)
    val entries      = Entries(ast)

    entries.key("url", entry => {
      val value = ValueNode(entry.value)
      creativeWork.set(CreativeWorkModel.Url, value.string(), entry.annotations())
    })

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      creativeWork.set(CreativeWorkModel.Description, value.string(), entry.annotations())
    })

    AnnotationParser(creativeWork, entries).parse()

    creativeWork
  }
}

case class OrganizationParser(ast: AMFAST) {
  def parse(): Organization = {

    val organization = Organization(ast)
    val entries      = Entries(ast)

    entries.key("url", entry => {
      val value = ValueNode(entry.value)
      organization.set(OrganizationModel.Url, value.string(), entry.annotations())
    })

    entries.key("name", entry => {
      val value = ValueNode(entry.value)
      organization.set(OrganizationModel.Name, value.string(), entry.annotations())
    })

    entries.key("email", entry => {
      val value = ValueNode(entry.value)
      organization.set(OrganizationModel.Email, value.string(), entry.annotations())
    })

    AnnotationParser(organization, entries).parse()

    organization
  }
}

case class AnnotationTypesParser(node: EntryNode, adopt: (CustomDomainProperty) => Unit, declarations: Declarations) {
  def parse(): CustomDomainProperty = {
    val custom         = CustomDomainProperty(node.annotations())
    val annotationName = node.key.content.unquote
    custom.withName(annotationName)
    adopt(custom)

    val entries = Entries(node.value)

    entries.key(
      "allowedTargets",
      entry => {
        val annotations = entry.annotations()
        val targets: AmfArray = entry.value.`type` match {
          case StringToken =>
            annotations += SingleValueArray()
            AmfArray(Seq(ValueNode(entry.value).string()))
          case _ =>
            ArrayNode(entry.value).strings()
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

    entries.key("displayName", entry => {
      val value = ValueNode(entry.value)
      custom.set(CustomDomainPropertyModel.DisplayName, value.string(), entry.annotations())
    })

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      custom.set(CustomDomainPropertyModel.Description, value.string(), entry.annotations())
    })

    entries.key(
      "type",
      entry => {
        RamlTypeParser(entry, shape => shape.adopted(custom.id), declarations)
          .parse()
          .foreach({ shape =>
            custom.set(CustomDomainPropertyModel.Schema, shape, entry.annotations())
          })
      }
    )

    AnnotationParser(custom, entries).parse()

    custom
  }
}

case class Entries(ast: AMFAST) {

  def key(keyword: String): Option[EntryNode] = entries.get(keyword)

  def key(keyword: String, fn: (EntryNode => Unit)): Unit = key(keyword).foreach(fn)

  def regex(regex: String, fn: (Iterable[EntryNode] => Unit)): Unit = {
    val path: Regex = regex.r
    val values = entries
      .filterKeys({
        case path() => true
        case _      => false
      })
      .values
    if (values.nonEmpty) fn(values)
  }

  var entries: ListMap[String, EntryNode] = ListMap(ast.children.map(n => n.head.content.unquote -> EntryNode(n)): _*)

}

case class EntryNode(ast: AMFAST) {

  val key: AMFAST   = ast.head
  val value: AMFAST = Option(ast).filter(_.children.size > 1).map(_.last).orNull

  def annotations(): Annotations = Annotations(ast)
}

case class ArrayNode(ast: AMFAST) {

  def strings(): AmfArray = {
    val elements = ast.children.map(child => ValueNode(child).string())
    AmfArray(elements, annotations())
  }

  private def annotations() = Annotations(ast)
}

case class ValueNode(ast: AMFAST) {

  def string(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(content, annotations())
  }

  def integer(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(content.toInt, annotations())
  }

  def boolean(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(content.toBoolean, annotations())
  }

  def negated(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(!content.toBoolean, annotations())
  }

  private def annotations() = Annotations(ast)
}

case class AnnotationParser(element: DomainElement, entries: Entries) {
  def parse(): Unit = {
    val domainExtensions:ListBuffer[DomainExtension] = ListBuffer()
    entries.entries.foreach {
      case (key, entry) => {
        if (WellKnownAnnotation.normalAnnotation(key, element)) {
          domainExtensions += ExtensionParser(key, element.id, entry).parse()
        }
      }
    }
    if (domainExtensions.nonEmpty)
      element.withCustomDomainProperties(domainExtensions)
  }
}

case class ExtensionParser(annotationName: String, parent: String, entry: EntryNode) {
  def parse(): DomainExtension = {
    val domainExtension = DomainExtension()
    val dataNode = DataNodeParser(entry.value, Some(parent)).parse()
    // TODO
    // this is temporary, we should look for the annotation in the annotationTypes declared in the schema
    val customDomainProperty = CustomDomainProperty(entry.annotations()).withName(WellKnownAnnotation.parseRamlName(annotationName))
    domainExtension.adopted(parent)
    domainExtension
      .withExtension(dataNode)
      .withDefinedBy(customDomainProperty)
  }
}

case class DataNodeParser(value: AMFAST, parent: Option[String] = None) {
  def parse(): DataNode = {
    value.`type` match {
      case StringToken   => parseScalar(value, "string")
      case IntToken      => parseScalar(value, "integer")
      case FloatToken    => parseScalar(value, "float")
      case BooleanToken  => parseScalar(value, "boolean")
      case Null          => parseScalar(value, "nil")
      case SequenceToken => parseArray(value)
      case MapToken      => parseObject(value)
      case other         => throw new Exception(s"Cannot parse data node from AST structure $other")
    }
  }

  protected def parseScalar(ast: AMFAST, datatype: String): DataNode = {
    val node = DataScalarNode(ast.content.unquote, Some((Namespace.Xsd + datatype).iri()), Annotations(ast))
    if (parent.isDefined) node.adopted(parent.get)
    node
  }

  protected def parseArray(value: AMFAST): DataNode = {
    val node = DataArrayNode(Annotations(value))
    if (parent.isDefined) node.adopted(parent.get)
    value.children.foreach { ast =>
      val element = DataNodeParser(ast).parse()
      node.addMember(element)
    }
    node
  }

  protected def parseObject(value: AMFAST): DataNode = {
    val node = DataObjectNode(Annotations(value))
    if (parent.isDefined) node.adopted(parent.get)
    value.children.map { ast =>
      val property = ast.head.content.unquote
      val value = Option(ast).filter(_.children.size > 1).map(_.last).orNull
      val propertyAnnotations = Annotations(ast)

      val propertyNode = DataNodeParser(value).parse()
      node.addProperty(property, propertyNode, propertyAnnotations)
    }
    node
  }
}