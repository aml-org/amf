package amf.plugins.features.validation.model

import amf.ProfileName
import amf.core.validation.core._
import amf.plugins.document.vocabularies.model.domain.DialectDomainElement

import scala.collection.mutable

trait DialectWrapper {

  def expand(value: String, prefixes: mutable.Map[String,String]): String = {
    val valuePrefix = value.split("\\.").head
    prefixes.get(valuePrefix) match {
      case Some(uri) => value.replace(valuePrefix + ".", uri)
      case _         => value
    }
  }

  def extractString(node: DialectDomainElement, property: String): Option[String] = {
    node.definedBy.propertiesMapping().find(_.name().value() == property).flatMap(p => node.literalProperty(p.toField)).map(_.toString)
  }

  def extractInt(node: DialectDomainElement, property: String): Option[Int] = {
    node.definedBy.propertiesMapping().find(_.name().value() == property).flatMap(p => node.literalProperty(p.toField)).map(_.asInstanceOf[Int])
  }

  def extractStrings(node: DialectDomainElement, property: String): Seq[String] = {
    node.definedBy.propertiesMapping().find(_.name().value() == property).map(p => node.literalProperties(p.toField).map(_.toString)).getOrElse(Nil)
  }

  def mapEntities[T](node: DialectDomainElement, property: String, f: DialectDomainElement => T): Seq[T] = {
    node.definedBy.propertiesMapping().find(_.name().value() == property).map(p => node.objectCollectionProperty(p.toField).map(f)).getOrElse(Nil)
  }


  def mapIndexedEntities[T](node: DialectDomainElement, property: String, f: (DialectDomainElement, Int) => T): Seq[T] = {
    node.definedBy.propertiesMapping().find(_.name().value() == property).map {p =>
      node.objectCollectionProperty(p.toField).zipWithIndex.map { case (dialectDomainElement,i) =>
        f(dialectDomainElement,i)
      }
    } getOrElse(Nil)
  }


  def mapEntity[T](node: DialectDomainElement, property: String, f: DialectDomainElement => T): Option[T] =
    node.definedBy.propertiesMapping().find(_.name().value() == property).flatMap(p => node.objectProperty(p.toField).map(f))

  def prefixes(node: DialectDomainElement): mutable.Map[String, String] = {
    val prefixMap: mutable.Map[String,String] = mutable.HashMap()
    node.definedBy.propertiesMapping().find(_.name().value() == "prefixes").map(_.toField) match {
      case Some(prefixesProperty) => node.objectCollectionProperty(prefixesProperty).foreach { prefixEntity =>
          val prefix = extractString(prefixEntity, "prefix").getOrElse("")
          val prefixUri = extractString(prefixEntity, "uri").getOrElse("")
          prefixMap.put(prefix, prefixUri)
      }
      case _ =>
    }
    prefixMap
  }

  def mandatory[T](message: String, x: Option[T]): T = x match {
    case Some(e) => e
    case None    => throw new Exception(s"Missing mandatory property '$message'")
  }

}

object ParsedFunctionConstraint extends DialectWrapper {
  def apply(node: DialectDomainElement): FunctionConstraint = {
    FunctionConstraint(
      message          = extractString(node, "message"),
      code             = extractString(node, "code"),
      libraries        = extractStrings(node, "libraries"),
      functionName     = extractString(node, "functionName"),
    )
  }
}


object ParsedPropertyConstraint extends DialectWrapper {
  val validationNs = "http://a.ml/vocabularies/amf-validation#"

  def apply(node: DialectDomainElement, prefixes: mutable.Map[String,String], nameForNestedValidation: String, message: Option[String] = None, nested: String): (PropertyConstraint, Seq[ValidationSpecification]) = {
    val name = extractString(node, "name")

    val (nestedNode, nestedConstraints) = mapEntity(node, "nested", ParsedValidationSpecification(_, prefixes, Some(s"${nameForNestedValidation}_${name.getOrElse("")}_node"), message, None, Some(nested))).map { case (v, nested) => (Seq(v), nested) } getOrElse((Nil, Nil))
    val atLeastTuple = mapEntity(node, "atLeast", ParsedQualifiedValidationSpecification(_, prefixes, Some(s"${nameForNestedValidation}_${name.getOrElse("")}_atLeast_"), message, None, Some(nested)))
    val atMostTuple = mapEntity(node, "atMost", ParsedQualifiedValidationSpecification(_, prefixes, Some(s"${nameForNestedValidation}_${name.getOrElse("")}_atLeast_"), message, None, Some(nested)))


    val property = PropertyConstraint(
      ramlPropertyId      = expand(mandatory("ramlID in property constraint", name), prefixes),
      name                = mandatory("name in property constraint", name),
      message             = extractString(node, "message"),
      pattern             = extractString(node, "pattern"),
      maxCount            = extractString(node, "maxCount"),
      minCount            = extractString(node, "minCount"),
      maxLength           = extractString(node, "maxLength"),
      minLength           = extractString(node, "minLength"),
      maxExclusive        = extractString(node, "maxExclusive"),
      minExclusive        = extractString(node, "minExclusive"),
      maxInclusive        = extractString(node, "maxInclusive"),
      minInclusive        = extractString(node, "minInclusive"),
      in                  = extractStrings(node, "in"),
      node                = nestedNode.headOption.map(_.id),
      atLeast = atLeastTuple.map { t => (t._1, t._2.id)},
      atMost = atMostTuple.map { t => (t._1, t._2.id)},
    )

    val atLeastAcc = atLeastTuple.map(t => Seq(t._2)).getOrElse(Nil) ++ atLeastTuple.map(_._3).getOrElse(Nil)
    val atLMostAcc = atMostTuple.map(t => Seq(t._2)).getOrElse(Nil) ++ atMostTuple.map(_._3).getOrElse(Nil)

    (property, nestedNode ++ nestedConstraints ++ atLeastAcc ++ atLMostAcc)
  }
}
object ParsedQualifiedValidationSpecification extends DialectWrapper {

