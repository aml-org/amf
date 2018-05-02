package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{LexicalInformation, SynthesizedField}
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.{AmfScalar, Shape}
import amf.core.parser.{Annotations, _}
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
import amf.core.utils.Strings
import amf.plugins.document.webapi.annotations.FormBodyParameter

/**
  *
  */
case class RamlParametersParser(map: YMap, producer: String => Parameter, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext) {

  def parse(): Seq[Parameter] =
    map.entries
      .map(entry => ctx.factory.parameterParser(entry, producer, parseOptional).parse())
}

object RamlHeaderParser {
  def parse(producer: String => Parameter, parseOptional: Boolean = false)(node: YNode)(
      implicit ctx: RamlWebApiContext): Parameter = {
    RamlParameterParser.parse(producer, parseOptional)(node).withBinding("header")
  }
}

object RamlQueryParameterParser {
  def parse(producer: String => Parameter, parseOptional: Boolean = false)(node: YNode)(
      implicit ctx: RamlWebApiContext): Parameter = {
    RamlParameterParser.parse(producer, parseOptional)(node).withBinding("query")
  }
}

object RamlParameterParser {
  def parse(producer: String => Parameter, parseOptional: Boolean = false)(node: YNode)(
      implicit ctx: RamlWebApiContext): Parameter = {
    val head = node.as[YMap].entries.head
    ctx.factory.parameterParser(head, producer, parseOptional).parse()
  }
}

case class Raml10ParameterParser(entry: YMapEntry, producer: String => Parameter, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlParameterParser(entry, producer) {
  override def parse(): Parameter = {

    val name: String = entry.key.as[YScalar].text
    val parameter    = producer(name).withParameterName(name).add(Annotations(entry)) // TODO parameter id is using a name that is not final.

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
                  .withName(name)
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
      val required  = !name.endsWith("?")
      val paramName = if (required) name else name.stripSuffix("?")
      p.set(ParameterModel.Required, required)
      p.set(ParameterModel.Name, paramName).set(ParameterModel.ParameterName, paramName)
    }

    p
  }
}

case class Raml08ParameterParser(entry: YMapEntry, producer: String => Parameter, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlParameterParser(entry, producer) {
  def parse(): Parameter = {

    var name: String = entry.key.as[YScalar].text
    val parameter    = producer(name).withParameterName(name).add(Annotations(entry))

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
                         name,
                         entry.value,
                         (s: Shape) => s.withName(name).adopted(parameter.id),
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

    if (parseOptional && name.endsWith("?")) {
      parameter.set(ParameterModel.Optional, value = true)
      name = name.stripSuffix("?")
      parameter.set(ParameterModel.Name, name).set(ParameterModel.ParameterName, name)
    }

    parameter
  }
}

abstract class RamlParameterParser(entry: YMapEntry, producer: String => Parameter)(
    implicit val ctx: RamlWebApiContext)
    extends RamlTypeSyntax
    with SpecParserOps {
  def parse(): Parameter
}

case class OasParameterParser(map: YMap, parentId: String, name: Option[String])(implicit ctx: OasWebApiContext)
    extends SpecParserOps {

  def parse(): OasParameter = {
    map.key("$ref") match {
      case Some(ref) => parseParameterRef(ref, parentId)
      case None =>
        val p         = OasParameter(map)
        val parameter = p.parameter

        parameter.set(ParameterModel.Required, value = false)

        if (name.isDefined) {
          parameter.set(ParameterModel.Name, name.get)
        } else {
          map.key("name", ParameterModel.Name in parameter) // name of the parameter in the HTTP binding (path, request parameter, etc)
        }
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
            map,
            "",
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

case class OasParametersParser(values: Seq[YMap], parentId: String)(implicit ctx: OasWebApiContext) {
  def parse(inRequest: Boolean = false): Parameters = {
    val parameters = values
      .map(value => OasParameterParser(value, parentId, None).parse())

    if (inRequest) {
      if (parameters.exists(_.isBody) && parameters.exists(_.isFormData)) {
        val bodyParam = parameters.find(_.isBody).head
        ctx.violation(
          ParserSideValidations.OasBodyAndFormDataParameterSpecification.id(),
          bodyParam.payload.id,
          "Cannot declare body and formData params at the same time for a request",
          bodyParam.ast.get
        )
      }

    }

    Parameters(
      parameters.filter(_.isQuery).map(_.parameter),
      parameters.filter(_.isPath).map(_.parameter),
      parameters.filter(_.isHeader).map(_.parameter),
      Nil,
      parameters.filter(_.isBody).map(_.payload).headOption.orElse {
        parameters.filter(_.isFormData).map(_.payload).headOption
      }
    )
  }
}
