package amf.apicontract.internal.validation.shacl.graphql

import amf.apicontract.internal.validation.shacl.graphql.values.ValueValidator
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain._
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ScalarNodeModel
import amf.core.internal.metamodel.domain.extensions.{DomainExtensionModel, PropertyShapePathModel}
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain._
import amf.shapes.client.scala.model.domain.federation.Key
import amf.shapes.internal.domain.metamodel.NodeShapeModel
import amf.shapes.internal.domain.metamodel.operations.AbstractParameterModel
import amf.validation.internal.shacl.custom.CustomShaclValidator.ValidationInfo

object GraphQLValidator {

  def checkValidPath(path: Seq[PropertyShape], key: Key): Seq[ValidationInfo] = {
    path.flatMap { propertyShape =>
      propertyShape.range match {
        case n: NodeShape =>
          if (n.isAbstract.value())
            validationInfo(
              PropertyShapePathModel.Path,
              s"Property '${propertyShape.name}' reference by field set can't be from an interface",
              key.annotations
            )
          else if (n.isInputOnly.value())
            validationInfo(
              PropertyShapePathModel.Path,
              s"Property '${propertyShape.name}' reference by field set can't be from an input type",
              key.annotations
            )
          else {
            if (n == path.last.range) {
              validationInfo(
                PropertyShapePathModel.Path,
                s"Property '${propertyShape.name}' reference by field set can't be an object type",
                key.annotations
              )
            } else None
          }
        case _: ScalarShape => None
        case _ =>
          if (propertyShape.name.value() != "error") {
            validationInfo(
              PropertyShapePathModel.Path,
              s"Property '${propertyShape.name}' must be from an object type",
              key.annotations
            )
          } else None
      }
    }
  }

  def validateKeyDirective(node: NodeShape): Seq[ValidationInfo] = {
    node.keys.flatMap { key =>
      if (node.isAbstract.value())
        validationInfo(NodeShapeModel.Keys, "The directive '@key' can't be applied to an interface", key.annotations)
      else if (node.isInputOnly.value())
        validationInfo(NodeShapeModel.Keys, "The directive '@key' can't be applied to an input type", key.annotations)
      else {
        val components = key.components
        components.flatMap { component =>
          checkValidPath(component.path, key)
        }
      }
    }
  }

  case class RequiredField(interface: String, field: GraphQLField)

  def validateRequiredFields(obj: GraphQLObject): Seq[ValidationInfo] = {

    val requiredFields: Seq[RequiredField] = obj.inherits.flatMap { interface =>
      interface.allFields().map { field => RequiredField(interface.name, field) }
    }

    requiredFields flatMap { requiredField =>
      val maybeActualField = obj.allFields().find(_.name == requiredField.field.name)
      maybeActualField match {
        case Some(actualField) =>
          (requiredField, actualField) match {
            case (RequiredField(interface, op: GraphQLOperation), actual: GraphQLOperation) =>
              validateArgumentTypes(op, interface, actual) ++ validateCovariance(requiredField, actual)
            case (RequiredField(_, _: GraphQLProperty), actual: GraphQLProperty) =>
              validateCovariance(requiredField, actual)
            case (RequiredField(interface, field: GraphQLOperation), _: GraphQLProperty) =>
              validationInfo(
                NodeShapeModel.Properties,
                s"Field '${field.name}' required by interface '$interface' is missing it's required arguments: ${field.parameters.map(_.name).mkString(", ")}",
                obj.annotations
              )
            case (RequiredField(interface, field: GraphQLProperty), _: GraphQLOperation) =>
              validationInfo(
                NodeShapeModel.Properties,
                s"Field '${field.name}' required by interface '$interface' has no arguments defined",
                obj.annotations
              )
            case _ => None
          }
        case None =>
          validationInfo(
            NodeShapeModel.Properties,
            s"Field '${requiredField.field.name}' required by interface '${requiredField.interface}' is missing in '${obj.name}'",
            obj.annotations
          )
      }
    }
  }

