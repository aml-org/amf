package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{ExplicitField, SynthesizedField}
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.document.webapi.parser.spec.declaration.{Raml08TypeParser, Raml10TypeParser, RamlTypeSyntax}
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.models.Parameter
import org.yaml.model.{YMap, YMapEntry, YScalar, YType}

/**
  *
  */
case class RamlParametersParser(map: YMap, producer: String => Parameter, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext) {

  def parse(): Seq[Parameter] =
    map.entries
      .map(entry => ctx.factory.parameterParser(entry, producer, parseOptional).parse())
}

case class Raml10ParameterParser(entry: YMapEntry, producer: String => Parameter, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
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

case class Raml08ParameterParser(entry: YMapEntry, producer: String => Parameter, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlParameterParser(entry, producer) {
  def parse(): Parameter = {

    var name: String = entry.key
    val parameter    = producer(name).add(Annotations(entry))

    // Named Parameter Parse
    Raml08TypeParser(entry, name, entry.value, (s: Shape) => s.withName(name).adopted(parameter.id))
      .parse()
      .foreach(parameter.withSchema)

    parameter.schema.fields.entry(ShapeModel.RequiredShape) match {
      case Some(e) =>
        parameter.set(ParameterModel.Required, value = e.scalar.toBool)
      case None =>
        parameter.set(ParameterModel.Required, value = false)
    }

    if (parseOptional && name.endsWith("?")) {
      parameter.set(ParameterModel.Optional, value = true)
      name = name.stripSuffix("?")
      parameter.set(ParameterModel.Name, name)
    }

    parameter
  }
}

abstract class RamlParameterParser(entry: YMapEntry, producer: String => Parameter)(
    implicit val ctx: RamlWebApiContext)
    extends RamlTypeSyntax {
  def parse(): Parameter
}
