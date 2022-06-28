package amf.apicontract.internal.spec.common.parser

import amf.apicontract.client.scala.model.domain.{Parameter, Payload, SchemaContainer}
import amf.apicontract.internal.annotations._
import amf.apicontract.internal.metamodel.domain.{ParameterModel, PayloadModel, ResponseModel}
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorParameter
import amf.apicontract.internal.spec.common.{OasParameter, Parameters}
import amf.apicontract.internal.spec.oas.parser.context.{OasSpecVersionFactory, OasWebApiContext}
import amf.apicontract.internal.spec.oas.parser.domain.OasContentsParser
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.spec.spec.{OasDefinitions, toOas}
import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.apicontract.internal.validation.definitions.ParserSideValidations._
import amf.core.client.common.position.Range
import amf.core.client.scala.model.domain._
import amf.core.internal.annotations._
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.core.internal.parser.{YMapOps, YNodeLikeOps}
import amf.core.internal.utils.{AmfStrings, IdCounter}
import amf.core.internal.validation.CoreValidations.UnresolvedReference
import amf.core.internal.validation.core.ValidationSpecification
import amf.shapes.client.scala.model.domain.{AnyShape, Example, FileShape, NodeShape}
import amf.shapes.internal.annotations.ExternalReferenceUrl
import amf.shapes.internal.domain.resolution.ExampleTracking.tracking
import amf.shapes.internal.spec.common.parser.{AnnotationParser, OasExamplesParser, YMapEntryLike}
import amf.shapes.internal.spec.common.{OAS20SchemaVersion, OAS30SchemaVersion, SchemaPosition}
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.spec.raml.parser._
import org.yaml.model._
import scala.language.postfixOps

case class RamlParametersParser(map: YMap, adopted: Parameter => Unit, parseOptional: Boolean = false, binding: String)(
    implicit ctx: RamlWebApiContext
) {

  def parse(): Seq[Parameter] =
    map.entries
      .map(entry => ctx.factory.parameterParser(entry, adopted, parseOptional, binding).parse())
}

object RamlHeaderParser {
  def parse(adopted: Parameter => Unit, parseOptional: Boolean = false)(
      node: YNode
  )(implicit ctx: RamlWebApiContext): Parameter = {
    RamlParameterParser.parse(adopted, parseOptional, "header")(node)
  }
}

object RamlQueryParameterParser {
  def parse(adopted: Parameter => Unit, parseOptional: Boolean = false)(
      node: YNode
  )(implicit ctx: RamlWebApiContext): Parameter = {
    RamlParameterParser.parse(adopted, parseOptional, "query")(node)
  }
}

object RamlParameterParser {
  def parse(adopted: Parameter => Unit, parseOptional: Boolean = false, binding: String)(
      node: YNode
  )(implicit ctx: RamlWebApiContext): Parameter = {
    val head = node.as[YMap].entries.head
    ctx.factory.parameterParser(head, adopted, parseOptional, binding).parse()
  }
}

