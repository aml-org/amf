package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{LexicalInformation, SynthesizedField}
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.{AmfScalar, Shape}
import amf.core.parser.{Annotations, _}
import amf.core.utils.Strings
import amf.plugins.document.webapi.annotations.FormBodyParameter
import amf.plugins.document.webapi.contexts.{OasWebApiContext, RamlWebApiContext}
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.{
  Raml08TypeParser,
  Raml10TypeParser,
  RamlTypeSyntax,
  StringDefaultType,
  _
}
import amf.plugins.document.webapi.parser.spec.raml.RamlTypeExpressionParser
import amf.plugins.domain.shapes.models.FileShape
import amf.plugins.domain.webapi.annotations.{InvalidBinding, ParameterBindingInBodyLexicalInfo}
import amf.plugins.domain.webapi.metamodel.{ParameterModel, PayloadModel}
import amf.plugins.domain.webapi.models.{Parameter, Payload}
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model.{YMap, YMapEntry, YScalar, YType, _}

/**
  *
  */
case class RamlParametersParser(map: YMap, adopted: Parameter => Unit, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext) {

  def parse(): Seq[Parameter] =
    map.entries
      .map(entry => ctx.factory.parameterParser(entry, adopted, parseOptional).parse())
}

object RamlHeaderParser {
  def parse(adopted: Parameter => Unit, parseOptional: Boolean = false)(node: YNode)(
      implicit ctx: RamlWebApiContext): Parameter = {
    RamlParameterParser.parse(adopted, parseOptional)(node).withBinding("header")
  }
}

object RamlQueryParameterParser {
  def parse(adopted: Parameter => Unit, parseOptional: Boolean = false)(node: YNode)(
      implicit ctx: RamlWebApiContext): Parameter = {
    RamlParameterParser.parse(adopted, parseOptional)(node).withBinding("query")
  }
}

object RamlParameterParser {
  def parse(adopted: Parameter => Unit, parseOptional: Boolean = false)(node: YNode)(
      implicit ctx: RamlWebApiContext): Parameter = {
    val head = node.as[YMap].entries.head
    ctx.factory.parameterParser(head, adopted, parseOptional).parse()
  }
}

case class Raml10ParameterParser(entry: YMapEntry, adopted: (Parameter) => Unit, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlParameterParser(entry, adopted) {
  override def parse(): Parameter = {

    val name      = ScalarNode(entry.key)
    val parameter = Parameter(entry).set(ParameterModel.Name, name.text()).withParameterName(name.text().toString) // TODO parameter id is using a name that is not final.
    adopted(parameter)

    val p = entry.value.to[YMap] match {
      case Right(map) =>
        map.key("required", (ParameterModel.Required in parameter).explicit.allowingAnnotations)
        map.key("description", (ParameterModel.Description in parameter).allowingAnnotations)
        map.key("binding".asRamlAnnotation, (ParameterModel.Binding in parameter).explicit)

        Raml10TypeParser(entry, shape => shape.withName("schema").adopted(parameter.id))
          .parse()
          .foreach(parameter.set(ParameterModel.Schema, _, Annotations(entry)))

        AnnotationParser(parameter, map).parse()

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
              shape => shape.withName("schema").adopted(parameter.id)
            ).parse().foreach { schema =>
              schema.annotations += SynthesizedField()
              parameter.set(ParameterModel.Schema, schema, Annotations(entry))
            }
            parameter
          case _ => // we have a property type
            entry.value.to[YScalar] match {
              case Right(ref) if ctx.declarations.findParameter(ref.text, scope).isDefined =>
                ctx.declarations
                  .findParameter(ref.text, scope)
                  .get
                  .link(ref.text, Annotations(entry))
                  .asInstanceOf[Parameter]
                  .set(ParameterModel.Name, name.text())
              case Right(ref) if ctx.declarations.findType(ref.text, scope).isDefined =>
                val schema = ctx.declarations
                  .findType(ref.text, scope)
                  .get
                  .link[Shape](ref.text, Annotations(entry))
                  .withName("schema")
                  .adopted(parameter.id)
                parameter.withSchema(schema)
              case Right(ref) if wellKnownType(ref.text) =>
                val schema = parseWellKnownTypeRef(ref.text).withName("schema").adopted(parameter.id)
                parameter.withSchema(schema)

              case Right(ref) if isTypeExpression(ref.text) =>
                RamlTypeExpressionParser((shape) => shape.withName("schema").adopted(parameter.id))
                  .parse(ref.text) match {
                  case Some(schema) => parameter.withSchema(schema)
                  case _ =>
                    ctx.violation(parameter.id,
                                  s"Cannot parse type expression for unresolved parameter '${parameter.name}'",
                                  entry.value)
                    parameter
                }
              case _ =>
                ctx.violation(parameter.id, "Cannot declare unresolved parameter", entry.value)
                parameter

            }
        }
    }

    if (p.fields.entry(ParameterModel.Required).isEmpty) {
      val stringName = name.text().toString
      val required   = !stringName.endsWith("?")
      val paramName  = if (required) stringName else stringName.stripSuffix("?")
      p.set(ParameterModel.Required, required)
      p.set(ParameterModel.Name, AmfScalar(paramName, name.text().annotations))
        .set(ParameterModel.ParameterName, paramName)
    }

    p
  }
}

