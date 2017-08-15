package amf.spec

import amf.builder._
import amf.common.AMFToken
import amf.common.AMFToken.{Entry, Extension}
import amf.common.Strings.strings
import amf.domain.Annotation._
import amf.domain.{Annotation, _}
import amf.metadata.Type
import amf.metadata.domain.EndPointModel.{Operations, Path}
import amf.metadata.domain.OperationModel.Method
import amf.metadata.domain.ParameterModel.Required
import amf.metadata.domain.ResponseModel.Headers
import amf.metadata.domain.WebApiModel.EndPoints
import amf.metadata.domain._
import amf.parser.ASTNode
import amf.remote.{Oas, Raml, Vendor}
import amf.spec.ContextKey.{EndPointBodyParameter, OperationBodyParameter}
import amf.spec.Matcher.RegExpMatcher
import amf.spec.Spec._

import scala.language.existentials
import scala.collection.mutable.ListBuffer

/**
  * Spec parsers.
  */
object FieldParser {

  trait SpecFieldParser {
    def parse(spec: SpecField, node: ASTNode[_], container: Builder, context: ParserContext): Unit
  }

  trait ValueParser[T] extends SpecFieldParser {
    def value(content: String): T

    override def parse(spec: SpecField, entry: ASTNode[_], container: Builder, context: ParserContext): Unit = {
      spec.fields.foreach(container.set(_, value(valueField(entry).content.unquote), annotations(entry)))
    }

    private def valueField(entry: ASTNode[_]) = if (entry.last.`type` == Extension) entry.last.last else entry.last
  }

  object StringValueParser extends ValueParser[String] {
    override def value(content: String): String = content
  }

  object BoolValueParser extends ValueParser[Boolean] {
    override def value(content: String): Boolean = content.toBoolean
  }

  object StringListParser extends SpecFieldParser {

    override def parse(spec: SpecField, entry: ASTNode[_], container: Builder, context: ParserContext): Unit =
      spec.fields.foreach(container.set(_, values(entry.last), annotations(entry)))

    private def values(value: ASTNode[_]): Seq[String] = value.`type` match {
      case AMFToken.StringToken => Seq(value.content)
      case _                    => value.children.map(c => c.content.unquote)
    }
  }

  case class ChildrenParser() extends SpecFieldParser {
    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder, context: ParserContext): Unit =
      parseMap(spec, entry.last, builder, context)

