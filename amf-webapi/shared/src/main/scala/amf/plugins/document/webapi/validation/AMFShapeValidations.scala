package amf.plugins.document.webapi.validation

import java.net.URISyntaxException

import amf.ProfileNames.PAYLOAD
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain._
import amf.core.rdf.RdfModel
import amf.core.utils.Strings
import amf.core.validation.core._
import amf.core.vocabulary.Namespace
import amf.plugins.domain.shapes.metamodel.{ArrayShapeModel, NodeShapeModel, ScalarShapeModel, UnionShapeModel}
import amf.plugins.domain.shapes.models.TypeDef.NumberType
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.parser.TypeDefXsdMapping
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable

class AMFShapeValidations(root: Shape) {

  var emitMultipleOf: Boolean                           = false
  var currentDataNode: Option[DataNode]                 = None
  var polymorphicExpanded: mutable.Map[String, Boolean] = mutable.Map()

  def profile(dataNode: DataNode): ValidationProfile = {
    currentDataNode = Some(dataNode)
    val parsedValidations = validations() ++ customFunctionValidations()
    ValidationProfile(
      name = PAYLOAD,
      baseProfile = Some(PAYLOAD),
      violationLevel = parsedValidations.map(_.name),
      validations = parsedValidations ++ ParserSideValidations.validations
    )
  }

  protected def validations(): List[ValidationSpecification] = emitShapeValidations("/", root)

  protected def customFunctionValidations(): Seq[ValidationSpecification] = {
    var acc: Seq[ValidationSpecification] = Nil
    if (emitMultipleOf) {
      val msg = "Property must validate the 'multipleOf' constraint"
      acc = acc ++ Seq(
        ValidationSpecification(
          name = (Namespace.Shapes + "multipleOfValidationParamSpecification").iri(),
          message = msg,
          ramlMessage = Some(msg),
          oasMessage = Some(msg),
          functionConstraint = Some(
            FunctionConstraint(
              message = Some(msg),
              code = Some(
                """|function(shape, value, multipleOf) {
                 |  if (value != null && multipleOf != null) {
                 |    return (amfExtractLiteral(value) % amfExtractLiteral(multipleOf)) === 0;
                 |  } else {
                 |    return false;
                 |  }
                 |}
              """.stripMargin
              ),
              parameters = Seq(
                FunctionConstraintParameter(
                  (Namespace.Shapes + "multipleOfValidationParam").iri(),
                  (Namespace.Xsd + "integer").iri()
                )
              )
            ))
        )
      )
    }

    acc
  }

  def polymorphic(obj: NodeShape) = {
    obj.supportsInheritance && !polymorphicExpanded.getOrElse(obj.id, false)
  }

  protected def emitShapeValidations(context: String, shape: Shape): List[ValidationSpecification] = {
    shape match {
      case union: UnionShape                   => unionConstraints(context, union)
      case scalar: ScalarShape                 => scalarConstraints(context, scalar)
      case tuple: TupleShape                   => tupleConstraints(context, tuple)
      case array: ArrayShape                   => arrayConstraints(context, array)
      case obj: NodeShape if !polymorphic(obj) => nodeConstraints(context, obj)
      case obj: NodeShape if polymorphic(obj)  => polymorphicNodeConstraints(context, obj)
      case nil: NilShape                       => nilConstraints(context, nil)
      case recur: RecursiveShape               => recursiveShapeConstraints(context, recur)
      case any: AnyShape                       => anyConstraints(context, any)
      case _                                   => List.empty
    }
  }

  def validationId(shape: Shape): String = {
    shape match {
      case rec: RecursiveShape if rec.fixpoint.option().isDefined =>
        validationLiteralId(root.id)
      case _ =>
        validationLiteralId(shape.id)
    }
  }

  def validationLiteralId(id: String): String = {
    val name = id + "_validation"
    if (name.startsWith("http://") || name.startsWith("https://") || name.startsWith("file:")) {
      name
    } else if (name.contains("#")) {
      try {
        name.normalizeUrl.normalizePath
      } catch {
        case e: URISyntaxException => (Namespace.Data + "default_for_invalid_uri").iri()
      }
    } else {
      (Namespace.Data + name).iri()
    }
  }

//  protected def canonicalShape(): Shape = CanonicalShapePipeline(shape)

