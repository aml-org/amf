package amf.spec.domain

import amf.domain.Annotation.{ExplicitField, SynthesizedField}
import amf.domain.{Annotations, Parameter}
import amf.metadata.domain.ParameterModel
import amf.parser.{YMapOps, YScalarYRead}
import amf.plugins.domain.webapi.contexts.WebApiContext
import amf.shape.Shape
import amf.spec.{ParserContext, SearchScope}
import amf.spec.common.{AnnotationParser, ValueNode}
import amf.spec.declaration.{RamlTypeParser, RamlTypeSyntax}
import org.yaml.model.{YMap, YMapEntry, YScalar, YType}

/**
  *
  */
case class RamlParametersParser(map: YMap, producer: String => Parameter)(implicit ctx: WebApiContext) {
  def parse(): Seq[Parameter] =
    map.entries
      .map(entry => RamlParameterParser(entry, producer).parse())
}

case class RamlParameterParser(entry: YMapEntry, producer: String => Parameter)(implicit val ctx: WebApiContext)
    extends RamlTypeSyntax {
  def parse(): Parameter = {

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

        RamlTypeParser(entry, shape => shape.withName("schema").adopted(parameter.id))
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
            RamlTypeParser(
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