case class Raml10ParameterParser(
    entry: YMapEntry,
    adopted: Parameter => Unit,
    parseOptional: Boolean = false,
    binding: String
)(implicit ctx: RamlWebApiContext)
    extends RamlParameterParser(entry, adopted) {

  override def parse(): Parameter = {
    val nameNode = ScalarNode(entry.key)
    val parameter = Parameter(entry)
      .withName(nameNode)
      .setWithoutId(
        ParameterModel.ParameterName,
        nameNode.text(),
        Annotations.inferred()
      ) // TODO parameter id is using a name that is not final.
    parameter.syntheticBinding(binding)
    adopted(parameter)
    val p = entry.value.to[YMap] match {
      case Right(map) =>
        map.key("required", (ParameterModel.Required in parameter).explicit.allowingAnnotations)
        map.key("description", (ParameterModel.Description in parameter).allowingAnnotations)
        map.key("binding".asRamlAnnotation, (ParameterModel.Binding in parameter).explicit)
        Raml10TypeParser(entry, shape => shape.withName("schema"), TypeInfo(isPropertyOrParameter = true))(
          WebApiShapeParserContextAdapter(ctx)
        )
          .parse()
          .foreach(s => parameter.setWithoutId(ParameterModel.Schema, tracking(s, parameter), Annotations(entry)))

        AnnotationParser(parameter, map)(WebApiShapeParserContextAdapter(ctx)).parse()

        parameter
      case _ =>
        val scope = ctx.link(entry.value) match {
          case Left(_) => SearchScope.Fragments
          case _       => SearchScope.Named
        }
        entry.value.tagType match {
          case YType.Null =>
            Raml10TypeParser(
              entry,
              shape => shape.withName("schema")
            )(WebApiShapeParserContextAdapter(ctx)).parse().foreach { schema =>
              tracking(schema, parameter).annotations += SynthesizedField()
              parameter.setWithoutId(ParameterModel.Schema, schema, Annotations(entry))
            }
            parameter
          case _ => // we have a property type
            entry.value.to[YScalar] match {
              case Right(ref) if referencesDeclaredParameter(scope, ref) =>
                ctx.declarations
                  .findParameter(ref.text, scope)
                  .get
                  .link(ScalarNode(entry.value), Annotations(entry))
                  .asInstanceOf[Parameter]
                  .setWithoutId(ParameterModel.Name, nameNode.text())
              case Right(_) =>
                Raml10TypeParser(
                  entry,
                  shape => shape.withName("schema", Annotations(SynthesizedField())),
                  TypeInfo(isPropertyOrParameter = true),
                  StringDefaultType
                )(WebApiShapeParserContextAdapter(ctx))
                  .parse() match {
                  case Some(schema) =>
                    parameter.setWithoutId(ParameterModel.Schema, tracking(schema, parameter), Annotations(entry))
                  case None =>
                    ctx.eh.violation(
                      UnresolvedReference,
                      parameter,
                      "Cannot declare unresolved parameter",
                      entry.value.location
                    )
                    parameter
                }
              case _ =>
                ctx.eh.violation(
                  UnresolvedReference,
                  parameter,
                  "Cannot declare unresolved parameter",
                  entry.value.location
                )
                parameter

            }
        }
    }

    if (p.fields.entry(ParameterModel.Required).isEmpty) {
      val stringName    = nameNode.text().toString
      val optionalParam = stringName.endsWith("?")

      // should this be always synthetized? if so, change validation for RAML Overlay overriding required (to ignore Synthetized)
      if (optionalParam)
        p.setWithoutId(ParameterModel.Required, AmfScalar(!optionalParam), Annotations.synthesized())
      else
        p.setWithoutId(
          ParameterModel.Required,
          AmfScalar(!optionalParam, Annotations.synthesized()),
          Annotations.inferred()
        )

      val paramName = if (optionalParam) stringName.stripSuffix("?") else stringName
      p.setWithoutId(ParameterModel.Name, AmfScalar(paramName, nameNode.text().annotations), Annotations.inferred())
        .setWithoutId(
          ParameterModel.ParameterName,
          AmfScalar(paramName, nameNode.text().annotations),
          Annotations.inferred()
        )
    }
    p.syntheticBinding(binding)
    p
  }

  private def referencesDeclaredParameter(scope: SearchScope.Scope, ref: YScalar) = {
    ctx.declarations.findParameter(ref.text, scope).isDefined
  }
}

