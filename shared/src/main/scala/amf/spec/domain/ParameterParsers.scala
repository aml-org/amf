package amf.spec.domain

import amf.domain.Annotation.ExplicitField
import amf.domain.{Annotations, Parameter}
import amf.metadata.domain.ParameterModel
import amf.shape.Shape
import amf.spec.Declarations
import amf.spec.common.{AnnotationParser, ErrorReporterParser, ValueNode}
import amf.spec.declaration.{RamlTypeParser, RamlTypeSyntax}
import amf.validation.{Validation, ValidationAware}
import org.yaml.model.{YMap, YMapEntry, YScalar}
import amf.parser.YMapOps

/**
  *
  */
case class RamlParametersParser(map: YMap, producer: String => Parameter, declarations: Declarations) {
  def parse(): Seq[Parameter] =
    map.entries
      .map(entry => RamlParameterParser(entry, producer, declarations).parse())
}

case class RamlParameterParser(entry: YMapEntry, producer: String => Parameter, declarations: Declarations)
    extends RamlTypeSyntax
    with ValidationAware {
  def parse(): Parameter = {

    val name: String = entry.key
    val parameter    = producer(name).add(Annotations(entry)) // TODO parameter id is using a name that is not final.

    val p = entry.value.value match {
      case ref: YScalar if declarations.findParameter(ref.text).isDefined =>
        declarations
          .findParameter(ref.text)
          .get
          .link(ref.text, Annotations(entry))
          .asInstanceOf[Parameter]
          .withName(name)

      case ref: YScalar if declarations.findType(ref.text).isDefined =>
        val schema = declarations
          .findType(ref.text)
          .get
          .link[Shape](ref.text, Annotations(entry))
          .withName("schema")
          .adopted(parameter.id)
        parameter.withSchema(schema)

      case ref: YScalar if wellKnownType(ref.text) =>
        val schema = parseWellKnownTypeRef(ref.text).withName("schema").adopted(parameter.id)
        parameter.withSchema(schema)

      case _: YScalar =>
        parsingErrorReport(currentValidation,
                           parameter.id,
                           "Cannot declare unresolved parameter",
                           Some(entry.value.value))
        parameter

      case map: YMap =>
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

        RamlTypeParser(entry, shape => shape.withName("schema").adopted(parameter.id), declarations, currentValidation)
          .parse()
          .foreach(parameter.set(ParameterModel.Schema, _, Annotations(entry)))

        AnnotationParser(() => parameter, map).parse()

        parameter
    }

    if (p.fields.entry(ParameterModel.Required).isEmpty) {
      val required = !name.endsWith("?")

      p.set(ParameterModel.Required, required)
      p.set(ParameterModel.Name, if (required) name else name.stripSuffix("?"))
    }

    p
  }
}