  protected def checkLogicalConstraints(context: String,
                                        parent: Shape,
                                        validation: ValidationSpecification,
                                        acc: List[ValidationSpecification]): List[ValidationSpecification] = {
    var computedValidation = validation
    var nestedConstraints  = acc
    var count              = 0

    if (Option(parent.and).isDefined && parent.and.nonEmpty) {
      parent.and.foreach { shape =>
        nestedConstraints ++= emitShapeValidations(context + s"/and_$count", shape)
        count += 1
      }

      computedValidation = computedValidation.copy(andConstraints = parent.and.map(s => validationId(s)))
    }

    count = 0
    if (Option(parent.or).isDefined && parent.or.nonEmpty) {
      parent.or.foreach { shape =>
        nestedConstraints ++= emitShapeValidations(context + s"/or_$count", shape)
        count += 1
      }

      computedValidation = computedValidation.copy(unionConstraints = parent.or.map(s => validationId(s)))
    }

    count = 0
    if (Option(parent.xone).isDefined && parent.xone.nonEmpty) {
      parent.xone.foreach { shape =>
        nestedConstraints ++= emitShapeValidations(context + s"/xone_$count", shape)
        count += 1
      }

      computedValidation = computedValidation.copy(xoneConstraints = parent.xone.map(s => validationId(s)))
    }

    if (Option(parent.not).isDefined) {
      nestedConstraints ++= emitShapeValidations(context + "/not", parent.not)
      computedValidation = computedValidation.copy(notConstraint = Some(validationId(parent.not)))
    }
    List(computedValidation) ++ nestedConstraints
  }

  protected def anyConstraints(context: String, any: AnyShape): List[ValidationSpecification] = {
    val msg = s"Data at $context must be a valid shape"

    val validation = new ValidationSpecification(
      name = validationId(any),
      message = msg,
      ramlMessage = Some(msg),
      oasMessage = Some(msg)
    )

    checkLogicalConstraints(context, any, validation, Nil)
  }

  def isPolymorphicUnion(union: UnionShape): Boolean = {
    union.anyOf.foldLeft(true) {
      case (acc, shape) =>
        acc && shape.isInstanceOf[AnyShape] && shape.asInstanceOf[AnyShape].supportsInheritance
    }
  }

  protected def unionConstraints(context: String, union: UnionShape): List[ValidationSpecification] = {
    val msg = if (union.isPolymorphicUnion) {
      s"Data at $context must be a valid polymorphic type: ${union.anyOf.map(_.name.option().getOrElse("type").urlDecoded).distinct.mkString(", ")}"
    } else {
      s"Data at $context must be one of the valid union types: ${union.anyOf.map(_.name.option().getOrElse("type").urlDecoded).distinct.mkString(", ")}"
    }
    var nestedConstraints: List[ValidationSpecification] = List.empty
    var count                                            = 0
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
    checkLogicalConstraints(context, union, validation, nestedConstraints)
  }

  protected def arrayConstraints(context: String, array: ArrayShape): List[ValidationSpecification] = {
    val msg                                              = s"Array at $context must be valid"
    var nestedConstraints: List[ValidationSpecification] = List.empty
    var validation = new ValidationSpecification(
      name = validationId(array),
      message = msg,
      ramlMessage = Some(msg),
      oasMessage = Some(msg),
      targetClass = Seq.empty,
      propertyConstraints = Seq()
    )

    if (array.fields.entry(ArrayShapeModel.Items).isDefined) {
      nestedConstraints ++= emitShapeValidations(context + "/items", array.items)

      val itemsValidationId = validationId(array) + "/prop"
      val itemsConstraint = PropertyConstraint(
        ramlPropertyId = (Namespace.Rdf + "member").iri(),
        name = itemsValidationId,
        message = Some(s"Array items at $context must be valid"),
        node = Some(validationId(array.items))
      )

      validation = validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(itemsConstraint))
    }

    validation = checkMinItems(context, validation, array)
    validation = checkMaxItems(context, validation, array)
    validation = checkArrayType(array, context, validation)

