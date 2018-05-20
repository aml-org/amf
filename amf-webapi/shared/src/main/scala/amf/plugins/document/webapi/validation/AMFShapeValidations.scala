package amf.plugins.document.webapi.validation

import java.net.URISyntaxException

import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{AmfArray, AmfScalar, RecursiveShape, Shape}
import amf.core.rdf.RdfModel
import amf.core.utils.Strings
import amf.core.validation.core.{PropertyConstraint, ValidationProfile, ValidationSpecification}
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.resolution.pipelines.CanonicalShapePipeline
import amf.plugins.domain.shapes.metamodel.{ArrayShapeModel, NodeShapeModel, ScalarShapeModel}
import amf.plugins.domain.shapes.models.TypeDef.NumberType
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.parser.TypeDefXsdMapping
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model.YDocument.EntryBuilder
class AMFShapeValidations(shape: Shape) {

  def profile(): ValidationProfile = {
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
      case union: UnionShape     => unionConstraints(context, union)
      case scalar: ScalarShape   => scalarConstraints(context, scalar)
      case tuple: TupleShape     => tupleConstraints(context, tuple)
      case array: ArrayShape     => arrayConstraints(context, array)
      case obj: NodeShape        => nodeConstraints(context, obj)
      case nil: NilShape         => nilConstraints(context, nil)
      case recur: RecursiveShape => recursiveShapeConstraints(context, recur)
      case any: AnyShape         => anyConstraints(context, any)
      case _                     => List.empty
    }
  }

  def validationId(shape: Shape): String = {
    shape match {
      case rec: RecursiveShape if rec.fixpoint.option().isDefined =>
        validationLiteralId(rec.fixpoint.value())
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

  protected def canonicalShape(): Shape = CanonicalShapePipeline(shape)

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

  protected def unionConstraints(context: String, union: UnionShape): List[ValidationSpecification] = {
    val msg                                              = s"Data at $context must be one of the valid union types"
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
    /*
    val msg                                              = s"Recursive object at $context must be valid"
    var nestedConstraints: List[ValidationSpecification] = List.empty
    var validation = new ValidationSpecification(
      name = validationLiteralId(shape.fixpoint.value()),
      message = "Recursive validation failure"
    )
    List(validation)
     */
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

    node.properties.foreach { property =>
      val encodedName = property.name.value().urlComponentEncoded
      nestedConstraints ++= emitShapeValidations(context + s"/$encodedName", property.range)

      val propertyValidationId = validationId(property.range)
      val propertyId           = (Namespace.Data + encodedName).iri()
      val nodeConstraint = PropertyConstraint(
        ramlPropertyId = propertyId,
        name = validationId(node) + s"_validation_node_prop_${property.name.value()}",
        message = Some(s"Property ${property.name.value()} at $context must have a valid value"),
        node = Some(propertyValidationId)
      )
      validation = validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(nodeConstraint))
      validation = checkMinCount(context + s"/$encodedName", property, validation, property)
      validation = checkMaxCount(context + s"/$encodedName", property, validation, property)
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
    if (scalar.format.option().isDefined && scalar.format
          .value()
          .toLowerCase() == "rfc2616" && scalar.dataType.value().endsWith("dateTime")) {
      // RAML 0.8 date type following RFC2616
      val msg = s"Scalar at $context must be valid RFC2616 date"
      var validation = new ValidationSpecification(
        name = validationId(scalar),
        message = msg,
        ramlMessage = Some(msg),
        oasMessage = Some(msg),
        targetClass = Seq.empty,
        propertyConstraints = Seq(
          PropertyConstraint(
            ramlPropertyId = (Namespace.Data + "value").iri(),
            name = scalar.id + "_validation_range/prop",
            message = Some(msg),
            pattern = Some(
              "((Mon|Tue|Wed|Thu|Fri|Sat|Sun), [0-9]{2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) [0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2} GMT)")
          ))
      )
      checkLogicalConstraints(context, scalar, validation, Nil)
    } else {
      val msg = s"Scalar at $context must be valid"

      var propertyConstraints = if (scalar.dataType.value() == (Namespace.Xsd + "string").iri()) {
        val custom = Some((b: EntryBuilder, parentId: String) => {
          b.entry(
            (Namespace.Shacl + "or").iri(),
            _.obj(
              _.entry(
                "@list",
                _.list {
                  l =>
                    l.obj { v =>
                      v.entry((Namespace.Shacl + "datatype").iri(),
                              _.obj(_.entry("@id", (Namespace.Xsd + "string").iri().trim)))
                    }
                    l.obj { v =>
                      v.entry((Namespace.Shacl + "datatype").iri(),
                              _.obj(_.entry("@id", (Namespace.Xsd + "time").iri().trim)))
                    }
                    l.obj { v =>
                      v.entry((Namespace.Shacl + "datatype").iri(),
                              _.obj(_.entry("@id", (Namespace.Xsd + "date").iri().trim)))
                    }
                    l.obj { v =>
                      v.entry((Namespace.Shacl + "datatype").iri(),
                              _.obj(_.entry("@id", (Namespace.Xsd + "dateTime").iri().trim)))
                    }
                    l.obj { v =>
                      v.entry((Namespace.Shacl + "datatype").iri(),
                              _.obj(_.entry("@id", (Namespace.Shapes + "dateTimeOnly").iri().trim)))
                    }
                }
              ))
          )
        })

        val customRdf = Some((rdfModel: RdfModel, subject: String) => {
          val propId = rdfModel.nextAnonId()
          val firstConstraintListId = propId + "_ointdoub1"
          val nextConstraintListId2 = propId + "_ointdoub2"
          val nextConstraintListId3 = propId + "_ointdoub3"
          val nextConstraintListId4 = propId + "_ointdoub4"
          val nextConstraintListId5 = propId + "_ointdoub5"

          rdfModel.addTriple(subject, (Namespace.Shacl + "or").iri(), firstConstraintListId)
          rdfModel.addTriple(firstConstraintListId, (Namespace.Rdf + "first").iri(), firstConstraintListId + "_v")
          rdfModel.addTriple(firstConstraintListId + "_v", (Namespace.Shacl + "datatype").iri(), (Namespace.Xsd + "string").iri().trim)
          rdfModel.addTriple(firstConstraintListId, (Namespace.Rdf + "rest").iri(), nextConstraintListId2)
          rdfModel.addTriple(nextConstraintListId2, (Namespace.Rdf + "first").iri(), nextConstraintListId2 + "_v")
          rdfModel.addTriple(nextConstraintListId2 + "_v", (Namespace.Shacl + "datatype").iri(), (Namespace.Xsd + "time").iri().trim)
          rdfModel.addTriple(nextConstraintListId2, (Namespace.Rdf + "rest").iri(), nextConstraintListId3)
          rdfModel.addTriple(nextConstraintListId3, (Namespace.Rdf + "first").iri(), nextConstraintListId3 + "_v")
          rdfModel.addTriple(nextConstraintListId3 + "_v", (Namespace.Shacl + "datatype").iri(), (Namespace.Xsd + "date").iri().trim)
          rdfModel.addTriple(nextConstraintListId3, (Namespace.Rdf + "rest").iri(), nextConstraintListId4)
          rdfModel.addTriple(nextConstraintListId4, (Namespace.Rdf + "first").iri(), nextConstraintListId4 + "_v")
          rdfModel.addTriple(nextConstraintListId4 + "_v", (Namespace.Shacl + "datatype").iri(), (Namespace.Xsd + "dateTime").iri().trim)
          rdfModel.addTriple(nextConstraintListId4, (Namespace.Rdf + "rest").iri(), nextConstraintListId5)
          rdfModel.addTriple(nextConstraintListId5, (Namespace.Rdf + "first").iri(), nextConstraintListId5 + "_v")
          rdfModel.addTriple(nextConstraintListId5 + "_v", (Namespace.Shacl + "datatype").iri(), (Namespace.Shapes + "dateTimeOnly").iri().trim)
          rdfModel.addTriple(nextConstraintListId5, (Namespace.Rdf + "rest").iri(), (Namespace.Rdf + "nil").iri())
        })

        Seq(
          PropertyConstraint(
            ramlPropertyId = (Namespace.Data + "value").iri(),
            name = scalar.id + "_validation_range/prop",
            message = Some(s"Scalar at $context must have data type ${scalar.dataType.value()}"),
            custom = custom,
            customRdf = customRdf
          ))
      } else if (scalar.dataType.value() == (Namespace.Shapes + "number").iri()) {
        val custom = Some((b: EntryBuilder, parentId: String) => {
          b.entry(
            (Namespace.Shacl + "or").iri(),
            _.obj(
              _.entry(
                "@list",
                _.list {
                  l =>
                    l.obj { v =>
                      v.entry((Namespace.Shacl + "datatype").iri(),
                              _.obj(_.entry("@id", (Namespace.Xsd + "integer").iri().trim)))
                    }
                    l.obj { v =>
                      v.entry((Namespace.Shacl + "datatype").iri(),
                              _.obj(_.entry("@id", (Namespace.Xsd + "long").iri().trim)))
                    }
                    l.obj { v =>
                      v.entry((Namespace.Shacl + "datatype").iri(),
                              _.obj(_.entry("@id", (Namespace.Xsd + "float").iri().trim)))
                    }
                    l.obj { v =>
                      v.entry((Namespace.Shacl + "datatype").iri(),
                              _.obj(_.entry("@id", (Namespace.Xsd + "double").iri().trim)))
                    }
                }
              ))
          )
        })

        val customRdf = Some((rdfModel: RdfModel, subject: String) => {
          val propId = rdfModel.nextAnonId()
          val firstConstraintListId = propId + "_ointdoub1"
          val nextConstraintListId2 = propId + "_ointdoub2"
          val nextConstraintListId3 = propId + "_ointdoub3"
          val nextConstraintListId4 = propId + "_ointdoub4"

          rdfModel.addTriple(subject, (Namespace.Shacl + "or").iri(), firstConstraintListId)
          rdfModel.addTriple(firstConstraintListId, (Namespace.Rdf + "first").iri(), firstConstraintListId + "_v")
          rdfModel.addTriple(firstConstraintListId + "_v", (Namespace.Shacl + "datatype").iri(), (Namespace.Xsd + "integer").iri().trim)
          rdfModel.addTriple(firstConstraintListId, (Namespace.Rdf + "rest").iri(), nextConstraintListId2)
          rdfModel.addTriple(nextConstraintListId2, (Namespace.Rdf + "first").iri(), nextConstraintListId2 + "_v")
          rdfModel.addTriple(nextConstraintListId2 + "_v", (Namespace.Shacl + "datatype").iri(), (Namespace.Xsd + "long").iri().trim)
          rdfModel.addTriple(nextConstraintListId2, (Namespace.Rdf + "rest").iri(), nextConstraintListId3)
          rdfModel.addTriple(nextConstraintListId3, (Namespace.Rdf + "first").iri(), nextConstraintListId3 + "_v")
          rdfModel.addTriple(nextConstraintListId3 + "_v", (Namespace.Shacl + "datatype").iri(), (Namespace.Xsd + "float").iri().trim)
          rdfModel.addTriple(nextConstraintListId3, (Namespace.Rdf + "rest").iri(), nextConstraintListId4)
          rdfModel.addTriple(nextConstraintListId4, (Namespace.Rdf + "first").iri(), nextConstraintListId4 + "_v")
          rdfModel.addTriple(nextConstraintListId4 + "_v", (Namespace.Shacl + "datatype").iri(), (Namespace.Xsd + "double").iri().trim)
          rdfModel.addTriple(nextConstraintListId4, (Namespace.Rdf + "rest").iri(), (Namespace.Rdf + "nil").iri())
        })

        Seq(
          PropertyConstraint(
            ramlPropertyId = (Namespace.Data + "value").iri(),
            name = scalar.id + "_validation_range/prop",
            message = Some(s"Scalar at $context must have data type ${scalar.dataType.value()}"),
            custom = custom,
            customRdf = customRdf
          ))
      } else if (scalar.dataType.value() == (Namespace.Xsd + "float")
                   .iri() || scalar.dataType.value() == (Namespace.Xsd + "double").iri()) {
        val custom = Some((b: EntryBuilder, parentId: String) => {
          b.entry(
            (Namespace.Shacl + "or").iri(),
            _.obj(
              _.entry(
                "@list",
                _.list { l =>
                  l.obj { v =>
                    v.entry((Namespace.Shacl + "datatype").iri(),
                            _.obj(_.entry("@id", (Namespace.Xsd + "float").iri().trim)))
                  }
                  l.obj { v =>
                    v.entry((Namespace.Shacl + "datatype").iri(),
                            _.obj(_.entry("@id", (Namespace.Xsd + "double").iri().trim)))
                  }
                }
              ))
          )
        })

        val customRdf = Some((rdfModel: RdfModel, subject: String) => {
          val propId = rdfModel.nextAnonId()
          val firstConstraintListId = propId + "_ointdoub1"
          val nextConstraintListId2 = propId + "_ointdoub2"

          rdfModel.addTriple(subject, (Namespace.Shacl + "or").iri(), firstConstraintListId)
          rdfModel.addTriple(firstConstraintListId, (Namespace.Rdf + "first").iri(), firstConstraintListId + "_v")
          rdfModel.addTriple(firstConstraintListId + "_v", (Namespace.Shacl + "datatype").iri(), (Namespace.Xsd + "float").iri().trim)
          rdfModel.addTriple(firstConstraintListId, (Namespace.Rdf + "rest").iri(), nextConstraintListId2)
          rdfModel.addTriple(nextConstraintListId2, (Namespace.Rdf + "first").iri(), nextConstraintListId2 + "_v")
          rdfModel.addTriple(nextConstraintListId2 + "_v", (Namespace.Shacl + "datatype").iri(), (Namespace.Xsd + "double").iri().trim)
          rdfModel.addTriple(nextConstraintListId2, (Namespace.Rdf + "rest").iri(), (Namespace.Rdf + "nil").iri())
        })

        Seq(
          PropertyConstraint(
            ramlPropertyId = (Namespace.Data + "value").iri(),
            name = scalar.id + "_validation_range/prop",
            message = Some(s"Scalar at $context must have data type ${scalar.dataType.value()}"),
            custom = custom,
            customRdf = customRdf
          ))
      } else {
        Seq(
          PropertyConstraint(
            ramlPropertyId = (Namespace.Data + "value").iri(),
            name = scalar.id + "_validation_range/prop",
            message = Some(s"Scalar at $context must have data type ${scalar.dataType.value()}"),
            datatype = Some(scalar.dataType.value())
          ))
      }

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
      validation = checkMinimumExclusive(context, validation, scalar)
      validation = checkMaximumExclusive(context, validation, scalar)
      validation = checkEnum(context, validation, scalar)

      checkLogicalConstraints(context, scalar, validation, Nil)
    }
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
                              shape: PropertyShape): ValidationSpecification = {
    shape.fields.?[AmfScalar](PropertyShapeModel.MinCount) match {
      case Some(minCount) if minCount.toNumber.intValue() > 0 =>
        val msg = s"Data at $context must have min. cardinality $minCount"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Data + shape.name.value().urlComponentEncoded).iri(),
          name = validation.name + "_" + property.name.value().urlComponentEncoded + "_validation_minCount/prop",
          message = Some(msg),
          minCount = Some(s"$minCount"),
          datatype = effectiveDataType(shape)
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case _ => validation
    }
  }

  protected def checkMaxCount(context: String,
                              property: PropertyShape,
                              validation: ValidationSpecification,
                              shape: PropertyShape): ValidationSpecification = {
    shape.fields.?[AmfScalar](PropertyShapeModel.MaxCount) match {
      case Some(maxCount) =>
        val msg = s"Data at $context must have max. cardinality $maxCount"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Data + shape.name.value().urlComponentEncoded).iri(),
          name = validation.name + "_" + property.name.value().urlComponentEncoded + "_validation_minCount/prop",
          message = Some(msg),
          maxCount = Some(s"$maxCount"),
          datatype = effectiveDataType(shape)
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
        val msg = s"Data at $context must be greater than or equal to $minimum"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_minimum/prop",
          message = Some(msg),
          minInclusive = Some(s"$minimum"),
          datatype = effectiveDataType(shape)
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMaximum(context: String,
                             validation: ValidationSpecification,
                             shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.fields.?[AmfScalar](ScalarShapeModel.Maximum) match {
      case Some(maximum) =>
        val msg = s"Data at $context must be smaller than or equal to $maximum"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_maximum/prop",
          message = Some(msg),
          maxInclusive = Some(s"$maximum"),
          datatype = effectiveDataType(shape)
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMinimumExclusive(context: String,
                                      validation: ValidationSpecification,
                                      shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.fields.?[AmfScalar](ScalarShapeModel.ExclusiveMinimum) match {
      case Some(exclusiveMinimum) =>
        val msg = s"Data at $context must be greater than $exclusiveMinimum"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_exclusiveMinimum/prop",
          message = Some(msg),
          minExclusive = Some(s"$exclusiveMinimum"),
          datatype = effectiveDataType(shape)
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def checkMaximumExclusive(context: String,
                                      validation: ValidationSpecification,
                                      shape: Shape with CommonShapeFields): ValidationSpecification = {
    shape.fields.?[AmfScalar](ScalarShapeModel.ExclusiveMaximum) match {
      case Some(exclusiveMaximum) =>
        val msg = s"Data at $context must be smaller than $exclusiveMaximum"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_exclusiveMaximum/prop",
          message = Some(msg),
          maxExclusive = Some(s"$exclusiveMaximum"),
          datatype = effectiveDataType(shape)
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
        val values = valuesArray.scalars.map(_.toString)
        val msg    = s"Data at $context must be within the values (${values.mkString(",")})"
        val propertyValidation = PropertyConstraint(
          ramlPropertyId = (Namespace.Data + "value").iri(),
          name = validation.name + "_validation_enum/prop",
          message = Some(msg),
          in = values
        )
        validation.copy(propertyConstraints = validation.propertyConstraints ++ Seq(propertyValidation))
      case None => validation
    }
  }

  protected def effectiveDataType(scalar: Shape): Option[String] = {
    shape.fields.?[AmfScalar](ScalarShapeModel.DataType).map(_.toString) match {
      case Some(datatype) =>
        val format = shape.fields.?[AmfScalar](ScalarShapeModel.Format).map(_.toString)
        TypeDefXsdMapping.typeDef(datatype, format.getOrElse("")) match {
          case NumberType =>
            Some((Namespace.Shapes + "number").iri()) // if this is a number, send our custom scalar type
          case _ => Some(datatype) // normal 1:1 mapping, we send the regular XSD type
        }
      case None => None // no XSD datatype

    }
  }
}
