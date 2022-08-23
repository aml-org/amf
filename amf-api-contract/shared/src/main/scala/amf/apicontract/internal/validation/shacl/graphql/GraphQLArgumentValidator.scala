package amf.apicontract.internal.validation.shacl.graphql

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.{DataNode, NamedDomainElement, ScalarNode, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ScalarNodeModel
import amf.core.internal.metamodel.domain.extensions.DomainExtensionModel
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.operations.AbstractParameter
import amf.shapes.client.scala.model.domain.{ArrayShape, UnionShape}
import amf.shapes.internal.domain.metamodel.NodeShapeModel
import amf.validation.internal.shacl.custom.CustomShaclValidator.ValidationInfo

object GraphQLArgumentValidator {
  // https://spec.graphql.org/June2018/#IsOutputType()
  def validateOutputTypes(obj: GraphQLObject): Seq[ValidationInfo] = {
    val fields = obj.fields()
    if (!obj.isInput) {
      // fields from an output type can return anything except an input type (`input Foo {...})
      val propertiesValidations = fields.properties.flatMap { prop =>
        if (!prop.isValidOutputType) {
          validationInfo(
            NodeShapeModel.Properties,
            s"Field '${prop.name}' must be an output type, '${getShapeName(prop.range)}' it's not",
            prop.annotations
          )
        } else None
      }

      val operationValidations = obj.operations.flatMap { op =>
        if (!op.isValidOutputType) {
          validationInfo(
            NodeShapeModel.Properties,
            s"Field '${op.name}' must return a valid an output type, '${getShapeName(op.payload.get.schema)}' it's not",
            op.annotations
          )
        } else None
      }

      operationValidations ++ propertiesValidations
    } else {
      // fields from an input type can only return valid input types, and this is already validated in validateInputTypes()
      Seq()
    }
  }

  def validateOutputTypes(endpoint: GraphQLEndpoint): Seq[ValidationInfo] = {
    endpoint.operations.flatMap { op =>
      if (!op.isValidOutputType) {
        validationInfo(
          NodeShapeModel.Properties,
          s"Field '${op.name}' type must be an output type, '${getShapeName(op.payload.get.schema)}' it's not",
          op.annotations
        )
      } else None
    }
  }

  def validateInputTypes(obj: GraphQLObject): Seq[ValidationInfo] = {
    // fields arguments can't be output types
    val operationValidations = obj.operations.flatMap { op =>
      op.parameters.flatMap { param =>
        if (!param.isValidInputType) {
          validationInfo(
            NodeShapeModel.Properties,
            s"Argument '${param.name}' must be an input type, '${getShapeName(param.schema)}' it's not",
            param.annotations
          )
        } else None
      }
    }

    // input type fields or directive arguments can't be output types
    val propertiesValidations = obj.properties.flatMap { prop =>
      if (!prop.isValidInputType && obj.isInput) {
        validationInfo(
          NodeShapeModel.Properties,
          s"Field '${prop.name}' must be an input type, '${getShapeName(prop.range)}' it's not",
          prop.annotations
        )
      } else None
    }

    operationValidations ++ propertiesValidations
  }

  def validateInputTypes(endpoint: GraphQLEndpoint): Seq[ValidationInfo] = {
    endpoint.parameters.flatMap { param =>
      if (!param.isValidInputType) {
        validationInfo(
          NodeShapeModel.Properties,
          s"Argument '${param.name}' type must be an input type, '${getShapeName(param.schema)}' it's not",
          param.annotations
        )
      } else None
    }
  }

  def validateDirectiveApplication(directive: GraphQLAppliedDirective): Seq[ValidationInfo] = {
    val definedProps: Seq[GraphQLProperty] = directive.definedProps()
    val parsedProps: Seq[ScalarNode]       = directive.parsedProps()

    // validate types
    val typesValidations = parsedProps.flatMap { parsedProp =>
      val definition = definedProps.find(_.name == parsedProp.name.value())
      definition flatMap { definedProp =>
        val parsedDatatype: String          = parsedProp.dataType.value()
        val definedDatatype: Option[String] = definedProp.datatype
        definedDatatype
          .filter(x => x != parsedDatatype && x != DataType.Any)
          .flatMap { datatype =>
            validationInfo(
              ScalarNodeModel.DataType,
              s"Property '${parsedProp.name.value()}' must be of type $datatype",
              parsedProp.annotations
            )
          }
      }
    }

    // validate missing arguments
    val missingArguments = definedProps.filter(_.default.isEmpty).flatMap { requiredProp =>
      if (!parsedProps.exists(_.name.value() == requiredProp.name)) {
        validationInfo(
          DomainExtensionModel.DefinedBy,
          s"Missing required argument ${requiredProp.name}",
          directive.annotations
        )
      } else None
    }

    typesValidations ++ missingArguments
  }

  def validateDefaultValues(node: GraphQLObject): Seq[ValidationInfo] = {
    node.properties.flatMap { prop =>
      validateDefaultValue(prop.datatype.getOrElse(""), prop.default.orNull, prop.property, prop.annotations)
    }
  }

  def validateIn(value: Option[DataNode], in: Seq[DataNode], field: Field): Option[ValidationInfo] = {
    (value, in.headOption) match {
      case (Some(value: ScalarNode), Some(_: ScalarNode)) =>
        val inScalar   = in.asInstanceOf[Seq[ScalarNode]]
        val conformsIn = inScalar.exists(_.value.value() == value.value.value())
        if (!conformsIn) {
          validationInfo(
            field,
            s"Default value of argument must be one of [${inScalar.map(_.value.value()).mkString(",")}]",
            value.annotations
          )
        } else None
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