case class Raml08ParameterParser(
    entry: YMapEntry,
    adopted: Parameter => Unit,
    parseOptional: Boolean = false,
    binding: String
)(implicit ctx: RamlWebApiContext)
    extends RamlParameterParser(entry, adopted) {
  def parse(): Parameter = {
    val nameNode = ScalarNode(entry.key)
    val parameter = Parameter(entry)
      .withName(nameNode)
      .setWithoutId(
        ParameterModel.ParameterName,
        nameNode.text(),
        Annotations.inferred()
      ) // TODO parameter id is using a name that is not final.
    parameter.syntheticBinding(binding)
    adopted(parameter)

    entry.value.tagType match {
      case YType.Null =>
        Raml10TypeParser(
          entry,
          shape => shape.withName("schema")
        )(WebApiShapeParserContextAdapter(ctx)).parse().foreach { schema =>
          tracking(schema, parameter).annotations += SynthesizedField()
          parameter.setWithoutId(ParameterModel.Schema, schema, Annotations(entry))
        }
      case _ =>
        // Named Parameter Parse
        Raml08TypeParser(
          entry,
          (s: Shape) => s.withName(nameNode.text().toString),
          isAnnotation = false,
          StringDefaultType
        )(WebApiShapeParserContextAdapter(ctx))
          .parse()
          .foreach(s => parameter.setWithoutId(ParameterModel.Schema, tracking(s, parameter), Annotations(entry)))
    }

    entry.value.toOption[YMap] match {
      case Some(map) =>
        map.key("required", (ParameterModel.Required in parameter).explicit)
        map.key("description", (ParameterModel.Description in parameter).allowingAnnotations)
      case _ =>
    }

    if (parameter.fields.entry(ParameterModel.Required).isEmpty)
      parameter.set(ParameterModel.Required, AmfScalar(false), Annotations.synthesized())

    val stringName = nameNode.text().toString
    if (parseOptional && stringName.endsWith("?")) {
      parameter.set(ParameterModel.Optional, value = true)
      val n = stringName.stripSuffix("?")
      parameter
        .set(ParameterModel.Name, AmfScalar(n, nameNode.text().annotations))
        .set(ParameterModel.ParameterName, n)
    }

    parameter
  }
}

abstract class RamlParameterParser(entry: YMapEntry, adopted: Parameter => Unit)(implicit val ctx: RamlWebApiContext)
    extends RamlTypeSyntax
    with SpecParserOps {

  def parse(): Parameter
}

trait OasParameterParser extends SpecParserOps {
  def parse: OasParameter
}

