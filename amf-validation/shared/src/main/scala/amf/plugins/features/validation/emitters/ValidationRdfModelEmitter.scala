package amf.plugins.features.validation.emitters

import amf._
import amf.core.model.DataType
import amf.core.rdf.RdfModel
import amf.core.validation.core.{FunctionConstraint, PropertyConstraint, ValidationSpecification}
import amf.core.validation.model.{AlternatePath, PredicatePath, PropertyPath, PropertyPathParser, SequencePath}
import amf.core.vocabulary.Namespace

class ValidationRdfModelEmitter(targetProfile: ProfileName,
                                rdfModel: RdfModel,
                                defaultJSLibraryUrl: String = ValidationJSONLDEmitter.validationLibraryUrl) {

  /**
    * Emit the triples for the validations
    * @param validations validations
    */
  def emit(validations: Seq[ValidationSpecification]): Unit = {
    validations.foreach(emitValidation)
  }

  private def emitValidation(validation: ValidationSpecification): Unit = {
    val validationId = validation.id

    rdfModel.addTriple(validationId, (Namespace.Rdf + "type").iri(), (Namespace.Shacl + "NodeShape").iri())
    val message = targetProfile match {
      case RamlProfile | Raml08Profile => validation.ramlMessage.getOrElse(validation.message)
      case OasProfile                  => validation.oasMessage.getOrElse(validation.message)
      case _                           => validation.message
    }

    if (message != "") {
      genValue(validationId, (Namespace.Shacl + "message").iri(), message)
    }

    if (validation.targetInstance.nonEmpty) {
      validation.targetInstance.distinct.foreach { ti =>
        link(validationId, (Namespace.Shacl + "targetNode").iri(), expandRamlId(ti))
      }
    }

    if (validation.targetClass.nonEmpty) {
      validation.targetClass.foreach { tc =>
        link(validationId, (Namespace.Shacl + "targetClass").iri(), expandRamlId(tc))
      }
    }

    for {
      closedShape <- validation.closed
    } yield {
      if (closedShape) {
        genValue(validationId, (Namespace.Shacl + "closed").iri(), closedShape.toString)
      }
    }

    if (validation.targetObject.nonEmpty) {
      validation.targetObject.foreach { to =>
        link(validationId, (Namespace.Shacl + "targetObjectsOf").iri(), expandRamlId(to))
      }
    }

    if (validation.unionConstraints.nonEmpty) {
      val unionConstraintListId = emitList(validation.unionConstraints, link)
      rdfModel.addTriple(validationId, (Namespace.Shacl + "or").iri(), unionConstraintListId)
    }

    if (validation.andConstraints.nonEmpty) {
      val andConstraintListId = emitList(validation.andConstraints, link)
      rdfModel.addTriple(validationId, (Namespace.Shacl + "and").iri(), andConstraintListId)
    }

    if (validation.xoneConstraints.nonEmpty) {
      val xoneConstraintListId = emitList(validation.xoneConstraints, link)
      rdfModel.addTriple(validationId, (Namespace.Shacl + "xone").iri(), xoneConstraintListId)
    }

    if (validation.notConstraint.isDefined) {
      link(validationId, (Namespace.Shacl + "not").iri(), validation.notConstraint.get)
    }

    if (validation.query.isDefined) {
      val queryValidationId = validationId + "/query"
      link(validationId, (Namespace.Shacl + "sparql").iri(), queryValidationId)
      rdfModel.addTriple(queryValidationId, (Namespace.Shacl + "message").iri(), validation.message, Some(DataType.String))
      rdfModel.addTriple(queryValidationId, (Namespace.Shacl + "select").iri(), validation.query.get.query, Some(DataType.String))
      val ontologyId = validationId + "/query/ontology"
      rdfModel.addTriple(queryValidationId, (Namespace.Shacl + "prefixes").iri(), ontologyId)
      validation.query.get.prefixes.foreach { case (alias, ns) =>
        val declarationId = rdfModel.nextAnonId()
        rdfModel.addTriple(ontologyId, (Namespace.Shacl + "declare").iri(), declarationId)
        rdfModel.addTriple(declarationId, (Namespace.Shacl + "prefix").iri(), alias, Some(DataType.AnyUri))
        rdfModel.addTriple(declarationId, (Namespace.Shacl + "namespace").iri(), ns, Some(DataType.String))
      }
    }

    validation.functionConstraint match {
      case Some(f) => emitFunctionConstraint(validationId, f)
      case _       => // ignore
    }

    for {
      (constraint, values) <- validation.nodeConstraints.groupBy(_.constraint)
    } yield {
      values.foreach { v =>
        link(validationId, Namespace.expand(constraint).iri(), Namespace.expand(v.value).iri())
      }
    }

    if (validation.propertyConstraints.nonEmpty) {
      for {
        (constraint,i) <- validation.propertyConstraints.zipWithIndex
      } yield {
        // processed properties will always include with /prop, this is a CONVENTION
        // can be tricking to follow when debugging
        if (constraint.path.isEmpty && isPropertyConstraintUri(constraint.name)) {
          rdfModel.addTriple(validationId, (Namespace.Shacl + "property").iri(), constraint.name)
          // These are the standard constraints for AMF/RAML/OAS they have already being sanitised
          emitConstraint(constraint.name, constraint)

        } else {
          // this happens when the constraint comes from a profile document
          // an alias for a model element is all the name we provide
          val constraintId = s"$validationId/prop/$i"
          rdfModel.addTriple(validationId, (Namespace.Shacl + "property").iri(), constraintId)
          emitConstraint(constraintId, constraint)
        }
      }
    }
  }
  private def isPropertyConstraintUri(name: String): Boolean = {
    (name.startsWith("http://") || name.startsWith("https://") || name.startsWith("file:")) && name.indexOf("/prop") > -1
  }


  /**
   * Builds a path property in a property constraint parsing a provided constraint path
   * @param constraintId
   * @param constraint
   */
  protected def assertPropertyPath(constraintId: String, constraint: PropertyConstraint): Unit = {
    val parsedPath = constraint.path.get
    val pathId = buildPath(constraintId + "_path", parsedPath)
    link(constraintId, (Namespace.Shacl + "path").iri(), pathId)
  }

  /**
   * Emits the triples
   * @param base
   * @param parsedPath
   * @return
   */
  def buildPath(base: String, parsedPath: PropertyPath): String = {
    parsedPath match {
      case PredicatePath(p, false, false) =>
        p
      case PredicatePath(p, true, false) =>
        val uri = base + "_neg"
        link(uri, (Namespace.Shacl + "inversePath").iri(), p)
        uri
      case PredicatePath(p, false, true) =>
        val uri = base + "_neg"
        link(uri, (Namespace.Shacl + "zeroOrMorePath").iri(), p)
        uri
      case SequencePath(elements)        =>
        val uri = base + "_seq"
        val nestedIds = elements.zipWithIndex.map { case (path, i) =>  buildPath(s"${uri}$i", path) }
        emitAnonList(nestedIds, link)
        // emitList(nestedIds, link)
      case AlternatePath(elements)       =>
        val uri = base + "_alt"
        val nestedIds = elements.zipWithIndex.map { case (path, i) =>  buildPath(s"${uri}$i", path) }
        val listId = emitAnonList(nestedIds, link)
        val altId = rdfModel.nextAnonId()
        link(altId, (Namespace.Shacl + "alternativePath").iri(), listId)
        altId
      case other                         => throw new Exception(s"""Cannote emit path, unsupported type of path token $other""")// ignore
    }
  }

  private def emitConstraint(constraintId: String, constraint: PropertyConstraint): Unit = {
    if (Option(constraint.ramlPropertyId).isDefined) {

      if (constraint.path.isDefined) {
        assertPropertyPath(constraintId, constraint)
      } else {
        link(constraintId, (Namespace.Shacl + "path").iri(), expandRamlId(constraint.ramlPropertyId))
      }

      constraint.maxCount.foreach(genPropertyConstraintValue(constraintId, "maxCount", _, Some(constraint)))
      constraint.minCount.foreach(genPropertyConstraintValue(constraintId, "minCount", _, Some(constraint)))
      constraint.maxLength.foreach(genPropertyConstraintValue(constraintId, "maxLength", _, Some(constraint)))
      constraint.minLength.foreach(genPropertyConstraintValue(constraintId, "minLength", _, Some(constraint)))
      constraint.maxExclusive.foreach(
        genNumericPropertyConstraintValue(constraintId, "maxExclusive", _, Some(constraint)))
      constraint.minExclusive.foreach(
        genNumericPropertyConstraintValue(constraintId, "minExclusive", _, Some(constraint)))
      constraint.maxInclusive.foreach(
        genNumericPropertyConstraintValue(constraintId, "maxInclusive", _, Some(constraint)))
      constraint.minInclusive.foreach(
        genNumericPropertyConstraintValue(constraintId, "minInclusive", _, Some(constraint)))
      constraint.multipleOf.foreach(
        genCustomPropertyConstraintValue(constraintId, (Namespace.Shapes + "multipleOfValidationParam").iri(), _))
      constraint.pattern.foreach(v => genPropertyConstraintValue(constraintId, "pattern", v))
      constraint.node.foreach(genPropertyConstraintValue(constraintId, "node", _))
      constraint.value.foreach(genPropertyConstraintValue(constraintId, "hasValue", _, Some(constraint)))
      if (constraint.atLeast.isDefined) {
        rdfModel.addTriple(constraintId, (Namespace.Shacl + "qualifiedMinCount").iri(), constraint.atLeast.get._1.toString, Some((Namespace.Xsd + "integer").iri()))
        link(constraintId, (Namespace.Shacl + "qualifiedValueShape").iri(), constraint.atLeast.get._2)
      }
      if (constraint.atMost.isDefined) {
        rdfModel.addTriple(constraintId, (Namespace.Shacl + "qualifiedMaxCount").iri(), constraint.atMost.get._1.toString, Some((Namespace.Xsd + "integer").iri()))
        link(constraintId, (Namespace.Shacl + "qualifiedValueShape").iri(), constraint.atMost.get._2)
      }
      if (constraint.equalToProperty.isDefined) {
        link(constraintId, (Namespace.Shacl + "equals").iri(), constraint.equalToProperty.get)
      }
      if (constraint.disjointWithProperty.isDefined) {
        link(constraintId, (Namespace.Shacl + "disjoint").iri(), constraint.disjointWithProperty.get)
      }
      if (constraint.lessThanProperty.isDefined) {
        link(constraintId, (Namespace.Shacl + "lessThan").iri(), constraint.lessThanProperty.get)
      }
      if (constraint.lessThanOrEqualsToProperty.isDefined) {
        link(constraintId, (Namespace.Shacl + "lessThanOrEquals").iri(), constraint.lessThanOrEqualsToProperty.get)
      }
      constraint.datatype.foreach { v =>
        if (v.endsWith("#integer")) {
          link(constraintId, (Namespace.Shacl + "datatype").iri(), DataType.Long)
        } else if (!v.endsWith("#float") && !v.endsWith("#number")) {
          // raml/oas 'number' are actually the union of integers and floats
          // i handle the data type integer and float inside of every constraint. Here only need to generate the simples data types for path entry
          link(constraintId, (Namespace.Shacl + "datatype").iri(), v)
        }
      }
      if (constraint.`class`.nonEmpty) {
        if (constraint.`class`.length == 1) {
          link(constraintId, (Namespace.Shacl + "class").iri(), constraint.`class`.head)
        } else {
          val classListId = emitList(constraint.`class`, link)
          rdfModel.addTriple(constraintId, (Namespace.Shacl + "or").iri(), classListId)
        }
      }

      // custom builder
      constraint.customRdf.foreach { builder =>
        builder(rdfModel, constraintId)
      }

      if (constraint.in.nonEmpty) {
        val inListId = emitList(constraint.in, (s, p, o) => genValue(s, p, o, constraint.datatype))
        link(constraintId, (Namespace.Shacl + "in").iri(), inListId)
      }
    }
  }

  private def emitFunctionConstraint(validationId: String, f: FunctionConstraint): Unit = {
    genJSValidator(validationId, f)
    genJSConstraint(validationId, f)
    genValue(validationId, f.validatorPath(validationId), "true")
  }

  private def genJSConstraint(validationId: String, f: FunctionConstraint) = {
    val constraintId  = f.constraintId(validationId)
    val validatorId   = f.validatorId(validationId)
    val validatorPath = f.validatorPath(validationId)

    rdfModel.addTriple(constraintId, (Namespace.Rdf + "type").iri(), (Namespace.Shacl + "ConstraintComponent").iri())
    link(constraintId, (Namespace.Shacl + "validator").iri(), validatorId)

    if (f.parameters.nonEmpty) {
      val parameterId = constraintId + "_param"
      link(constraintId, (Namespace.Shacl + "parameter").iri(), parameterId)
      link(parameterId, (Namespace.Shacl + "path").iri(), f.parameters.head.path)
      link(parameterId, (Namespace.Shacl + "datatype").iri(), f.parameters.head.datatype)
    } else {
      val parameterId = constraintId + "_param"
      link(constraintId, (Namespace.Shacl + "parameter").iri(), parameterId)
      link(parameterId, (Namespace.Shacl + "path").iri(), validatorPath)
      link(parameterId, (Namespace.Shacl + "datatype").iri(), DataType.Boolean)
    }
  }

  private def genJSValidator(validationId: String, f: FunctionConstraint) = {
    val validatorId = f.validatorId(validationId)

    rdfModel.addTriple(validatorId, (Namespace.Rdf + "type").iri(), (Namespace.Shacl + "JSValidator").iri())
    f.message.foreach(msg => genValue(validatorId, (Namespace.Shacl + "message").iri(), msg))
    val libraryUrl = validatorId + "_url"
    //val libraryUrl = ValidationJSONLDEmitter.validationLibraryUrl
    link(validatorId, (Namespace.Shacl + "jsLibrary").iri(), libraryUrl)

    f.functionName match {
      case Some(fnName) =>
        for { library <- f.libraries } {
          rdfModel.addTriple(libraryUrl,
                             (Namespace.Shacl + "jsLibraryURL").iri(),
                             library,
                             Some("http://www.w3.org/2001/XMLSchema#anyUri"))
        }
        genValue(validatorId, (Namespace.Shacl + "jsFunctionName").iri(), fnName)

      case None =>
        f.code match {
          case Some(_) =>
            for { library <- Seq(defaultJSLibraryUrl) ++ f.libraries } {
              rdfModel.addTriple(libraryUrl,
                                 (Namespace.Shacl + "jsLibraryURL").iri(),
                                 library,
                                 Some("http://www.w3.org/2001/XMLSchema#anyUri"))
            }
            genValue(validatorId, (Namespace.Shacl + "jsFunctionName").iri(), f.computeFunctionName(validationId))

          case _ =>
            throw new Exception("Cannot emit validator without JS code or JS function name")
        }
    }
  }

  private def genPropertyConstraintValue(constraintId: String,
                                         constraintName: String,
                                         value: String,
                                         constraint: Option[PropertyConstraint] = None): Unit = {
    constraint.flatMap(_.datatype) match {
      case Some(scalarType) if scalarType == (Namespace.Shapes + "number").iri() =>
        val orConstraintListId   = constraintId + "_ointdoub1"
        val nextConstraintListId = constraintId + "_ointdoub2"
        rdfModel.addTriple(constraintId, (Namespace.Shacl + "or").iri(), orConstraintListId)
        link(orConstraintListId, (Namespace.Rdf + "first").iri(), orConstraintListId + "_v")
        genValue(orConstraintListId + "_v",
                 (Namespace.Shacl + constraintName).iri(),
                 value.toDouble.floor.toInt.toString,
                 Some(DataType.Long))
        link(orConstraintListId, (Namespace.Rdf + "rest").iri(), nextConstraintListId)
        link(nextConstraintListId, (Namespace.Rdf + "first").iri(), nextConstraintListId + "_v")
        genValue(nextConstraintListId + "_v",
                 (Namespace.Shacl + constraintName).iri(),
                 value.toDouble.toString,
                 Some(DataType.Double))
        link(nextConstraintListId, (Namespace.Rdf + "rest").iri(), (Namespace.Rdf + "nil").iri())
      case Some(_) =>
        genValue(constraintId, (Namespace.Shacl + constraintName).iri(), value, constraint.flatMap(_.datatype))
      case None =>
        genValue(constraintId, (Namespace.Shacl + constraintName).iri(), value, None)
    }

  }

  private def genCustomPropertyConstraintValue(constraintId: String, constraintIri: String, value: String): Unit = {
    genValue(constraintId, constraintIri, value, None)
  }

  def emitList(listValues: Seq[String], generator: (String, String, String) => Unit): String = {
    val baseListId = rdfModel.nextAnonId()
    var listId     = baseListId
    val origListId = listId

    val totalElements = listValues.length
    listValues.zipWithIndex.foreach {
      case (value, i) =>
        generator(listId, (Namespace.Rdf + "first").iri(), value)
        if (i < totalElements - 1) {
          val nextListId = baseListId + s"_$i"
          link(listId, (Namespace.Rdf + "rest").iri(), nextListId)
          listId = nextListId
        } else {
          link(listId, (Namespace.Rdf + "rest").iri(), (Namespace.Rdf + "nil").iri())
        }
    }

    origListId
  }

  def emitAnonList(listValues: Seq[String], generator: (String, String, String) => Unit): String = {
    var listId = rdfModel.nextAnonId()
    val origListId = listId

    val totalElements = listValues.length
    listValues.zipWithIndex.foreach {
      case (value, i) =>
        generator(listId, (Namespace.Rdf + "first").iri(), value)
        if (i < totalElements - 1) {
          val nextListId = rdfModel.nextAnonId()
          link(listId, (Namespace.Rdf + "rest").iri(), nextListId)
          listId = nextListId
        } else {
          link(listId, (Namespace.Rdf + "rest").iri(), (Namespace.Rdf + "nil").iri())
        }
    }

    origListId
  }

  private def genNumericPropertyConstraintValue(constraintId: String,
                                                constraintName: String,
                                                value: String,
                                                constraint: Option[PropertyConstraint] = None): Unit = {
    constraint.flatMap(_.datatype) match {
      case Some(scalarType) if scalarType == DataType.Number || scalarType == DataType.Double =>
        genValue(constraintId,
                 (Namespace.Shacl + constraintName).iri(),
                 value.toDouble.toString,
                 Some(DataType.Double))
      case None =>
        genValue(constraintId,
                 (Namespace.Shacl + constraintName).iri(),
                 value.toDouble.toString,
                 Some(DataType.Double))
      case Some(scalarType) if scalarType == DataType.Float =>
        genValue(constraintId, (Namespace.Shacl + constraintName).iri(), value.toDouble.toString, Some(DataType.Float))
      case Some(scalarType)
          if scalarType == DataType.Integer || scalarType == (Namespace.Xsd + "long")
            .iri() => // serialize the int has integer beacuse the example it is parsed with that datatype
        genValue(constraintId,
                 (Namespace.Shacl + constraintName).iri(),
                 value.toDouble.floor.toInt.toString,
                 Some(DataType.Long))
      case _ => // ignore
    }
  }

  private def expandRamlId(s: String): String =
    if (s.startsWith("http://") || s.startsWith("https://") || s.startsWith("file:")) {
      s.trim
    } else {
      Namespace.expand(s.replace(".", ":")).iri().trim
    }

  private def genNonEmptyList(subject: String, property: String): Unit = {
    val listId           = rdfModel.nextAnonId()
    val listIdConstraint = listId + "_c"
    rdfModel.addTriple(listId, (Namespace.Rdf + "type").iri(), (Namespace.Shacl + "NodeShape").iri())
    rdfModel.addTriple(listId, (Namespace.Shacl + "message").iri(), "List cannot be empty", None)
    rdfModel.addTriple(listId, (Namespace.Shacl + "property").iri(), listIdConstraint)
    rdfModel.addTriple(listIdConstraint, (Namespace.Shacl + "path").iri(), (Namespace.Rdfs + "_1").iri())
    rdfModel.addTriple(listIdConstraint, (Namespace.Shacl + "minCount").iri(), "1", Some(DataType.Integer))
  }

  private def inGenValue(subject: String, property: String, s: String): Unit =
    rdfModel.addTriple(subject, property, s, Some("http://www.w3.org/2001/XMLSchema#string"))
//    genValue(subject, property, s, Some("http://www.w3.org/2001/XMLSchema#string"))

  private def genValue(subject: String, property: String, s: String, dType: Option[String] = None): Unit = {
    dType match {
      case Some(dt) =>
        rdfModel.addTriple(subject, property, s, Some(dt))
      case None =>
        if (s.matches("^-?[1-9]\\d*$|^0$")) { // if the number starts with 0 (and is not 0), its a string and should be quoted
          rdfModel.addTriple(subject, property, s, Some(DataType.Long))
        } else if (s == "true" || s == "false") {
          rdfModel.addTriple(subject, property, s, Some(DataType.Boolean))
        } else if (Namespace.expand(s).iri() == Namespace.expand("amf-parser:NonEmptyList").iri()) {
          genNonEmptyList(subject, property)
        } else if (s.startsWith("http://") || s.startsWith("https://") || s.startsWith("file:")) {
          link(subject, property, s)
        } else {
          rdfModel.addTriple(subject, property, s, None)
        }
    }
  }

  def link(subject: String, property: String, objId: String) = rdfModel.addTriple(subject, property, objId)
}