    protected def parseMap(spec: SpecField, mapNode: ASTNode[_], container: Builder, context: ParserContext): Unit = {
      mapNode.children.foreach(entry => {
        spec.children
          .map(_.copy(vendor = spec.vendor)) //TODO copying parent vendor to children here...
          .find(_.matcher.matches(entry)) match {
          case Some(field) => field.parser.parse(field, entry, container, context)
          case _           => // Unknown node...
        }
      })
    }
  }

  object ParametersParser extends ChildrenParser {
    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder, context: ParserContext): Unit = {
      spec.vendor match {
        case Raml => parseParamsFromMap(spec, entry, builder, context)
        case Oas if spec.fields.head.equals(Headers) =>
          parseParamsFromMap(spec, entry, builder, context) //Special case where params in oas have raml structure
        case Oas => parseParamsFromSequence(spec, entry, builder, context)
      }
    }

    private def parseParamsFromSequence(spec: SpecField,
                                        entry: ASTNode[_],
                                        container: Builder,
                                        context: ParserContext) = {
      entry.last.children.foreach(paramMap => {
        val b = ParameterBuilder()

        super.parseMap(spec, paramMap, b, context)

        val param = b.build

        if (spec.fields.size == 1) {
          param.binding match {
            case "path" | "header" | "query" => container.add(spec.fields.head, List(param), annotations(paramMap))
            case "body" =>
              context.set(EndPointBodyParameter, (param, PayloadsParser.parsePayload(paramMap, context, container)))
            case _ => //Invalid binding in endpoint parameter
          }
        } else {
          param.binding match {
            case "header"         => container.add(RequestModel.Headers, List(param), annotations(paramMap))
            case "path" | "query" => container.add(RequestModel.QueryParameters, param, annotations(paramMap))
            case "body" =>
              context.set(OperationBodyParameter, (param, PayloadsParser.parsePayload(paramMap, context, container)))
            case _ => //Invalid binding in endpoint parameter
          }
        }
      })
    }

    private def parseParamsFromMap(spec: SpecField,
                                   entry: ASTNode[_],
                                   builder: Builder,
                                   context: ParserContext): Unit = {

      entry.last.children.foreach(paramEntry => {
        val name = paramEntry.head.content.unquote

        val parameter: Parameter = Parameter(paramEntry)
          .set(Required, !name.endsWith("?")).set(ParameterModel.Name, name)


        val param = ParameterBuilder().set(Required, ).set(ParameterModel.Name, name)

        super.parse(spec, paramEntry, param, context)

        val paramAnnotations =
          if (spec.vendor == Raml) annotations(paramEntry) :+ UriParameters() else annotations(paramEntry)

        builder add (spec.fields.head, param.build, paramAnnotations)
      })
    }
  }

  object ResponseParser extends ChildrenParser {
    override def parse(spec: SpecField, entry: ASTNode[_], container: Builder, context: ParserContext): Unit = {
      val statusCode = entry.head.content.unquote

      val response = ResponseBuilder()
        .set(ResponseModel.Name, statusCode, annotations(entry.head))
        .set(
          ResponseModel.StatusCode,
          if (statusCode == "default") "200" else statusCode,
          annotations(entry.head)
        )
        .resolveId(container.getId)

      super.parse(spec, entry, response, context)

      //if OAS, check for default payload and add it

      if (spec.vendor == Oas) {
        val rootPayload = PayloadBuilder()
        if (traverseAndParse(Spec.OasPayload, entry, rootPayload, context))
          response add (ResponseModel.Payloads, rootPayload.build)
      }

      container add (spec.fields.head, response.build, annotations(entry))
    }
  }

  object PayloadsParser extends ChildrenParser {
    override def parse(spec: SpecField, entry: ASTNode[_], container: Builder, context: ParserContext): Unit = {
      spec.vendor match {
        case Raml =>
          //TODO this is the root. Search for a raml-type here and add a payload to the builder without mediaType.

          entry.last.children
            .filter(RegExpMatcher(".*/.*").matches)
            .foreach(e => {
              val somePayload =
                PayloadBuilder().set(PayloadModel.MediaType, e.head.content.unquote, annotations(e.head))

              if (e.last != e.head)
                somePayload.set(PayloadModel.Schema, e.last.content.unquote, annotations(e.last))

              //TODO match a raml-type inside of node e.. . .. .... ?

              container.add(spec.fields.head, List(somePayload.build))
            })
        case Oas =>
          //This will parse only payloads from extensions (in a sequence)
          //TODO .last.last to avoid extension node.
          entry.last.children.foreach(payloadMap => {
            val payload = PayloadBuilder()
            if (traverseAndParseMap(Spec.OasExtensionPayload, payloadMap, payload, context))
              container.add(spec.fields.head, List(payload.build))
          })
        case _ =>
      }
    }

    private[spec] def parsePayload(payloadMap: ASTNode[_], context: ParserContext, container: Builder): Payload = {
      val payload = PayloadBuilder()
      traverseAndParseMap(Spec.OasPayload, payloadMap, payload, context)
      payload.resolveId(container.getId)
      payload.build
    }
  }

  object EndPointParser extends ChildrenParser {
    override def parse(spec: SpecField, node: ASTNode[_], container: Builder, context: ParserContext): Unit = {
      val result: ListBuffer[EndPoint] = ListBuffer()
      parse(spec, node, None, result, context, container)
      container.add(EndPoints, result.toList)
    }

    private def parse(spec: SpecField,
                      node: ASTNode[_],
                      parent: Option[EndPoint],
                      collector: ListBuffer[EndPoint],
                      context: ParserContext,
                      container: Builder): Unit = {

      val as = annotations(node) ++ parent
        .map(p => List(ParentEndPoint(p)))
        .getOrElse(Nil)
      val endpointContext = context.copy()

      val endpoint = EndPointBuilder(as)
        .set(Path, parent.map(_.path).getOrElse("") + node.head.content.unquote, annotations(node.head))
        .resolveId(container.getId)

      super.parse(spec, node, endpoint, endpointContext)

      val actual = endpoint.build
      collector += actual

      if (spec.vendor == Raml)
        node.last.children
          .filter(RegExpMatcher("/.*").matches)
          .foreach(parse(spec, _, Some(actual), collector, endpointContext, container))
    }
  }

  object OperationParser extends ChildrenParser {

    def addDefaultPayload(request: RequestBuilder, context: ParserContext, annotations: List[Annotation]): Boolean = {
      //TODO What if 'parameters' key is after this operation? The endpoint body parameter wouldn't be in the context.

      context(OperationBodyParameter) match {
        case Some((opParam: Parameter, opPayload: Payload)) =>
          context(EndPointBodyParameter) match {
            case Some((endPointParam: Parameter, endPointPayload: Payload)) =>
              request add (RequestModel.Payloads, Payload(
                opPayload.fields,
                opPayload.annotations ++ List(Annotation.OperationBodyParameter(opParam),
                                              OverrideEndPointBodyParameter(endPointParam, endPointPayload))))
            case None =>
              request add (RequestModel.Payloads, Payload(
                opPayload.fields,
                opPayload.annotations ++ List(Annotation.OperationBodyParameter(opParam))))
            case _ =>
          }
          return true
        case None =>
          context(EndPointBodyParameter) match {
            case Some((endPointParam: Parameter, endPointPayload: Payload)) =>
              request add (RequestModel.Payloads, Payload(
                endPointPayload.fields,
                endPointPayload.annotations ++ List(Annotation.EndPointBodyParameter(endPointParam))))
              return true
            case _ =>
          }
        case _ =>
      }
      false
    }

    def setRequest(op: OperationBuilder,
                   entry: ASTNode[_],
                   vendor: Vendor,
                   context: ParserContext,
                   container: Builder): Unit = {
      val req: RequestBuilder = RequestBuilder().resolveId(container.getId)
      var add                 = traverseAndParse(RequestSpec(vendor), entry, req, context)
      if (vendor == Oas) add |= addDefaultPayload(req, context, annotations(entry))
      if (add) op set (OperationModel.Request, req.build, annotations(entry))
    }

    override def parse(spec: SpecField, entry: ASTNode[_], container: Builder, context: ParserContext): Unit = {
      val opContext = context.copy()

      val method = entry.head.content.unquote
      val op     = OperationBuilder().set(Method, method, annotations(entry)).resolveId(container.getId)
      super.parse(spec, entry, op, context)

      setRequest(op, entry, spec.vendor, opContext, container)

      container.add(Operations, op.build)
    }
  }

  object ObjectParser extends ChildrenParser {

    def builderForType(t: Type, annotations: List[Annotation]): Builder = t match {
      case OrganizationModel => OrganizationBuilder(annotations)
      case LicenseModel      => LicenseBuilder(annotations)
      case CreativeWorkModel => CreativeWorkBuilder(annotations)
    }

    override def parse(spec: SpecField, node: ASTNode[_], container: Builder, context: ParserContext): Unit = {
      val field            = spec.fields.head
      val builder: Builder = builderForType(field.`type`, annotations(node))
      super.parse(spec, node, builder, context)
      builder.resolveId(container.getId)
      container.set(field, builder.build)
    }
  }

  def annotations(node: ASTNode[_]): Annotations = node.`type` match {
    case Entry if node.head.content.unquote == "required" => List(LexicalInformation(node.range), ExplicitField())
    case _                                                => List(LexicalInformation(node.range))
  }

  private def traverseAndParseMap(spec: Spec, map: ASTNode[_], builder: Builder, context: ParserContext): Boolean = {
    var add = false
    map.children.foreach(entry => {
      spec.fields.find(_.matcher.matches(entry)) match {
        case Some(field) =>
          field.parser.parse(field, entry, builder, context)
          add = true
        case _ => // Unknown node...
      }
    })
    add
  }

  private def traverseAndParse(spec: Spec, entry: ASTNode[_], builder: Builder, context: ParserContext): Boolean =
    traverseAndParseMap(spec, entry.last, builder, context)
}