case class Oas2ParameterParser(
    entryOrNode: YMapEntryLike,
    parentId: String,
    nameNode: Option[YNode],
    nameGenerator: IdCounter
)(implicit ctx: OasWebApiContext)
    extends OasParameterParser {

  protected val map: YMap = entryOrNode.asMap

  protected def setName(p: DomainElement with NamedDomainElement): DomainElement = {
    p match {
      case _: Shape if nameNode.isDefined =>
        p.setWithoutId(ShapeModel.Name, nameNode.map(ScalarNode(_).text()).get, Annotations.inferred())
      case _: Shape =>
        map.key(
          "name",
          ShapeModel.Name in p
        ) // name of the parameter in the HTTP binding (path, request parameter, etc)
      case p: Payload =>
        if (nameNode.nonEmpty)
          p.setWithoutId(
            ParameterModel.Name,
            nameNode.map(ScalarNode(_).text()).get,
            map
              .key("name")
              .map(e => {
                Annotations(e) += ParameterNameForPayload(ScalarNode(e.value).text().value.toString, Range(e.range))
              })
              .getOrElse(Annotations.inferred())
          )
        else map.key("name", ParameterModel.Name in p)
        validateEntryName(p)
      case _ =>
        if (nameNode.nonEmpty)
          p.withName(nameNode.map(ScalarNode(_)).get)
        else
          map.key(
            "name",
            ParameterModel.Name in p
          ) // name of the parameter in the HTTP binding (path, request parameter, etc)
        validateEntryName(p)
    }
    p
  }

  private def buildFromBinding(in: String, bindingEntry: Option[YMapEntry]): OasParameter = {
    in match {
      case "body" =>
        OasParameter(parseBodyPayload(bindingEntry.map(a => Range(a.range))), Some(entryOrNode.ast))
      case "formData" =>
        OasParameter(parseFormDataPayload(bindingEntry.map(a => Range(a.range))), Some(entryOrNode.ast))
      case "query" | "header" | "path" =>
        OasParameter(parseCommonParam(in), Some(entryOrNode.ast))
      case _ =>
        val oasParam = buildFromBinding(defaultBinding, None)
        invalidBinding(bindingEntry, in, oasParam)
        oasParam
    }
  }

  private def validateEntryName(element: DomainElement with NamedDomainElement): Unit = {
    // if is invalid need a aut generated name different from default to avoid collision with uri parameters
    if (element.name.option().isEmpty)
      element.setWithoutId(
        ParameterModel.Name,
        AmfScalar(nameGenerator.genId("parameter"), Annotations(AutoGeneratedName())),
        Annotations.synthesized()
      )
    if (map.key("name").isEmpty) {
      ctx.eh.violation(
        ParameterNameRequired,
        element,
        "'name' property is required in a parameter.",
        map.location
      )
    }
  }

  def parse(): OasParameter = {
    val parameter = map.key("$ref") match {
      case Some(ref) => parseParameterRef(ref, parentId)
      case None =>
        map.key("in") match {
          case Some(entry: YMapEntry) =>
            val in = entry.value.as[YScalar].text
            buildFromBinding(in, Some(entry))
          case _ => // ignore
            /** Binding is required, i'm not setting any default value so It will be some model validation.
              */
            val parameter = Parameter(entryOrNode.annotations)
            setName(parameter)
            OasParameter(parameter, Some(map))
        }
    }
    checkExampleField(parameter)
    parameter
  }

  def checkExampleField(p: OasParameter): Unit =
    map.key("example") match {
      case Some(_) =>
        /* TODO: Should remove 'example' from syntax and delete this method and parser-side validation
                 as it will become a model validation and violation in the next major */
        ctx.eh.warning(
          invalidExampleFieldWarning,
          p.domainElement,
          s"Property 'example' not supported in a parameter node",
          map.location
        )
      case _ =>
    }

  protected def parseCommonParam(binding: String): Parameter = {
    val parameter = parseFixedFields()
    parseType(
      parameter,
      binding,
      () => {
        OasTypeParser(
          entryOrNode,
          "schema",
          map,
          shape => shape.withName("schema"),
          OAS20SchemaVersion(SchemaPosition.Parameter)
        )(WebApiShapeParserContextAdapter(ctx).toOasNext)
          .parse()
          .map { schema =>
            parameter.setWithoutId(ParameterModel.Schema, schema, Annotations(map))
          }
          .orElse {
            ctx.eh.violation(
              UnresolvedParameter,
              parameter,
              "Cannot find valid schema for parameter",
              map.location
            )
            None
          }
      }
    )
    parameter
  }

  protected def parseFixedFields(): Parameter = {
    val parameter = Parameter(entryOrNode.ast)
    setName(parameter)
    parameter.setWithoutId(ParameterModel.Required, AmfScalar(false), Annotations.synthesized())

    map.key(
      "name",
      ParameterModel.ParameterName in parameter
    ) // name of the parameter in the HTTP binding (path, request parameter, etc)

    map.key("in", ParameterModel.Binding in parameter)
    map.key("description", ParameterModel.Description in parameter)
    map.key("required", (ParameterModel.Required in parameter).explicit)

    ctx.closedShape(parameter, map, "parameter")
    AnnotationParser(parameter, map)(WebApiShapeParserContextAdapter(ctx)).parse()
    parameter
  }

  def parseType(
      container: SchemaContainer with AmfObject,
      binding: String,
      typeParsing: () => Unit
  ): SchemaContainer = {

    def setDefaultSchema = (c: SchemaContainer) => c.setSchema(AnyShape(Annotations(Inferred())))

    map.key("type") match {
      case None =>
        setDefaultSchema(container)
        ctx.eh.violation(
          MissingParameterType,
          container,
          s"'type' is required in a parameter with binding '$binding'",
          map.location
        )
      case Some(entry) =>
        checkItemsField(entry, container)
        typeParsing()
    }
    container
  }

  private def checkItemsField(entry: YMapEntry, container: SchemaContainer with AmfObject): Unit = {
    val typeValue = entry.value.asScalar.get.text
    val items     = map.key("items")
    if (typeValue == "array" && items.isEmpty)
      ctx.eh.warning(
        ItemsFieldRequiredWarning, // TODO: Should be violation
        container,
        "'items' field is required when schema type is array",
        map.location
      )
  }

  private def parseFormDataPayload(bindingRange: Option[Range]): Payload = {
    val payload = commonPayload(bindingRange)
    ctx.closedShape(payload, map, "parameter")
    parseType(
      payload,
      "formData",
      () => {
        new OasTypeParser(
          entryOrNode,
          "schema",
          map,
          shape => setName(shape).asInstanceOf[Shape],
          OAS20SchemaVersion(SchemaPosition.Parameter)
        )((WebApiShapeParserContextAdapter(ctx)).toOasNext)
          .parse()
          .map { schema =>
            payload.setWithoutId(PayloadModel.Schema, tracking(schema, payload), Annotations(map))
          }
          .orElse {
            ctx.eh.violation(
              UnresolvedParameter,
              payload,
              "Cannot find valid schema for parameter",
              map.location
            )
            None
          }
      }
    )
    payload.annotations += FormBodyParameter()
    payload
  }

  private def commonPayload(bindingRange: Option[Range]): Payload = {
    val payload = Payload(entryOrNode.ast)
    setName(payload)
    if (payload.name.option().isEmpty)
      payload.setWithoutId(ParameterModel.Name, AmfScalar("default"), Annotations() += Inferred())
    map.key(
      "required",
      entry => {
        val req: Boolean = entry.value.as[Boolean]
        payload.annotations += RequiredParamPayload(req, Range(entry.range))
      }
    )
    map.key("description", PayloadModel.Description in payload)
    AnnotationParser(payload, map)(WebApiShapeParserContextAdapter(ctx)).parse()
    payload
  }

  private def parseBodyPayload(bindingRange: Option[Range]): Payload = {
    val payload: Payload = commonPayload(bindingRange)
    ctx.closedShape(payload, map, "bodyParameter")

    map.key("mediaType".asOasExtension, PayloadModel.MediaType in payload)
    // Force to re-adopt with the new mediatype if exists
    if (payload.mediaType.nonEmpty) validateEntryName(payload)

    map.key("schema") match {
      case Some(entry) =>
        // i don't need to set param need in here. Its necessary only for form data, because of the properties
        OasTypeParser(entry, shape => setName(shape).asInstanceOf[Shape])(
          WebApiShapeParserContextAdapter(ctx).toOasNext
        )
          .parse()
          .map { schema =>
            checkNotFileInBody(schema)
            payload.setWithoutId(PayloadModel.Schema, tracking(schema, payload), Annotations(entry))
            bindingRange.foreach { range =>
              schema.annotations += ParameterBindingInBodyLexicalInfo(range)
            }
            schema
          }
      case None =>
        ctx.eh.warning(OasInvalidParameterSchema, "", s"Schema is required for a parameter in body", map.location)
    }
    payload
  }

  private def invalidBinding(bindingEntry: Option[YMapEntry], binding: String, parameter: OasParameter): Unit = {
    val entryValueAnnotations = bindingEntry.map(e => Annotations(e.value)).getOrElse(Annotations())

    ctx.eh.violation(
      OasInvalidParameterBinding,
      "",
      s"Invalid parameter binding '$binding'",
      bindingEntry.map(_.value).getOrElse(map).location
    )

    parameter.domainElement match {
      case p: Parameter =>
        p.setWithoutId(
          ParameterModel.Binding,
          AmfScalar(p.binding.value(), entryValueAnnotations),
          entryValueAnnotations += InvalidBinding(binding)
        )
      case p: Payload =>
        p.add(InvalidBinding(binding))
      case _ => // ignore
    }
  }

  def defaultBinding: String = map.key("schema").map(_ => "body").getOrElse("query")

  protected def checkNotFileInBody(schema: Shape): Unit = {
    val schemaToCheck =
      if (schema.isLink) schema.linkTarget
      else schema
    if (schemaToCheck.isInstanceOf[FileShape])
      ctx.eh.violation(
        OasFormDataNotFileSpecification,
        schema,
        "File types in parameters must be declared in formData params",
        map.location
      )
  }

  protected def parseParameterRef(ref: YMapEntry, parentId: String): OasParameter = {
    val refUrl = OasDefinitions.stripParameterDefinitionsPrefix(ref.value)
    ctx.declarations.findParameter(refUrl, SearchScope.All) match {
      case Some(param) =>
        val parameter: Parameter = param.link(AmfScalar(refUrl), Annotations(map), Annotations.synthesized())
        parameter.withName(refUrl)
        OasParameter(parameter, Some(ref))
      case None =>
        ctx.declarations.findPayload(refUrl, SearchScope.All) match {
          case Some(payload) =>
            OasParameter(
              payload.link(AmfScalar(refUrl), Annotations(map), Annotations.synthesized()).asInstanceOf[Payload],
              Some(ref)
            )
          case None =>
            ctx.navigateToRemoteYNode(refUrl) match {
              case Some(result) =>
                val node         = YMapEntryLike(result.remoteNode)
                val newCtx       = result.context
                val oasParameter = newCtx.factory.parameterParser(node, parentId, nameNode, nameGenerator).parse
                oasParameter.domainElement.add(ExternalReferenceUrl(refUrl))
                oasParameter
              case _ =>
                val parameter: Parameter =
                  ErrorParameter(refUrl, ref).link(AmfScalar(refUrl), Annotations(ref), Annotations.synthesized())
                setName(parameter)
                ctx.eh.violation(
                  UnresolvedParameter,
                  parameter,
                  s"Cannot find parameter or payload reference $refUrl",
                  ref.location
                )
                OasParameter(parameter, Some(ref))
            }
        }
    }
  }
}

