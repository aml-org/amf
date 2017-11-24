package amf.validation.model

import amf.framework.model.domain.AmfScalar
import amf.plugins.domain.shapes.metamodel.{NodeShapeModel, PropertyShapeModel, ScalarShapeModel}
import amf.plugins.domain.shapes.models._
import amf.resolution.pipelines.CanonicalShapePipeline
import amf.framework.vocabulary.Namespace

class AMFShapeValidations(shape: Shape) {

  def profile() = {
    val parsedValidations = validations()
    ValidationProfile(
      name = "Payload",
      baseProfileName = None,
      violationLevel = parsedValidations.map(_.name),
      validations = parsedValidations ++ ParserSideValidations.validations
    )
  }

  protected def validations(): List[ValidationSpecification] = emitShapeValidations("/", canonicalShape())

  protected def emitShapeValidations(context: String, shape: Shape): List[ValidationSpecification] = {
    shape match {
      case union: UnionShape   =>  unionConstraints(context, union)
      case scalar: ScalarShape =>  scalarConstraints(context, scalar)
      case array: ArrayShape   =>  arrayConstraints(context, array)
      case obj: NodeShape      =>  nodeConstraints(context, obj)
      case _: AnyShape         =>  List.empty
      case _                   =>  List.empty
    }
  }

  def validationId(shape: Shape) = shape.id + "_validation"

  protected def canonicalShape(): Shape = CanonicalShapePipeline(shape)

  protected def unionConstraints(context: String, union: UnionShape): List[ValidationSpecification] = {
    val msg = s"Data at $context must be one of the valid union types"
    var nestedConstraints: List[ValidationSpecification] = List.empty
    var count = 0
    union.anyOf.foreach { shape =>
      nestedConstraints ++= emitShapeValidations(context + s"/union_$count", shape)
      count += 1
    }
    val validation = new ValidationSpecification(
      name = validationId(union),
      message = msg,
      ramlMessage = Some(msg),
      oasMessage = Some(msg),
      unionConstraints = union.anyOf.map(s => validationId(s))
    )
    List(validation) ++ nestedConstraints
  }

