package amf.spec.domain

import amf.domain.Annotations
import amf.domain.`abstract`.{AbstractDeclaration, ParametrizedDeclaration, VariableValue}
import amf.metadata.domain.`abstract`.ParametrizedDeclarationModel
import amf.parser.YValueOps
import org.yaml.model.{YMap, YScalar, YValue}

/**
  *
  */
case class ParametrizedDeclarationParser(value: YValue,
                                         producer: String => ParametrizedDeclaration,
                                         declarations: (String) => AbstractDeclaration) {
  def parse(): ParametrizedDeclaration = {
    value match {
      case map: YMap =>
        // TODO is it always the first child?
        val entry = map.entries.head

        val name = entry.key.value.toScalar.text
        val declaration =
          producer(name).add(Annotations(value)).set(ParametrizedDeclarationModel.Target, declarations(name).id)
        val variables = entry.value.value.toMap.entries.map(
          variableEntry =>
            VariableValue(variableEntry)
              .withName(variableEntry.key.value.toScalar.text)
              .withValue(variableEntry.value.value.toScalar.text))

        declaration.withVariables(variables)
      case scalar: YScalar =>
        producer(scalar.text)
          .add(Annotations(value))
          .set(ParametrizedDeclarationModel.Target, declarations(scalar.text).id)
      case _ => throw new Exception("Invalid model extension.")
    }
  }
}
