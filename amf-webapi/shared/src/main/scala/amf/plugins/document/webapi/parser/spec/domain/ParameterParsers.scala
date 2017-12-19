package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{ExplicitField, SynthesizedField}
import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.document.webapi.parser.spec.declaration.{Raml08TypeParser, Raml10TypeParser, RamlTypeSyntax}
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.models.Parameter
import org.yaml.model.{YMap, YMapEntry, YScalar, YType}

/**
  *
  */
case class Raml10ParametersParser(map: YMap, producer: String => Parameter)(implicit ctx: WebApiContext)
    extends RamlParametersParser(map, producer) {

  override def parameterParser: (YMapEntry, (String) => Parameter) => RamlParameterParser = Raml10ParameterParser.apply
}

case class Raml08ParametersParser(map: YMap, producer: String => Parameter)(implicit ctx: WebApiContext)
    extends RamlParametersParser(map, producer) {

  override def parameterParser: (YMapEntry, (String) => Parameter) => RamlParameterParser = Raml08ParameterParser.apply
}

abstract class RamlParametersParser(map: YMap, producer: String => Parameter)(implicit ctx: WebApiContext) {

  def parameterParser: (YMapEntry, (String) => Parameter) => RamlParameterParser

  def parse(): Seq[Parameter] =
    map.entries
      .map(entry => parameterParser(entry, producer).parse())
}

case class Raml10ParameterParser(entry: YMapEntry, producer: String => Parameter)(implicit ctx: WebApiContext)
    extends RamlParameterParser(entry, producer) {
  override def parse(): Parameter = {

    val name: String = entry.key
    val parameter    = producer(name).add(Annotations(entry)) // TODO parameter id is using a name that is not final.

    val p = entry.value.to[YMap] match {
      case Right(map) =>
        map.key("required", entry => {
          val value = ValueNode(entry.value)
          parameter.set(ParameterModel.Required, value.boolean(), Annotations(entry) += ExplicitField())
        })

        map.key("description", entry => {
          val value = ValueNode(entry.value)
          parameter.set(ParameterModel.Description, value.string(), Annotations(entry))
        })

        map.key(
          "(binding)",
          entry => {
            val value                    = ValueNode(entry.value)
            val annotations: Annotations = Annotations(entry) += ExplicitField()
            parameter.set(ParameterModel.Binding, value.string(), annotations)
          }
        )

        Raml10TypeParser(entry, shape => shape.withName("schema").adopted(parameter.id))
          .parse()
          .foreach(parameter.set(ParameterModel.Schema, _, Annotations(entry)))

        AnnotationParser(() => parameter, map).parse()

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

              case _ =>
                ctx.violation(parameter.id, "Cannot declare unresolved parameter", entry.value)
                parameter

            }
        }
    }

    if (p.fields.entry(ParameterModel.Required).isEmpty) {
      val required = !name.endsWith("?")

      p.set(ParameterModel.Required, required)
      p.set(ParameterModel.Name, if (required) name else name.stripSuffix("?"))
    }

    p
  }
}

case class Raml08ParameterParser(entry: YMapEntry, producer: String => Parameter)(implicit ctx: WebApiContext)
    extends RamlParameterParser(entry, producer) {
  def parse(): Parameter = {

    val name: String = entry.key
    val parameter    = producer(name).add(Annotations(entry)) // TODO parameter id is using a name that is not final.

    // Named Parameter Parse
    Raml08TypeParser(entry, name, entry.value, (s: Shape) => s.withName(name).adopted(parameter.id))
      .parse()
      .foreach(parameter.withSchema)

    parameter
  }
}

abstract class RamlParameterParser(entry: YMapEntry, producer: String => Parameter)(implicit val ctx: WebApiContext)
    extends RamlTypeSyntax {
  def parse(): Parameter
}
