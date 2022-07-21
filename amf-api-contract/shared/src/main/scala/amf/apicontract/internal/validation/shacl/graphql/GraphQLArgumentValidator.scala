package amf.apicontract.internal.validation.shacl.graphql

import amf.core.client.scala.model.domain.{DataNode, NamedDomainElement, ScalarNode}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ScalarNodeModel
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.operations.AbstractParameter
import amf.validation.internal.shacl.custom.CustomShaclValidator.ValidationInfo

object GraphQLArgumentValidator {
  def validateDirectiveApplicationTypes(directive: GraphQLAppliedDirective): Seq[ValidationInfo] = {
    val definedProps: Seq[GraphQLProperty] = directive.definedProps()
    val parsedProps: Seq[ScalarNode]       = directive.parsedProps()

    parsedProps.flatMap { parsedProp =>
      val definition = definedProps.find(_.name == parsedProp.name.value())
      definition flatMap { definedProp =>
        val parsedDatatype: String          = parsedProp.dataType.value()
        val definedDatatype: Option[String] = definedProp.datatype
        definedDatatype
          .filter(x => x != parsedDatatype)
          .flatMap { datatype =>
            validationInfo(
              ScalarNodeModel.DataType,
              s"Property ${parsedProp.name.value()} must be of type $datatype",
              parsedProp.annotations
            )
          }
      }
    }
  }

  def validateDefaultValues(node: GraphQLObject): Seq[ValidationInfo] = {
    val properties = node.properties.flatMap { prop =>
      validateDefaultValue(prop.datatype.getOrElse(""), prop.default, prop.property, prop.annotations)
    }
    properties
  }

  def validateDefaultValues(parameter: AbstractParameter): Seq[ValidationInfo] = {
    GraphQLUtils
      .datatype(parameter.schema)
      .flatMap(validateDefaultValue(_, parameter.defaultValue, parameter.schema, parameter.annotations))
      .toSeq
  }

  private def validateDefaultValue[T <: NamedDomainElement](
      declaredDatatype: String,
      defaultValue: DataNode,
      shape: T,
      annotations: Annotations
  ): Option[ValidationInfo] = {
    defaultValue match {
      case scalarNode: ScalarNode =>
        scalarNode.dataType.value() match {
          case s: String if s != declaredDatatype =>
            validationInfo(
              ScalarNodeModel.DataType,
              s"Default value of property ${shape.name.value()} must be of type $declaredDatatype",
              annotations
            )
          case _ => None
        }
      case _ => None // TODO: take input objects into account when resolution and hierarchy is done
    }
  }

  private def validationInfo(field: Field, message: String, annotations: Annotations): Some[ValidationInfo] = {
    Some(ValidationInfo(field, Some(message), Some(annotations)))
  }
}