  def apply(node: DialectDomainElement, prefixes: mutable.Map[String,String], nameForNestedValidation: Option[String] = None, message: Option[String] = None, targetClassForNestedValidation: Option[Seq[String]] = None, nested: Option[String] = None): (Int, ValidationSpecification, Seq[ValidationSpecification]) = {
    val count:Int  = mandatory("count missing in qualified validation specification", extractInt(node, "count"))
    val (validation, nestedValidations) = mapEntity(node, "validation", ParsedValidationSpecification(_, prefixes, nameForNestedValidation, message, None, nested)).get


    (count, validation, nestedValidations)
  }
}

object ParsedValidationSpecification extends DialectWrapper {

  def apply(node: DialectDomainElement, prefixes: mutable.Map[String,String], nameForNestedValidation: Option[String] = None, message: Option[String] = None, targetClassForNestedValidation: Option[Seq[String]] = None, nested: Option[String] = None): (ValidationSpecification, Seq[ValidationSpecification]) = {
    val name:String  = nameForNestedValidation.getOrElse(mandatory("name in validation specification", extractString(node, "name")))
    val targetClasses: Seq[String] = targetClassForNestedValidation.getOrElse(extractStrings(node, "targetClass").map(expand(_, prefixes)))
    val finalMessage: String = message.getOrElse(extractString(node, "message").getOrElse(s"Unsatisfied constraint ${name}"))
    val finalNested: Option[String] = Some(nested.getOrElse(name))

    val propsTuples: Seq[(PropertyConstraint, Seq[ValidationSpecification])] = mapEntities(node, "propertyConstraints", ParsedPropertyConstraint(_, prefixes, name, Some(finalMessage), finalNested.get))
    val propertyConstraints = propsTuples.map(_._1)
    val nestedProperties = propsTuples.flatMap(_._2)

    val base = ValidationSpecification(
      name                = name,
      message             = finalMessage,
      targetClass         = targetClasses,
      propertyConstraints = propertyConstraints,
      functionConstraint  = mapEntity(node, "functionConstraint", ParsedFunctionConstraint.apply),
      nested              = nested
    )


    // compute nested shapes
    val (andConstraints, nestedAndConstraints) = collectNestedValidations(mapIndexedEntities(node, "and", { case (n: DialectDomainElement,i: Int) =>
      ParsedValidationSpecification(n, prefixes, Some(s"${name}_and_${i}"), Some(finalMessage), None, finalNested)
    }))
    val (orConstraints, nestedOrConstraints) = collectNestedValidations(mapIndexedEntities(node, "or", { case (n: DialectDomainElement,i: Int) =>
      ParsedValidationSpecification(n, prefixes, Some(s"${name}_or_${i}"), Some(finalMessage), None, finalNested)
    }))
    val (xoneConstraints, xoneNestedConstraints) = collectNestedValidations(mapIndexedEntities(node, "xone", { case (n: DialectDomainElement,i: Int) =>
      ParsedValidationSpecification(n, prefixes, Some(s"${name}_xone_${i}"), Some(finalMessage), None, finalNested)
    }))

    val (notConstraints, notNestedConstraints) = mapEntity(node, "not", ParsedValidationSpecification(_, prefixes, Some(s"${name}_not"), Some(finalMessage), None, finalNested)).map { case (v, nested) => (Seq(v), nested) } getOrElse((Nil, Nil))

    val allNested = nestedAndConstraints ++ nestedOrConstraints ++ xoneNestedConstraints ++ notNestedConstraints ++ nestedProperties

    val finalValidation = base.copy(
      andConstraints = andConstraints.map(_.id),
      unionConstraints = orConstraints.map(_.id),
      xoneConstraints = xoneConstraints.map(_.id),
      notConstraint =  notConstraints.headOption.map(_.id)
    )
    (finalValidation, andConstraints ++ orConstraints ++ xoneConstraints ++ notConstraints  ++ allNested)
  }

  protected def collectNestedValidations(nestedValidations: Seq[(ValidationSpecification, Seq[ValidationSpecification])]): (Seq[ValidationSpecification], Seq[ValidationSpecification]) = {
    nestedValidations.foldLeft((Seq[ValidationSpecification](), Seq[ValidationSpecification]())) { case ((validationsAcc, nestedAcc), (validation, nested)) =>
      (validationsAcc ++ Seq(validation), nestedAcc ++ nested)
    }
  }
}

object ParsedValidationProfile extends DialectWrapper {

  def apply(node: DialectDomainElement): ValidationProfile = {
    val prfx = prefixes(node)
    ValidationProfile(
      name            = ProfileName(mandatory("profile in validation profile", extractString(node, "profile"))),
      baseProfile = extractString(node, "extends").map(ProfileName.apply),
      violationLevel  = extractStrings(node, "violation"),
      infoLevel       = extractStrings(node, "info"),
      warningLevel    = extractStrings(node, "warning"),
      disabled        = extractStrings(node, "disabled"),
      validations     = collectValidations(mapEntities(node, "validations", ParsedValidationSpecification(_, prfx))),
      prefixes        = prfx
    )
  }

  protected def collectValidations(nestedValidations: Seq[(ValidationSpecification, Seq[ValidationSpecification])]): Seq[ValidationSpecification] = {
    nestedValidations.foldLeft(Seq[ValidationSpecification]()) { case (validationsAcc, (validation, nested)) =>
      validationsAcc ++ Seq(validation) ++ nested
    }
  }

}