  private def validateArgumentTypes(
      requiredOp: GraphQLOperation,
      fromInterface: String,
      actual: GraphQLOperation
  ): Seq[ValidationInfo] = {
    val tuples = requiredOp.parameters.map { requiredArg =>
      (requiredArg, actual.parameters.find(_.name == requiredArg.name))
    }

    val incorrectDatatypeValidations = tuples flatMap {
      case (requiredArg: GraphQLParameter, None) =>
        validationInfo(
          AbstractParameterModel.Required,
          s"Field '${actual.name}' required by interface '$fromInterface' is missing its required argument '${requiredArg.name}''",
          actual.annotations
        )
      case (requiredArg: GraphQLParameter, Some(actualArg: GraphQLParameter)) =>
        if (requiredArg.datatype != actualArg.datatype) {
          validationInfo(
            AbstractParameterModel.Schema,
            s"Argument '${requiredArg.name}' of field '${requiredOp.name}' required by interface '$fromInterface' must be of type '${requiredArg.datatype
                .getOrElse("")}'",
            actualArg.annotations
          )
        } else None
      case _ => None
    }

    val notDefinedValidations = actual.parameters
      .filter { arg => !requiredOp.parameters.exists(_.name == arg.name) && arg.required }
      .flatMap { arg =>
        validationInfo(
          AbstractParameterModel.Schema,
          s"Field '${requiredOp.name}' required by interface '$fromInterface' does not define a non-optional argument '${arg.name}'",
          arg.annotations
        )
      }

    incorrectDatatypeValidations ++ notDefinedValidations
  }

  private def validateCovariance(requiredField: RequiredField, actual: GraphQLField): Seq[ValidationInfo] = {
    val message =
      s"Field '${actual.name}' required by interface '${requiredField.interface}' must be of type '${requiredField.field.datatype
          .getOrElse("")}'"
    val validation = (requiredField.field, actual) match {
      case (requiredProp: GraphQLProperty, actualProp: GraphQLProperty) =>
        if (requiredProp.minCount > actualProp.minCount) {
          validationInfo(
            AbstractParameterModel.Schema,
            s"field '${actual.name}' required by interface '${requiredField.interface}' can't be nullable because it's definition is non-nullable",
            actual.annotations
          )
        } else if (!isValidSubType(requiredProp.range, actualProp.range))
          validationInfo(AbstractParameterModel.Schema, message, actual.annotations)
        else None

      case (requiredOp: GraphQLOperation, actualOp: GraphQLOperation) =>
        (requiredOp.schema, actualOp.schema) match {
          case (Some(required), Some(actual)) =>
            if (!isValidSubType(required, actual)) {
              validationInfo(AbstractParameterModel.Schema, message, actual.annotations)
            } else None
          case _ => None
        }
      case _ => None
    }
    Seq(validation).flatten
  }

  private def isValidSubType(requiredOutput: Shape, actualOutput: Shape): Boolean = {
    (requiredOutput, actualOutput) match {
      case (required: ScalarShape, actual: ScalarShape) => required.dataType.value() == actual.dataType.value()
      case (required: NodeShape, actual: NodeShape) =>
        actual == required || checkInheritance(actual, required)
      case (required: ArrayShape, actual: ArrayShape) => isValidSubType(required.items, actual.items)
      case (required: UnionShape, actual: UnionShape) =>
        (required.anyOf, actual.anyOf) match {
          case (Seq(_: NilShape, req), Seq(_: NilShape, act)) => isValidSubType(req, act)
          case _                                              => actual == required
        }
      case (required: UnionShape, actual: NodeShape) => required.anyOf.contains(actual)
      case _                                         => true
    }
  }

  private def checkInheritance(actual: NodeShape, required: NodeShape): Boolean = {
    actual.inherits.contains(required) // todo: check for recursions (recursiveShapes)
  }

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
    val values = directive.propertyValues().map(value => value.name.value() -> value).toMap

    directive.definedProps().flatMap { prop =>
      values.get(prop.name) match {
        case Some(value) => ValueValidator.validate(prop.range, value)
        case None if prop.default.isEmpty && !prop.isNullable =>
          Seq(
            ValidationInfo(
              DomainExtensionModel.DefinedBy,
              Some(s"Missing required argument ${prop.name}"),
              Some(directive.annotations)
            )
          )
        case _ => Nil
      }
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