    checkLogicalConstraints(context, array, validation, nestedConstraints)
  }

  protected def tupleConstraints(context: String, tuple: TupleShape): List[ValidationSpecification] = {
    val msg                                              = s"Tuple at $context must be valid"
    var nestedConstraints: List[ValidationSpecification] = List.empty
    var validation = new ValidationSpecification(
      name = validationId(tuple),
      message = msg,
      ramlMessage = Some(msg),
      oasMessage = Some(msg),
      targetClass = Seq.empty,
      propertyConstraints = Seq()
    )

    val itemsConstraints = tuple.items.zipWithIndex.map {
      case (item, i) =>
        nestedConstraints ++= emitShapeValidations(context + s"/items/", item)
        val itemsValidationId = validationId(item) + "/prop"
        PropertyConstraint(
          ramlPropertyId = (Namespace.Data + s"pos$i").iri(),
          name = itemsValidationId,
          message = Some(s"Tupe items at $context/items pos $i must be valid"),
          node = Some(validationId(item))
        )
    }

    validation = validation.copy(propertyConstraints = validation.propertyConstraints ++ itemsConstraints)
    validation = checkMinItems(context, validation, tuple)
    validation = checkMaxItems(context, validation, tuple)
    validation = checkArrayType(tuple, context, validation)

    checkLogicalConstraints(context, tuple, validation, nestedConstraints)
  }

  protected def recursiveShapeConstraints(context: String, shape: RecursiveShape): List[ValidationSpecification] = {
    Nil
  }

  protected def nodeConstraints(context: String, node: NodeShape): List[ValidationSpecification] = {
    val msg                                              = s"Object at $context must be valid"
    var nestedConstraints: List[ValidationSpecification] = List.empty
    var validation = new ValidationSpecification(
      name = validationId(node),
      message = msg,
      ramlMessage = Some(msg),
      oasMessage = Some(msg),
      targetClass = Seq.empty,
      propertyConstraints = Seq()
    )

    // Property shape generated from the discriminator definition
    val discriminatorProperty = node.discriminator.option() match {
      case None => Seq()
      case Some(discriminator) => {
        val discriminatorValueRegex = node.discriminatorValue.option() match {
          case None    => node.name.value()
          case Some(v) => v
        }
        Seq(
          PropertyShape()
            .withId(node.id + "_discriminator")
            .withName(discriminator.urlComponentEncoded)
            .withMinCount(1)
            .withRange(
              ScalarShape()
                .withId(node.id + "_discriminator_value")
                .withDataType((Namespace.Xsd + "string").iri())
                .withPattern("^" + discriminatorValueRegex + "$")
            )
        )
      }
    }

    (node.properties ++ discriminatorProperty).foreach { property =>
      val patternedProperty = property.patternName.option()
      val encodedName       = property.name.value().urlComponentEncoded
      nestedConstraints ++= emitShapeValidations(context + s"/$encodedName", property.range)

      val propertyValidationId = validationId(property.range)
      val propertyId           = (Namespace.Data + encodedName).iri()
      val nodeConstraint = PropertyConstraint(
        ramlPropertyId = propertyId,
        name = validationId(node) + s"_validation_node_prop_${property.name.value()}",
        message = Some(s"Property ${property.name.value()} at $context must have a valid value"),
        node = Some(propertyValidationId),
        patternedProperty = patternedProperty
      )
      validation = validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(nodeConstraint))
      if (patternedProperty.isEmpty) { // TODO: Should we emit this for pattern properties?
        validation = checkMinCount(context + s"/$encodedName", property, validation, property, patternedProperty)
        validation = checkMaxCount(context + s"/$encodedName", property, validation, property, patternedProperty)
      }
    }

    // Additional properties coming from JSON Schema
    node.fields.entry(NodeShapeModel.AdditionalPropertiesSchema) match {
      case Some(f) =>
        val encodedName = "json_schema_additional_property"
        val range       = f.value.value.asInstanceOf[Shape]
        nestedConstraints ++= emitShapeValidations(context + s"/$encodedName", range)

        val propertyValidationId = validationId(range)
        val propertyId           = (Namespace.Data + encodedName).iri()
        val nodeConstraint = PropertyConstraint(
          ramlPropertyId = propertyId,
          name = validationId(node) + s"_validation_node_prop_$encodedName",
          message = Some(s"Property $encodedName at $context must have a valid value"),
          node = Some(propertyValidationId),
          patternedProperty = Some("^.*$")
        )
        validation = validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(nodeConstraint))
      case _ => // ignore
    }

    // Validation to allow to emit the properties number in the model graph
    validation = validation.copy(
      propertyConstraints = validation.propertyConstraints ++ Seq(
        PropertyConstraint(
          ramlPropertyId = Namespace.AmfValidation.base + "/properties",
          name = validationId(node) + s"_validation_node_prop_properties",
          message = Some(s"Property /properties at $context must have a valid value"),
          node = Some(Namespace.AmfValidation.base + "/properties")
        )
      ))

    validation = checkClosed(validation, node)
    validation = checkObjectType(node, context, validation)
    validation = checkMinProperties(context, validation, node)
    validation = checkMaxProperties(context, validation, node)
    checkLogicalConstraints(context, node, validation, nestedConstraints)
  }

  def polymorphicNodeConstraints(context: String, obj: NodeShape): List[ValidationSpecification] = {
    val closure: Seq[Shape] = obj.effectiveStructuralShapes
    if (currentDataNode.isDefined && context == "/") {
      // we try to find a matching shape without using shacl
      findPolymorphicEffectiveShape(closure) match {
        case Some(shape: NodeShape) =>
          nodeConstraints(context, shape)
        case _ =>
          closure.map { s =>
            polymorphicExpanded.put(s.id, true)
            s
          }
          val polymorphicUnion = UnionShape().withId(obj.id + "_polymorphic")
          polymorphicUnion.setArrayWithoutId(UnionShapeModel.AnyOf, closure)
          emitShapeValidations(context, polymorphicUnion)
      }
    } else {
      closure.map { s =>
        polymorphicExpanded.put(s.id, true)
        s
      }
      val polymorphicUnion = UnionShape().withId(obj.id + "_polymorphic")
      polymorphicUnion.setArrayWithoutId(UnionShapeModel.AnyOf, closure)
      emitShapeValidations(context, polymorphicUnion)
    }
  }

  def findPolymorphicEffectiveShape(polymorphicUnion: Seq[Shape]): Option[Shape] = {
    polymorphicUnion.filter(_.isInstanceOf[NodeShape]).find {
      case nodeShape: NodeShape =>
        nodeShape.discriminator.option() match {
          case Some(discriminatorProp) =>
            val discriminatorValue = nodeShape.discriminatorValue.option().getOrElse(nodeShape.name.value())
            currentDataNode match {
              case Some(obj: ObjectNode) =>
                obj.properties.get(discriminatorProp) match {
                  case Some(v: ScalarNode) => {
                    v.value == discriminatorValue
                  }
                  case _ => false
                }
              case _ => false
            }
          case None => false
        }
    }
  }

  protected def checkClosed(validation: ValidationSpecification, shape: NodeShape): ValidationSpecification = {
    shape.fields.?[AmfScalar](NodeShapeModel.Closed) match {
      case Some(value) if value.toBool => validation.copy(closed = Some(true))
      case _                           => validation
    }
  }

  protected def nilConstraints(context: String, nil: NilShape): List[ValidationSpecification] = {
    val msg = s"Property at $context must be null"
    var validation = new ValidationSpecification(
      name = validationId(nil),
      message = msg,
      ramlMessage = Some(msg),
      oasMessage = Some(msg),
      targetClass = Seq.empty,
      propertyConstraints = Seq(
        PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = nil.id + "_validation_range/prop",
          message = Some(msg),
          datatype = Some((Namespace.Xsd + "nil").iri())
        )
      )
    )
    List(validation)
  }

  protected def scalarConstraints(context: String, scalar: ScalarShape): List[ValidationSpecification] = {

    val propertyConstraints = scalar.dataType.value() match {
      case s if s == (Namespace.Xsd + "date").iri() =>
        val rfc3339DateRegex = "([0-9]+)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])"
        // OAS 2.0 and RAML 1.0 date type uses notation of RFC3339. RAML 0.8 has not "date" type (the date type of RAML 0.8 is noted as dateTime in amf model)
        val msg = s"Scalar at $context must be valid RFC3339 date"
        val dateDataTypes = List(
          (Namespace.Xsd + "string").iri().trim,
          (Namespace.Xsd + "date").iri().trim
        )

        val patternConstraint  = createPatternPropertyConstraint(msg, rfc3339DateRegex, scalar)
        val dataTypeConstraint = createDataTypeConstraint(scalar, context, dateDataTypes)
        Seq(patternConstraint, dataTypeConstraint)

      case s if s == (Namespace.Xsd + "time").iri() =>
        val timeRegex = "([0-9]{2}:[0-9]{2}:[0-9]{2}){1}(\\.\\d*)?"
        val msg       = s"Scalar at $context must be valid RFC3339 time"
        val timeDataTypes = List(
          (Namespace.Xsd + "string").iri().trim,
          (Namespace.Xsd + "time").iri().trim
        )

        val patternConstraint  = createPatternPropertyConstraint(msg, timeRegex, scalar)
        val dataTypeConstraint = createDataTypeConstraint(scalar, context, timeDataTypes)
        Seq(patternConstraint, dataTypeConstraint)

      case s if s == (Namespace.Shapes + "dateTimeOnly").iri() =>
        val rfc3339DateTimeOnlyRegex =
          "([0-9]+)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])[Tt]([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]|60)(\\.[0-9]+)?"
        // OAS 2.0 and RAML 1.0 dateTimeOnly type uses notation of RFC3339. RAML 0.8 has not "dateTimeOnly" type.
        val msg = s"Scalar at $context must be valid RFC3339 date"
        val dateTimeOnlyDataTypes = List(
          (Namespace.Xsd + "string").iri().trim,
          (Namespace.Shapes + "dateTimeOnly").iri().trim
        )

        val patternConstraint  = createPatternPropertyConstraint(msg, rfc3339DateTimeOnlyRegex, scalar)
        val dataTypeConstraint = createDataTypeConstraint(scalar, context, dateTimeOnlyDataTypes)
        Seq(patternConstraint, dataTypeConstraint)

      case s if s == (Namespace.Xsd + "dateTime").iri() =>
        val rfc3339 =
          "([0-9]+)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])[Tt]([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]|60)(\\.[0-9]+)?(([Zz])|([\\+|\\-]([01][0-9]|2[0-3]):[0-5][0-9]))"
        val rfc2616 =
          "((Mon|Tue|Wed|Thu|Fri|Sat|Sun), [0-9]{2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) [0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2} GMT)"
        val dateTimeDataTypes = List(
          (Namespace.Xsd + "string").iri().trim,
          (Namespace.Xsd + "dateTime").iri().trim
        )

        scalar.format.option().map(_.toLowerCase()) match {
          case Some(f) if f == "rfc2616" =>
            // RAML 0.8 date type following RFC2616 (default for this spec)
            val msg                = s"Scalar at $context must be valid RFC2616 date"
            val patternConstraint  = createPatternPropertyConstraint(msg, rfc2616, scalar)
            val dataTypeConstraint = createDataTypeConstraint(scalar, context, dateTimeDataTypes)
            Seq(patternConstraint, dataTypeConstraint)

          case _ => // If format is not RFC2616, it is RFC3339 (default for RAML 1.0 and OAS 2.0)
            val msg                = s"Scalar at $context must be valid RFC3339 date"
            val patternConstraint  = createPatternPropertyConstraint(msg, rfc3339, scalar)
            val dataTypeConstraint = createDataTypeConstraint(scalar, context, dateTimeDataTypes)
            Seq(patternConstraint, dataTypeConstraint)
        }

      case s if s == (Namespace.Xsd + "string").iri() =>
        val stringDataTypes = List(
          (Namespace.Xsd + "string").iri().trim,
          (Namespace.Xsd + "time").iri().trim,
          (Namespace.Xsd + "date").iri().trim,
          (Namespace.Xsd + "dateTime").iri().trim,
          (Namespace.Shapes + "dateTimeOnly").iri().trim
        )
        Seq(createDataTypeConstraint(scalar, context, stringDataTypes))

      case s if s == (Namespace.Shapes + "number").iri() =>
        val numberDataTypes = List(
          (Namespace.Xsd + "integer").iri().trim,
          (Namespace.Xsd + "long").iri().trim,
          (Namespace.Xsd + "float").iri().trim,
          (Namespace.Xsd + "double").iri().trim
        )
        Seq(createDataTypeConstraint(scalar, context, numberDataTypes))

      case s if s == (Namespace.Xsd + "float").iri() || s == (Namespace.Xsd + "double").iri() =>
        val floatDataTypes = List(
          (Namespace.Xsd + "float").iri().trim,
          (Namespace.Xsd + "double").iri().trim,
          (Namespace.Xsd + "integer").iri().trim,
          (Namespace.Xsd + "long").iri().trim
        )
        Seq(createDataTypeConstraint(scalar, context, floatDataTypes))

      case _ =>
        Seq(
          PropertyConstraint(
            ramlPropertyId = (Namespace.Data + "value").iri(),
            name = scalar.id + "_validation_range/prop",
            message = Some(s"Scalar at $context must have data type ${scalar.dataType.value()}"),
            datatype = Some(scalar.dataType.value())
          )
        )
    }

    val msg = s"Scalar at $context must be valid"

    var validation = new ValidationSpecification(
      name = validationId(scalar),
      message = msg,
      ramlMessage = Some(msg),
      oasMessage = Some(msg),
      targetClass = Seq.empty,
      propertyConstraints = propertyConstraints
    )
    validation = checkScalarType(scalar, context, validation)
    validation = checkPattern(context, validation, scalar)
    validation = checkMinLength(context, validation, scalar)
    validation = checkMaxLength(context, validation, scalar)
    validation = checkMinimum(context, validation, scalar)
    validation = checkMaximum(context, validation, scalar)
    validation = checkMultipleOf(context, validation, scalar)
    validation = checkEnum(context, validation, scalar)

    checkLogicalConstraints(context, scalar, validation, Nil)

  }

  protected def createPatternPropertyConstraint(msg: String, pattern: String, scalar: Shape): PropertyConstraint = {
    PropertyConstraint(
      ramlPropertyId = (Namespace.Data + "value").iri(),
      name = scalar.id + "_validation_range/prop",
      message = Some(msg),
      pattern = Some(pattern)
    )
  }

  protected def createDataTypeConstraint(scalar: ScalarShape,
                                         context: String,
                                         datatypes: List[String]): PropertyConstraint = {

    val shaclOrNamespace       = (Namespace.Shacl + "or").iri()
    val shaclDatatypeNamespace = (Namespace.Shacl + "datatype").iri()

    val custom = Some((b: EntryBuilder, parentId: String) => {
      b.entry(
        shaclOrNamespace,
        _.obj(
          _.entry(
            "@list",
            _.list { l =>
              datatypes.foreach { dt =>
                l.obj { v =>
                  v.entry(shaclDatatypeNamespace, _.obj(_.entry("@id", dt)))
                }
              }
            }
          )
        )
      )
    })

    val customRdf = Some((rdfModel: RdfModel, subject: String) => {
      val propId  = rdfModel.nextAnonId()
      var counter = 0
      datatypes.foreach {
        dt =>
          counter = counter + 1
          val constraintListId = propId + "_ointdoub" + counter
          if (counter == 1) rdfModel.addTriple(subject, shaclOrNamespace, constraintListId)
          rdfModel.addTriple(constraintListId, (Namespace.Rdf + "first").iri(), constraintListId + "_v")
          rdfModel.addTriple(constraintListId + "_v", shaclDatatypeNamespace, dt)
          if (counter < datatypes.size) {
            val nextConstraintListId = propId + "_ointdoub" + (counter + 1)
            rdfModel.addTriple(constraintListId, (Namespace.Rdf + "rest").iri(), nextConstraintListId)
          } else rdfModel.addTriple(constraintListId, (Namespace.Rdf + "rest").iri(), (Namespace.Rdf + "nil").iri())
      }
    })

    PropertyConstraint(
      ramlPropertyId = (Namespace.Data + "value").iri(),
      name = scalar.id + "_validation_range/prop",
      message = Some(s"Scalar at $context must have data type ${scalar.dataType.value()}"),
      custom = custom,
      customRdf = customRdf
    )
  }

  protected def checkScalarType(shape: Shape,
                                context: String,
                                validation: ValidationSpecification): ValidationSpecification = {
    val msg = s"Data at $context must be a scalar"
    val propertyValidation = PropertyConstraint(
      ramlPropertyId = (Namespace.Rdf + "type").iri(),
      name = validation.name + "_validation_type/prop",
      message = Some(msg),
      in = Seq((Namespace.Data + "Scalar").iri())
    )
    validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
  }

  protected def checkObjectType(shape: Shape,
                                context: String,
                                validation: ValidationSpecification): ValidationSpecification = {
    val msg = s"Data at $context must be an object"
    val propertyValidation = PropertyConstraint(
      ramlPropertyId = (Namespace.Rdf + "type").iri(),
      name = validation.name + "_validation_type/prop",
      message = Some(msg),
      in = Seq((Namespace.Data + "Object").iri())
    )
    validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
  }

  protected def checkArrayType(shape: Shape,
                               context: String,
                               validation: ValidationSpecification): ValidationSpecification = {
    val msg = s"Data at $context must be an array"
    val propertyValidation = PropertyConstraint(
      ramlPropertyId = (Namespace.Rdf + "type").iri(),
      name = validation.name + "_validation_type/prop",
      message = Some(msg),
      in = Seq((Namespace.Rdf + "Seq").iri(), (Namespace.Data + "Array").iri())
    )
    validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
  }

  protected def checkMinCount(context: String,
                              property: PropertyShape,
                              validation: ValidationSpecification,
                              shape: PropertyShape,
                              patterned: Option[String]): ValidationSpecification = {
    shape.fields.?[AmfScalar](PropertyShapeModel.MinCount) match {
      case Some(minCount) if minCount.toNumber.intValue() > 0 =>
        val msg = s"Data at $context must have min. cardinality $minCount"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Data + shape.name.value().urlComponentEncoded).iri(),
          name = validation.name + "_" + property.name.value().urlComponentEncoded + "_validation_minCount/prop",
          message = Some(msg),
          minCount = Some(s"$minCount"),
          datatype = effectiveDataType(shape),
          patternedProperty = patterned
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case _ => validation
    }
  }

  protected def checkMaxCount(context: String,
                              property: PropertyShape,
                              validation: ValidationSpecification,
                              shape: PropertyShape,
                              patterned: Option[String]): ValidationSpecification = {
    shape.fields.?[AmfScalar](PropertyShapeModel.MaxCount) match {
      case Some(maxCount) =>
        val msg = s"Data at $context must have max. cardinality $maxCount"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Data + shape.name.value().urlComponentEncoded).iri(),
          name = validation.name + "_" + property.name.value().urlComponentEncoded + "_validation_minCount/prop",
          message = Some(msg),
          maxCount = Some(s"$maxCount"),
          datatype = effectiveDataType(shape),
          patternedProperty = patterned
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkPattern(context: String,
                             validation: ValidationSpecification,
                             shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.pattern.option() match {
      case Some(pattern) =>
        val msg = s"Data at $context must match pattern $pattern"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_pattern/prop",
          message = Some(msg),
          pattern = Some(pattern)
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMinLength(context: String,
                               validation: ValidationSpecification,
                               shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.fields.?[AmfScalar](ScalarShapeModel.MinLength) match {
      case Some(length) =>
        val msg = s"Data at $context must have length greater than $length"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_minLength/prop",
          message = Some(msg),
          minLength = Some(s"$length")
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMaxLength(context: String,
                               validation: ValidationSpecification,
                               shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.fields.?[AmfScalar](ScalarShapeModel.MaxLength) match {
      case Some(length) =>
        val msg = s"Data at $context must have length smaller than $length"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_maxLength/prop",
          message = Some(msg),
          maxLength = Some(s"$length")
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMinimum(context: String,
                             validation: ValidationSpecification,
                             shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.fields.?[AmfScalar](ScalarShapeModel.Minimum) match {
      case Some(minimum) =>
        val propertyValidation = shape.fields.?[AmfScalar](ScalarShapeModel.ExclusiveMinimum) match {
          case Some(exclusiveMinimum) if exclusiveMinimum.toBool =>
            val msg = s"Data at $context must be greater than to $minimum"
            PropertyConstraint(
              ramlPropertyId = (Namespace.Data + "value").iri(),
              name = validation.name + "_validation_minimum/prop",
              message = Some(msg),
              minExclusive = Some(s"$minimum"),
              datatype = effectiveDataType(shape)
            )
          case _ =>
            val msg = s"Data at $context must be greater than or equal to $minimum"
            PropertyConstraint(
              ramlPropertyId = (Namespace.Data + "value").iri(),
              name = validation.name + "_validation_minimum/prop",
              message = Some(msg),
              minInclusive = Some(s"$minimum"),
              datatype = effectiveDataType(shape)
            )
        }
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMaximum(context: String,
                             validation: ValidationSpecification,
                             shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.fields.?[AmfScalar](ScalarShapeModel.Maximum) match {
      case Some(maximum) =>
        val propertyValidation = shape.fields.?[AmfScalar](ScalarShapeModel.ExclusiveMaximum) match {
          case Some(exclusiveMaximum) if exclusiveMaximum.toBool =>
            val msg = s"Data at $context must be smaller than to $maximum"
            PropertyConstraint(
              ramlPropertyId = (Namespace.Data + "value").iri(),
              name = validation.name + "_validation_maximum/prop",
              message = Some(msg),
              maxExclusive = Some(s"$maximum"),
              datatype = effectiveDataType(shape)
            )
          case _ =>
            val msg = s"Data at $context must be smaller than or equal to $maximum"
            PropertyConstraint(
              ramlPropertyId = (Namespace.Data + "value").iri(),
              name = validation.name + "_validation_maximum/prop",
              message = Some(msg),
              maxInclusive = Some(s"$maximum"),
              datatype = effectiveDataType(shape)
            )
        }
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMultipleOf(context: String,
                                validation: ValidationSpecification,
                                shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.fields.?[AmfScalar](ScalarShapeModel.MultipleOf) match {
      case Some(multOf) =>
        emitMultipleOf = true
        val msg = s"Data at is not a multipleOf '${multOf.toString}'"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_multipleOf/prop",
          message = Some(msg),
          multipleOf = Some(multOf.toString)
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMinProperties(context: String,
                                   validation: ValidationSpecification,
                                   shape: Shape): ValidationSpecification = {
    shape.fields.?[AmfScalar](NodeShapeModel.MinProperties) match {
      case Some(minProperties) =>
        val msg = s"Expected min properties $minProperties"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = Namespace.AmfValidation.base + "/properties",
          name = validation.name + "_validation_minProperties/prop",
          message = Some(msg),
          minInclusive = Some(s"$minProperties"),
          datatype = effectiveDataType(shape)
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMaxProperties(context: String,
                                   validation: ValidationSpecification,
                                   shape: Shape): ValidationSpecification = {
    shape.fields.?[AmfScalar](NodeShapeModel.MaxProperties) match {
      case Some(maxProperties) =>
        val msg = s"Expected max properties $maxProperties"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = Namespace.AmfValidation.base + "/properties",
          name = validation.name + "_validation_maxProperties/prop",
          message = Some(msg),
          maxInclusive = Some(s"$maxProperties"),
          datatype = effectiveDataType(shape)
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMinItems(context: String,
                              validation: ValidationSpecification,
                              shape: Shape with DataArrangementShape): ValidationSpecification = {
    shape.fields.?[AmfScalar](ArrayShapeModel.MinItems) match {
      case Some(itemsMinimum) =>
        val msg = s"Number of items at $context must be greater than $itemsMinimum"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Rdf + "member").iri(),
          name = validation.name + "_validation_minItems/prop",
          message = Some(msg),
          minCount = Some(s"$itemsMinimum"),
          datatype = effectiveDataType(shape)
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMaxItems(context: String,
                              validation: ValidationSpecification,
                              shape: Shape with DataArrangementShape): ValidationSpecification = {
    shape.fields.?[AmfScalar](ArrayShapeModel.MaxItems) match {
      case Some(itemsMaximum) =>
        val msg = s"Number of items at $context must be smaller than $itemsMaximum"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Rdf + "member").iri(),
          name = validation.name + "_validation_maxItems/prop",
          message = Some(msg),
          maxCount = Some(s"$itemsMaximum"),
          datatype = effectiveDataType(shape)
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkEnum(context: String,
                          validation: ValidationSpecification,
                          shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.fields.?[AmfArray](ScalarShapeModel.Values) match {
      case Some(valuesArray) =>
        // When the shape is of type string, force the enum values to be of string type too.
        val dataType = root.fields
          .?[AmfScalar](ScalarShapeModel.DataType)
          .map(_.toString)
          .filter(_ == "http://www.w3.org/2001/XMLSchema#string")
        val values = valuesArray.scalars.map(_.toString)
        val msg    = s"Data at $context must be within the values (${values.mkString(",")})"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_enum/prop",
          message = Some(msg),
          in = values,
          datatype = dataType
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def effectiveDataType(scalar: Shape): Option[String] = {
    root.fields.?[AmfScalar](ScalarShapeModel.DataType).map(_.toString) match {
      case Some(datatype) =>
        val format = root.fields.?[AmfScalar](ScalarShapeModel.Format).map(_.toString)
        TypeDefXsdMapping.typeDef(datatype, format.getOrElse("")) match {
          case NumberType =>
            Some((Namespace.Shapes + "number").iri()) // if this is a number, send our custom scalar type
          case _ => Some(datatype) // normal 1:1 mapping, we send the regular XSD type
        }
      case None => None // no XSD datatype

    }
  }
}
