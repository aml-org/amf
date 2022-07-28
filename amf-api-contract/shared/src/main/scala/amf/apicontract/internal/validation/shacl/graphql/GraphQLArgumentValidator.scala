package amf.apicontract.internal.validation.shacl.graphql

import amf.core.client.scala.model.domain.{DataNode, NamedDomainElement, ScalarNode, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ScalarNodeModel
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{ArrayShape, UnionShape}
import amf.shapes.client.scala.model.domain.operations.AbstractParameter
import amf.shapes.internal.domain.metamodel.NodeShapeModel
import amf.validation.internal.shacl.custom.CustomShaclValidator.ValidationInfo

object GraphQLArgumentValidator {
  def validateInputTypes(obj: GraphQLObject): Seq[ValidationInfo] = {
    // fields arguments can't be output types
    val operationValidations = obj.operations.flatMap { op =>
      op.parameters.flatMap { param =>
        if (!param.isValidInputType) {
          validationInfo(
            NodeShapeModel.Properties,
            s"Argument '${param.name}' must be an input type, ${getShapeName(param.schema)} it's not",
            param.annotations
          )
        } else {
          None
        }
      }
    }

    // input type fields or directive arguments can't be output types
    val propertiesValidations = obj.properties.flatMap { prop =>
      if (!prop.isValidInputType) {
        validationInfo(
          NodeShapeModel.Properties,
          s"${prop.name} must be an input type, ${getShapeName(prop.range)} it's not",
          prop.annotations
        )
      } else None
    }

    operationValidations ++ propertiesValidations
  }

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
    node.properties.flatMap { prop =>
      validateDefaultValue(prop.datatype.getOrElse(""), prop.default, prop.property, prop.annotations)
    }
  }

  def validateIn(value: DataNode, in: Seq[DataNode], field: Field): Option[ValidationInfo] = {
    (value, in.headOption) match {
      case (value: ScalarNode, _ @Some(_: ScalarNode)) =>
        val inScalar   = in.asInstanceOf[Seq[ScalarNode]]
        val conformsIn = inScalar.exists(_.value.value() == value.value.value())
        if (!conformsIn) {
          validationInfo(
            field,
            s"Default value of argument must be one of [${inScalar.map(_.value.value()).mkString(",")}]",
            value.annotations
          )
        } else {
          None
        }
      case _ => None
    }
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
              s"Default value of argument ${shape.name.value()} must be of type $declaredDatatype",
              annotations
            )
          case _ => None
        }
      case _ => None // TODO: take input objects into account when resolution and hierarchy is done
    }
  }

  private def getShapeName(shape: Shape): String = shape match {
    case u: UnionShape   => GraphQLNullable(u).name
    case arr: ArrayShape => s"a list of ${getShapeName(arr.items)}"
    case s               => s.name.value()
  }

  private def validationInfo(field: Field, message: String, annotations: Annotations): Some[ValidationInfo] = {
    Some(ValidationInfo(field, Some(message), Some(annotations)))
  }
}