case class Raml08ParameterParser(entry: YMapEntry, adopted: Parameter => Unit, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlParameterParser(entry, adopted) {
  def parse(): Parameter = {

    var name      = ScalarNode(entry.key)
    val parameter = Parameter(entry).set(ParameterModel.Name, name.text()).withParameterName(name.text().toString)
    adopted(parameter)

    entry.value.tagType match {
      case YType.Null =>
        Raml10TypeParser(
          entry,
          shape => shape.withName("schema").adopted(parameter.id)
        ).parse().foreach { schema =>
          schema.annotations += SynthesizedField()
          parameter.set(ParameterModel.Schema, schema, Annotations(entry))
        }
      case _ =>
        // Named Parameter Parse
        Raml08TypeParser(entry,
                         (s: Shape) => s.withName(name.text().toString).adopted(parameter.id),
                         isAnnotation = false,
                         StringDefaultType)
          .parse()
          .foreach(parameter.withSchema)
    }

    parameter.schema.fields.entry(ShapeModel.RequiredShape) match {
      case Some(e) =>
        parameter.set(ParameterModel.Required, value = e.scalar.toBool)
      case None =>
        parameter.set(ParameterModel.Required, value = false)
    }

    val stringName = name.text().toString
    if (parseOptional && stringName.endsWith("?")) {
      parameter.set(ParameterModel.Optional, value = true)
      val n = stringName.stripSuffix("?")
      parameter.set(ParameterModel.Name, AmfScalar(n, name.text().annotations)).set(ParameterModel.ParameterName, n)
    }

    parameter
  }
}

abstract class RamlParameterParser(entry: YMapEntry, adopted: Parameter => Unit)(implicit val ctx: RamlWebApiContext)
    extends RamlTypeSyntax
    with SpecParserOps {
  def parse(): Parameter
}

case class OasParameterParser(entryOrNode: Either[YMapEntry, YNode], parentId: String, nameNode: Option[YNode])(
    implicit ctx: OasWebApiContext)
    extends SpecParserOps {

  private val map = entryOrNode match {
    case Left(entry) => entry.value.as[YMap]
    case Right(node) => node.as[YMap]
  }

  private def setName(p: Parameter): Parameter = {
    if (nameNode.isDefined) {
      p.set(ParameterModel.Name, nameNode.map(ScalarNode(_).text()).get)
    } else {
      map.key("name", ParameterModel.Name in p) // name of the parameter in the HTTP binding (path, request parameter, etc)
    }
    p
  }
  def parse(): OasParameter = {
    map.key("$ref") match {
      case Some(ref) => parseParameterRef(ref, parentId)
      case None =>
        val p         = OasParameter(entryOrNode.toOption.getOrElse(entryOrNode.left.get))
        val parameter = setName(p.parameter)

        parameter.set(ParameterModel.Required, value = false)

        map.key("name", ParameterModel.ParameterName in parameter) // name of the parameter in the HTTP binding (path, request parameter, etc)
        map.key("in", ParameterModel.Binding in parameter)

        validateBinding(p)

        // This is for in: body parameters that might not have a name
        if ((p.isBody || p.isFormData) && parameter.name.isNullOrEmpty)
          parameter.withName("default")

        // type
        parameter.adopted(parentId)
        p.payload.adopted(parameter.id)

        map.key("description", ParameterModel.Description in parameter)
        map.key("required", (ParameterModel.Required in parameter).explicit)

        if (p.isBody) {
          ctx.closedShape(parameter.id, map, "bodyParameter")

          map.key(
            "schema",
            entry => {
              OasTypeParser(entry, (shape) => shape.withName("schema").adopted(p.payload.id))
                .parse()
                .map { schema =>
                  shapeFromOasParameter(parameter, schema)
                  checkNotFileInBody(schema)
                  p.payload.set(PayloadModel.Schema, schema, Annotations(entry))
                }
            }
          )

          map.key("mediaType".asOasExtension, PayloadModel.MediaType in p.payload)

        } else {

          ctx.closedShape(parameter.id, map, "parameter")
          OasTypeParser(
            entryOrNode,
            "schema",
            map,
            shape => shape.withName("schema").adopted(parameter.id),
            OAS20SchemaVersion(position = "parameter")
          ).parse()
            .map { schema =>
              if (p.isFormData) {
                shapeFromOasParameter(parameter, schema)
                p.payload.set(PayloadModel.Schema, schema, Annotations(map))
              } else parameter.set(ParameterModel.Schema, schema, Annotations(map))
            }
            .orElse {
              ctx.violation(
                ParserSideValidations.ParsingErrorSpecification.id(),
                p.payload.id,
                "Cannot find valid schema for parameter",
                map
              )
              None
            }

          if (p.isFormData) p.payload.annotations += FormBodyParameter()
        }

        AnnotationParser(parameter, map).parse()

        p
    }
  }

  private def validateBinding(parameter: OasParameter): Unit = if (parameter.hasInvalidBinding) {
    val old                   = parameter.parameter.binding.value()
    val entry                 = map.key("in")
    val entryAnnotations      = entry.map(Annotations(_)).getOrElse(Annotations())
    val entryValueAnnotations = entry.map(e => Annotations(e.value)).getOrElse(Annotations())

    ctx.violation(ParserSideValidations.OasInvalidParameterBinding.id(),
                  s"Invalid parameter binding '$old'",
                  entry.map(_.value).getOrElse(map))

    parameter.parameter.set(ParameterModel.Binding,
                            AmfScalar(defaultBinding, entryValueAnnotations),
                            entryAnnotations += InvalidBinding(old))
    if (parameter.isBody) parameter.payload.add(InvalidBinding(old))
  }

  def defaultBinding: String = map.key("schema").map(_ => "body").getOrElse("query")

  protected def checkNotFileInBody(schema: Shape): Unit = {
    val schemaToCheck =
      if (schema.isLink) schema.linkTarget
      else schema
    if (schemaToCheck.isInstanceOf[FileShape])
      ctx.violation(
        ParserSideValidations.OasFormDataNotFileSpecification.id(),
        schema.id,
        "File types in parameters must be declared in formData params",
        map
      )
  }

  protected def shapeFromOasParameter(parameter: Parameter, schema: Shape): Shape = {
    parameter.parameterName.option() match {
      case Some(paramName) => schema.set(ShapeModel.Name, AmfScalar(paramName), parameter.parameterName.annotations())
      case None            => schema.withName("schema")
    }
    parameter.description.option() match {
      case Some(description) =>
        schema.set(ShapeModel.Description, AmfScalar(description), parameter.description.annotations())
      case None => // ignore
    }
    parameter.binding.annotations().find(classOf[LexicalInformation]).foreach { lexicalInfo =>
      schema.annotations += ParameterBindingInBodyLexicalInfo(lexicalInfo.range)
    }
    schema
  }

  protected def parseParameterRef(ref: YMapEntry, parentId: String): OasParameter = {
    val refUrl = OasDefinitions.stripParameterDefinitionsPrefix(ref.value)
    ctx.declarations.findParameter(refUrl, SearchScope.All) match {
      case Some(p) =>
        val payload: Payload     = ctx.declarations.parameterPayload(p)
        val parameter: Parameter = p.link(refUrl, Annotations(map))
        parameter.withName(refUrl).adopted(parentId)
        OasParameter(parameter, payload)
      case None =>
        val oasParameter = OasParameter(Parameter(YMap.empty), Payload(YMap.empty))
        ctx.violation(oasParameter.parameter.id, s"Cannot find parameter reference $refUrl", ref)
        oasParameter
    }
  }
}

case class OasParametersParser(values: Seq[YNode], parentId: String)(implicit ctx: OasWebApiContext) {
  def parse(inRequest: Boolean = false): Parameters = {
    val parameters = values
      .map(value => OasParameterParser(Right(value), parentId, None).parse())

    if (inRequest) {
      val body     = parameters.filter(_.isBody)
      val formData = parameters.filter(_.isFormData)

      if (body.nonEmpty && formData.nonEmpty) {
        val bodyParam = body.head
        ctx.violation(
          ParserSideValidations.OasBodyAndFormDataParameterSpecification.id(),
          bodyParam.payload.id,
          "Cannot declare body and formData params at the same time for a request",
          bodyParam.ast.get
        )
      }

      validateOasPayloads(body, ParserSideValidations.OasInvalidBodyParameter.id())
      validateOasPayloads(formData, ParserSideValidations.OasInvalidFormDataParameter.id())
    }

    Parameters(
      parameters.filter(_.isQuery).map(_.parameter),
      parameters.filter(_.isPath).map(_.parameter),
      parameters.filter(_.isHeader).map(_.parameter),
      Nil,
      (parameters.filter(_.isBody) ++ parameters.filter(_.isFormData)).map(_.payload)
    )
  }

  private def validateOasPayloads(params: Seq[OasParameter], id: String): Unit = if (params.length > 1) {
    params.tail.foreach { param =>
      ctx.violation(
        id,
        param.payload.id,
        "Cannot declare more than one payload parameter for a request",
        param.ast.get
      )
    }
  }
}