  protected def arrayConstraints(context: String, array: ArrayShape) = {
    val msg = s"Array at $context must be valid"
    var nestedConstraints: List[ValidationSpecification] = List.empty
    var validation = new ValidationSpecification(
      name = validationId(array),
      message = msg,
      ramlMessage = Some(msg),
      oasMessage = Some(msg),
      targetClass = Seq.empty,
      propertyConstraints = Seq()
    )

    nestedConstraints ++= emitShapeValidations(context + s"/items", array.items)

    val itemsValidationId = validationId(array) + "/prop"
    val itemsConstraint = new PropertyConstraint(
      ramlPropertyId = (Namespace.Rdf + "member").iri(),
      name = itemsValidationId,
      message = Some(s"Array items at $context must be valid"),
      node = Some(validationId(array.items))
    )
    validation = validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(itemsConstraint))
    validation = checkArrayType(array, context, validation)
    List(validation) ++ nestedConstraints
  }

  protected def nodeConstraints(context: String, node: NodeShape): List[ValidationSpecification] = {
    val msg = s"Object at $context must be valid"
    var nestedConstraints: List[ValidationSpecification] = List.empty
    var validation = new ValidationSpecification(
      name = validationId(node),
      message = msg,
      ramlMessage = Some(msg),
      oasMessage = Some(msg),
      targetClass = Seq.empty,
      propertyConstraints = Seq()
    )

    node.properties.foreach { property =>
      nestedConstraints ++= emitShapeValidations(context + s"/${property.name}", property.range)

      val propertyValidationId = validationId(property.range)
      val propertyId = (Namespace.Data + property.name).iri()
      val nodeConstraint = new PropertyConstraint(
        ramlPropertyId = propertyId,
        name = validationId(node) + s"_validation_node_prop_${property.name}",
        message = Some(s"Property ${property.name} at $context must have a valid value"),
        node = Some(propertyValidationId)
      )
      validation = validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(nodeConstraint))
      validation = checkMinCount(context + s"/${property.name}", property, validation, property)
      validation = checkMaxCount(context + s"/${property.name}", property, validation, property)
    }

    validation = checkClosed(validation, node)
    validation = checkObjectType(node, context, validation)
    List(validation) ++ nestedConstraints
  }

  protected def checkClosed(validation: ValidationSpecification, shape: NodeShape): ValidationSpecification = {
    shape.fields.?[AmfScalar](NodeShapeModel.Closed) match {
      case Some(value) if value.toBool => validation.copy(closed = Some(true))
      case _ => validation
    }
  }

  protected def scalarConstraints(context: String, scalar: ScalarShape): List[ValidationSpecification] = {
    val msg = s"Scalar at $context must be valid"
    var validation = new ValidationSpecification(
      name = validationId(scalar),
      message = msg,
      ramlMessage = Some(msg),
      oasMessage = Some(msg),
      targetClass = Seq.empty,
      propertyConstraints = Seq(new PropertyConstraint(
        ramlPropertyId = (Namespace.Data + "value").iri(),
        name = scalar.id + "_validation_range/prop",
        message = Some(s"Scalar at $context must have data type ${scalar.dataType}"),
        datatype = Some(scalar.dataType)
      ))
    )
    validation = checkScalarType(scalar, context, validation)
    validation = checkPattern(context, validation, scalar)
    validation = checkMinLength(context, validation, scalar)
    validation = checkMaxLength(context, validation, scalar)
    validation = checkMinimum(context, validation, scalar)
    validation = checkMaximum(context, validation, scalar)
    validation = checkMinimumExclusive(context, validation, scalar)
    validation = checkMaximumExclusive(context, validation, scalar)
    List(validation)
  }

  protected def checkScalarType(shape: Shape, context: String, validation: ValidationSpecification): ValidationSpecification = {
    val msg = s"Data at $context must be a scalar"
    val propertyValidation = new PropertyConstraint(
      ramlPropertyId = (Namespace.Rdf + "type").iri(),
      name = validation.name +  "_validation_type/prop",
      message = Some(msg),
      in = Seq((Namespace.Data + "Scalar").iri())
    )
    validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
  }

  protected def checkObjectType(shape: Shape, context: String, validation: ValidationSpecification): ValidationSpecification = {
    val msg = s"Data at $context must be an object"
    val propertyValidation = new PropertyConstraint(
      ramlPropertyId = (Namespace.Rdf + "type").iri(),
      name = validation.name +  "_validation_type/prop",
      message = Some(msg),
      in = Seq((Namespace.Data + "Object").iri())
    )
    validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
  }

  protected def checkArrayType(shape: Shape, context: String, validation: ValidationSpecification): ValidationSpecification = {
    val msg = s"Data at $context must be an array"
    val propertyValidation = new PropertyConstraint(
      ramlPropertyId = (Namespace.Rdf + "type").iri(),
      name = validation.name +  "_validation_type/prop",
      message = Some(msg),
      in = Seq((Namespace.Rdf + "Seq").iri(), (Namespace.Data + "Array").iri())
    )
    validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
  }

  protected def checkMinCount(context: String, property: PropertyShape, validation: ValidationSpecification, shape: PropertyShape): ValidationSpecification = {
    shape.fields.?[AmfScalar](PropertyShapeModel.MinCount) match {
      case Some(minCount) if minCount.toNumber.intValue() > 0  =>
        val msg = s"Data at $context must have min. cardinality $minCount"
        val propertyValidation = new PropertyConstraint(
          ramlPropertyId = (Namespace.Data + shape.name).iri(),
          name = validation.name + "_" + property.name + "_validation_minCount/prop",
          message = Some(msg),
          minCount = Some(s"$minCount")
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case _ => validation
    }
  }

  protected def checkMaxCount(context: String, property: PropertyShape, validation: ValidationSpecification, shape: PropertyShape): ValidationSpecification = {
    shape.fields.?[AmfScalar](PropertyShapeModel.MaxCount) match {
      case Some(maxCount)  =>
        val msg = s"Data at $context must have max. cardinality $maxCount"
        val propertyValidation = new PropertyConstraint(
          ramlPropertyId = (Namespace.Data + shape.name).iri(),
          name = validation.name + "_" + property.name + "_validation_minCount/prop",
          message = Some(msg),
          maxCount = Some(s"$maxCount")
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkPattern(context: String, validation: ValidationSpecification, shape: Shape with CommonShapeFields): ValidationSpecification = {
    Option(shape.pattern) match {
      case Some(pattern) =>
        val msg = s"Data at $context must match pattern $pattern"
        val propertyValidation = new PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_pattern/prop",
          message = Some(msg),
          pattern = Some(pattern)
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMinLength(context: String, validation: ValidationSpecification, shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.fields.?[AmfScalar](ScalarShapeModel.MinLength) match {
      case Some(length) =>
        val msg = s"Data at $context must have length greater than $length"
        val propertyValidation = new PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_minLength/prop",
          message = Some(msg),
          minLength = Some(s"$length")
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMaxLength(context: String, validation: ValidationSpecification, shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.fields.?[AmfScalar](ScalarShapeModel.MaxLength) match {
      case Some(length) =>
        val msg = s"Data at $context must have length smaller than $length"
        val propertyValidation = new PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_maxLength/prop",
          message = Some(msg),
          maxLength = Some(s"$length")
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMinimum(context: String, validation: ValidationSpecification, shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.fields.?[AmfScalar](ScalarShapeModel.Minimum) match {
      case Some(minimum) =>
        val msg = s"Data at $context must be greater than or equal to $minimum"
        val propertyValidation = new PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_minimum/prop",
          message = Some(msg),
          minInclusive = Some(s"$minimum")
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMaximum(context: String, validation: ValidationSpecification, shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.fields.?[AmfScalar](ScalarShapeModel.Maximum) match {
      case Some(maximum) =>
        val msg = s"Data at $context must be smaller than or equal to $maximum"
        val propertyValidation = new PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_maximum/prop",
          message = Some(msg),
          maxInclusive = Some(s"$maximum")
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMinimumExclusive(context: String, validation: ValidationSpecification, shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.fields.?[AmfScalar](ScalarShapeModel.ExclusiveMinimum) match {
      case Some(exclusiveMinimum) =>
        val msg = s"Data at $context must be greater than $exclusiveMinimum"
        val propertyValidation = new PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_exclusiveMinimum/prop",
          message = Some(msg),
          minExclusive = Some(s"$exclusiveMinimum")
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMaximumExclusive(context: String, validation: ValidationSpecification, shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.fields.?[AmfScalar](ScalarShapeModel.ExclusiveMaximum) match {
      case Some(exclusiveMaximum) =>
        val msg = s"Data at $context must be smaller than $exclusiveMaximum"
        val propertyValidation = new PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_exclusiveMaximum/prop",
          message = Some(msg),
          maxInclusive = Some(s"$exclusiveMaximum")
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

}