class Oas3ParameterParser(
    entryOrNode: YMapEntryLike,
    parentId: String,
    nameNode: Option[YNode],
    nameGenerator: IdCounter
)(implicit ctx: OasWebApiContext)
    extends Oas2ParameterParser(entryOrNode, parentId, nameNode, nameGenerator) {

  override def parse(): OasParameter = {
    map.key("$ref") match {
      case Some(ref) => parseParameterRef(ref, parentId)
      case None      => buildParameter()
    }
  }

  def buildParameter(): OasParameter = {
    val result = parseFixedFields()
    parseSchema(result)
    parseExamples(result)
    parseContent(result)
    parseQueryFields(result)
    Oas3ParameterParser.parseStyleField(map, result)
    Oas3ParameterParser.parseExplodeField(map, result)
    map.key("deprecated", ParameterModel.Deprecated in result)
    Oas3ParameterParser.validateSchemaOrContent(map, result)
    OasParameter(result)
  }

  private def parseQueryFields(result: Parameter): Unit = {
    result.binding
      .option()
      .foreach(b =>
        if (b == "query") {
          result.set(ParameterModel.AllowReserved, value = false)
          result.set(ParameterModel.AllowEmptyValue, value = false)
          map.key("allowReserved", (ParameterModel.AllowReserved in result).explicit)
          map.key("allowEmptyValue", (ParameterModel.AllowEmptyValue in result).explicit)
        }
      )
  }

  private def parseSchema(param: Parameter): Unit =
    map.key(
      "schema",
      entry => {
        OasTypeParser(entry, shape => setName(shape).asInstanceOf[Shape], OAS30SchemaVersion(SchemaPosition.Schema))(
          WebApiShapeParserContextAdapter(ctx).toOasNext
        )
          .parse()
          .map { schema =>
            ctx.autoGeneratedAnnotation(schema)
            param.setWithoutId(ParameterModel.Schema, schema, Annotations(entry))
          }
      }
    )

  private def parseExamples(param: Parameter): Unit = {
    def setShape(examples: Seq[Example], maybeEntry: Option[YMapEntry]): Unit =
      maybeEntry
        .map(entry => param.setWithoutId(PayloadModel.Examples, AmfArray(examples), Annotations(entry)))
        .getOrElse(param.setWithoutId(PayloadModel.Examples, AmfArray(examples)))

    OasExamplesParser(map, param)(WebApiShapeParserContextAdapter(ctx)).parse()
  }

  private def parseContent(param: Parameter): Unit = {
    val payloadProducer: Option[String] => Payload = mediaType => {
      val res = Payload()
      mediaType.map(res.withMediaType)
      res
    }
    map.key(
      "content",
      entry => {
        val payloads = OasContentsParser(entry, payloadProducer)(toOas(ctx)).parse()
        param.setWithoutId(ResponseModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
      }
    )
  }

}

object Oas3ParameterParser {

  lazy val validStyles: Map[String, List[String]] = Map(
    "matrix"         -> List("path"),
    "label"          -> List("path"),
    "form"           -> List("query", "cookie"),
    "simple"         -> List("path", "header"),
    "spaceDelimited" -> List("query"),
    "pipeDelimited"  -> List("query"),
    "deepObject"     -> List("query")
  )

  def parseExplodeField(map: YMap, result: Parameter)(implicit errorHandler: IllegalTypeHandler): Unit = {
    map.key("explode") match {
      case Some(entry) =>
        result.fields.setWithoutId(
          ParameterModel.Explode,
          AmfScalar(entry.value.as[Boolean], Annotations(entry.value)),
          Annotations(entry) += ExplicitField()
        )
      case None =>
        val defValue: Option[Boolean] = result.style.option().map {
          case "form" => true
          case _      => false
        }
        defValue.foreach(d => result.setWithoutId(ParameterModel.Explode, AmfScalar(d), Annotations.synthesized()))
    }
  }

  def parseStyleField(map: YMap, result: Parameter)(implicit ctx: WebApiContext): Unit = {
    map.key("style") match {
      case Some(entry) =>
        val styleText = entry.value.asScalar.map(_.text).getOrElse("")
        validateStyle(result, styleText, ctx)
        result.fields.setWithoutId(
          ParameterModel.Style,
          AmfScalar(styleText, Annotations(entry.value)),
          Annotations(entry) += ExplicitField()
        )
      case None =>
        val defValue: Option[String] = result.binding.option() match {
          case Some("query") | Some("cookie") => Some("form")
          case Some("path") | Some("header")  => Some("simple")
          case _                              => None
        }
        defValue.foreach(result.set(ParameterModel.Style, _, Annotations.synthesized()))
    }
  }

  private def isStyleValid(paramBinding: String, style: String): Boolean = Oas3ParameterParser.validStyles.exists {
    case (key, value) => key.equals(style) && value.contains(paramBinding)
  }

  private def validateStyle(param: Parameter, style: String, ctx: WebApiContext): Unit = {
    val paramBinding = param.binding.value()
    if (!isStyleValid(paramBinding, style))
      ctx.eh.violation(
        InvalidParameterStyleBindingCombination,
        param,
        s"'$style' style cannot be used with '$paramBinding' value of parameter property 'in'"
      )
  }

  def validateSchemaOrContent(map: YMap, param: Parameter)(implicit ctx: WebApiContext): Unit = {
    (map.key("schema"), map.key("content")) match {
      case (Some(_), Some(_)) | (None, None) =>
        ctx.eh.violation(
          ParserSideValidations.ParameterMissingSchemaOrContent,
          param,
          s"Parameter must define a 'schema' or 'content' field, but not both",
          param.annotations
        )
      case _ =>
    }
  }
}

case class OasParametersParser(values: Seq[YNode], parentId: String)(implicit ctx: OasWebApiContext) {

  def formDataPayload(formData: Seq[Payload]): Option[Payload] =
    if (formData.isEmpty) None
    else {
      val shape: NodeShape = NodeShape()
      val schema           = shape.withName("formData")

      formData.foreach { p =>
        val payload = if (p.isLink) p.effectiveLinkTarget().asInstanceOf[Payload] else p

        val property = schema.withProperty(payload.name.value())
        payload.annotations.find(classOf[RequiredParamPayload]) match {
          case None => property.set(PropertyShapeModel.MinCount, 0)
          case Some(a) if !a.required =>
            property.set(
              PropertyShapeModel.MinCount,
              AmfScalar(0, Annotations() += LexicalInformation(a.range) += ExplicitField())
            )
          case Some(a) =>
            property.set(
              PropertyShapeModel.MinCount,
              AmfScalar(1, Annotations() += LexicalInformation(a.range) += ExplicitField())
            )
        }

        Option(payload.schema).foreach(property.withRange(_))
      }

      Some(
        Payload()
          .withName("formData")
          .setWithoutId(PayloadModel.Schema, schema, Annotations.inferred())
          .add(FormBodyParameter())
      )
    }

  private case class ParameterInformation(oasParam: OasParameter, name: String, binding: String)

  def parse(inRequestOrEndpoint: Boolean = false): Parameters = {
    val nameGenerator = new IdCounter()
    val oasParameters = values
      .map(value => ctx.factory.parameterParser(YMapEntryLike(value), parentId, None, nameGenerator).parse)

    val formData = oasParameters.flatMap(_.formData)
    val body     = oasParameters.filter(_.isBody)
    body.foreach(_.domainElement.annotations += BodyParameter())

    validateDuplicated(oasParameters)

    if (inRequestOrEndpoint) {
      if (body.nonEmpty && formData.nonEmpty) {
        val bodyParam = body.head
        ctx.eh.violation(
          OasBodyAndFormDataParameterSpecification,
          bodyParam.domainElement,
          "Cannot declare 'body' and 'formData' params at the same time for a request or resource",
          bodyParam.ast.get.location
        )
      }

      validateOasPayloads(body, OasInvalidBodyParameter)
    }

    Parameters(
      oasParameters.flatMap(_.query) ++ oasParameters.flatMap(_.invalids),
      oasParameters.flatMap(_.path),
      oasParameters.flatMap(_.header),
      oasParameters.flatMap(_.cookie),
      Nil,
      body.flatMap(_.body) ++ formDataPayload(formData)
    )
  }

  private def validateDuplicated(oasParameters: Seq[OasParameter]): Unit = {
    val paramsInformation = oasParameters.flatMap(oasParam => {
      oasParam.element match {
        case Left(parameter) =>
          val effectiveParam = parameter.effectiveLinkTarget().asInstanceOf[Parameter]
          for {
            name    <- Option(effectiveParam.parameterName.value())
            binding <- Option(effectiveParam.binding.value())
          } yield ParameterInformation(oasParam, name, binding)
        case Right(payload) =>
          val effectivePayload = payload.effectiveLinkTarget().asInstanceOf[Payload]
          val name             = obtainName(effectivePayload)
          Some(ParameterInformation(oasParam, name, if (oasParam.isFormData) "formData" else "body"))
      }
    })

    val groupedByBinding = paramsInformation.groupBy { case ParameterInformation(_, name, binding) =>
      (name, binding)
    }.values
    groupedByBinding
      .foreach {
        case equalParams if equalParams.length > 1 =>
          equalParams.tail.foreach { case ParameterInformation(oasParam, name, binding) =>
            oasParam.ast match {
              case Some(ast) =>
                ctx.eh.violation(
                  DuplicatedParameters,
                  oasParam.domainElement,
                  s"Parameter $name of type $binding was found duplicated",
                  ast.location
                )
              case None =>
                ctx.eh.violation(
                  DuplicatedParameters,
                  oasParam.domainElement,
                  s"Parameter $name of type $binding was found duplicated"
                )
            }

          }
        case _ =>
      }
  }

  private def obtainName(payload: Payload): String =
    payload.fields
      .getValueAsOption(PayloadModel.Name)
      .flatMap(_.annotations.find(classOf[ParameterNameForPayload]).map(_.paramName))
      .getOrElse(payload.name.value())

  private def validateOasPayloads(params: Seq[OasParameter], id: ValidationSpecification): Unit =
    if (params.length > 1) {
      params.tail.foreach { param =>
        ctx.eh.violation(
          id,
          param.domainElement,
          "Cannot declare more than one 'body' parameter for a request or a resource",
          param.ast.get.location
        )
      }
    }
}
