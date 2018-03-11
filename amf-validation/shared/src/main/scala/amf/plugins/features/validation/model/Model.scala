package amf.plugins.features.validation.model

import amf.core.validation.core._
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies2.model.domain.DialectDomainElement

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
    node.definedBy.propertiesMapping().find(_.name() == property) match {
      case Some(profileProperty) if node.literalProperties.contains(profileProperty.id) =>
        node.literalProperties.get(profileProperty.id).map(_.toString)
      case Some(profileProperty) if node.mapKeyProperties.contains(profileProperty.nodePropertyMapping()) =>
        node.mapKeyProperties.get(profileProperty.nodePropertyMapping()).map(_.toString)
      case _                     => None
    }
  }

  def extractStrings(node: DialectDomainElement, property: String): Seq[String] = {
    node.definedBy.propertiesMapping().find(_.name() == property) match {
      case Some(profileProperty) if node.literalProperties.contains(profileProperty.id) =>
        node.literalProperties(profileProperty.id).asInstanceOf[Seq[String]]
      case _ => Seq()
    }
  }

  def mapEntities[T](node: DialectDomainElement, property: String, f: (DialectDomainElement) => T): Seq[T] = {
    node.definedBy.propertiesMapping().find(_.name() == property) match {
      case Some(profileProperty) if node.objectCollectionProperties.contains(profileProperty.id) =>
        node.objectCollectionProperties(profileProperty.id).map(f)
      case _ => Seq()
    }
  }

  def mapEntity[T](node: DialectDomainElement, property: String, f: (DialectDomainElement) => T): Option[T] = {
    node.definedBy.propertiesMapping().find(_.name() == property) match {
      case Some(profileProperty) if node.objectProperties.contains(profileProperty.id) =>
        node.objectProperties.get(profileProperty.id).map(f)
      case _ => None
    }
  }

  def prefixes(node: DialectDomainElement) = {
    val prefixMap: mutable.Map[String,String] = mutable.HashMap()
    node.definedBy.propertiesMapping().find(_.name() == "prefixes") match {
      case Some(prefixesProperty) => node.objectCollectionProperties.get(prefixesProperty.id).map { prefixes =>
        prefixes foreach  { prefixEntity =>
          val prefix = extractString(prefixEntity, "prefix").getOrElse("")
          val prefixUri = extractString(prefixEntity, "uri").getOrElse("")
          prefixMap.put(prefix, prefixUri)
        }
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
  val validationNs = "http://raml.org/vocabularies/amf-validation#"

  def apply(node: DialectDomainElement, prefixes: mutable.Map[String,String]): PropertyConstraint = {
    PropertyConstraint(
      ramlPropertyId      = expand(mandatory("ramlID in property constraint", extractString(node, "name")), prefixes),
      name                = mandatory("name in property constraint", extractString(node, "name")),
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
      in                  = extractStrings(node, "in")
    )
  }
}

object ParsedValidationSpecification extends DialectWrapper {

  val PARSER_SIDE_VALIDATION = (Namespace.Shapes + "ParserShape").iri()

  def apply(node: DialectDomainElement, prefixes: mutable.Map[String,String]): ValidationSpecification = {
    ValidationSpecification(
      name                = mandatory("name in validation specification", extractString(node, "name")),
      message             = mandatory("message in validation specification", extractString(node, "message")),
      targetClass         = extractStrings(node, "targetClass").map(expand(_, prefixes)),
      propertyConstraints = mapEntities(node, "propertyConstraints", ParsedPropertyConstraint(_, prefixes)),
      functionConstraint  = mapEntity(node, "functionConstraint", ParsedFunctionConstraint.apply)
    )
  }
}

object ParsedValidationProfile extends DialectWrapper {
  def apply(node: DialectDomainElement): ValidationProfile = {
    val prfx = prefixes(node)
    ValidationProfile(
      name            = mandatory("profile in validation profile", extractString(node, "profile")),
      baseProfileName = extractString(node, "extends"),
      violationLevel  = extractStrings(node, "violation"),
      infoLevel       = extractStrings(node, "info"),
      warningLevel    = extractStrings(node, "warning"),
      disabled        = extractStrings(node, "disabled"),
      validations     = mapEntities(node, "validations", ParsedValidationSpecification(_, prfx)),
      prefixes        = prfx
    )
  }
}