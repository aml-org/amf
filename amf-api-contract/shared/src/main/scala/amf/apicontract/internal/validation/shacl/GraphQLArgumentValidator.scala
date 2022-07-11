package amf.apicontract.internal.validation.shacl

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{DataNode, NamedDomainElement, ScalarNode}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ScalarNodeModel
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.operations.ShapeParameter
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape, UnionShape}
import amf.validation.internal.shacl.custom.CustomShaclValidator.ValidationInfo

object GraphQLArgumentValidator {
  def validateDirectiveApplicationTypes(directive: GraphQLAppliedDirective): Seq[ValidationInfo] = {
    val definedProps: Seq[PropertyShape] = directive.definedProps()
    val parsedProps: Seq[ScalarNode]     = directive.parsedProps()

    parsedProps.flatMap { parsedProp =>
      val definition = findPropertyDefinition(definedProps, parsedProp)
      definition flatMap { definedProp =>
        val parsedDatatype: String          = parsedProp.dataType.value()
        val definedDatatype: Option[String] = getPropertyDatatype(definedProp)
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

  private def findPropertyDefinition(
      definedProps: Seq[PropertyShape],
      parsedProp: ScalarNode
  ): Option[PropertyShape] = {
    definedProps.find(_.name.value() == parsedProp.name.value())
  }

  def validateDefaultValues(node: NodeShape): Seq[ValidationInfo] = {
    val properties = node.properties.flatMap { property =>
      getPropertyDatatype(property) flatMap { declaredDatatype =>
        validateDefaultValue(declaredDatatype, property.default, property)
      }
    }
    val operations = node.operations.flatMap(_.requests).flatMap { _.queryParameters }.flatMap { param =>
      getParameterDatatype(param) flatMap { declaredDatatype =>
        validateDefaultValue(declaredDatatype, param.schema.default, param)
      }
    }
    properties ++ operations
  }

  private def validateDefaultValue[T <: NamedDomainElement](
      declaredDatatype: String,
      defaultValue: DataNode,
      shape: T
  ): Option[ValidationInfo] = {
    defaultValue match {
      case scalarNode: ScalarNode =>
        scalarNode.dataType.value() match {
          case s: String if s != declaredDatatype =>
            validationInfo(
              ScalarNodeModel.DataType,
              s"Default value of property ${shape.name.value()} must be of type $declaredDatatype",
              shape.annotations
            )
          case _ => None
        }
      case _ => None // TODO: take input objects into account when resolution and hierarchy is done
    }
  }

  private def validationInfo(field: Field, message: String, annotations: Annotations): Some[ValidationInfo] = {
    Some(ValidationInfo(field, Some(message), Some(annotations)))
  }

  private def getPropertyDatatype(definedProp: PropertyShape): Option[String] = {
    definedProp.range match {
      case u: UnionShape => // nullable type
        u.anyOf.collectFirst { case s: ScalarShape => s.dataType.value() }
      case s: ScalarShape => Some(s.dataType.value())
      case _              => None
    }
  }

  private def getParameterDatatype(param: ShapeParameter): Option[String] = {
    param.schema match {
      case u: UnionShape => // nullable type
        u.anyOf.collectFirst { case s: ScalarShape => s.dataType.value() }
      case s: ScalarShape => Some(s.dataType.value())
      case _              => None
    }
  }
}
