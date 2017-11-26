package amf.plugins.features.validation.model

import amf.core.validation.core._
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies.model.domain.DomainEntity

import scala.collection.mutable

trait DialectWrapper {

  def expand(value: String, prefixes: mutable.Map[String,String]): String = {
    val valuePrefix = value.split("\\.").head
    prefixes.get(valuePrefix) match {
      case Some(uri) => value.replace(valuePrefix + ".", uri)
      case _         => value
    }
  }

  def extractString(node: DomainEntity, property: String): Option[String] = {
    node.definition.props.get(property) match {
      case Some(profileProperty) => node.string(profileProperty)
      case _                     => None
    }
  }

  def extractStrings(node: DomainEntity, property: String): Seq[String] = {
    node.definition.props.get(property) match {
      case Some(profileProperty) => node.strings(profileProperty)
      case None                  => Seq()
    }
  }

  def mapEntities[T](node: DomainEntity, property: String, f: (DomainEntity) => T): Seq[T] = {
    node.definition.props.get(property) match {
      case Some(profileProperty) => node.entities(profileProperty).map(f)
      case None                  => Seq()
    }
  }

  def mapEntity[T](node: DomainEntity, property: String, f: (DomainEntity) => T): Option[T] = {
    node.definition.props.get(property) match {
      case Some(profileProperty) => node.entity(profileProperty).map(f)
      case _                     => None
    }
  }

  def prefixes(node: DomainEntity) = {
    val prefixMap: mutable.Map[String,String] = mutable.HashMap()
    node.definition.props.get("prefixes") match {
      case Some(prefixesProperty) => node.entities(prefixesProperty) foreach  { prefixEntity =>
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
    case None    => throw new Exception(s"Missing mandatory property $message")
  }

}

object ParsedFunctionConstraint extends DialectWrapper {
  def apply(node: DomainEntity): FunctionConstraint = {
    FunctionConstraint(
      message          = extractString(node, "message"),
      code             = extractString(node, "code"),
      libraries        = extractStrings(node, "libraries"),
      functionName     = extractString(node, "functionName"),
    )
  }
}


object ParsedPropertyConstraint extends DialectWrapper {
  def apply(node: DomainEntity, prefixes: mutable.Map[String,String]): PropertyConstraint = {
    PropertyConstraint(
      ramlPropertyId      = expand(mandatory("ramlID in property constraint", node.linkValue), prefixes),
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

  def apply(node: DomainEntity, prefixes: mutable.Map[String,String]): ValidationSpecification = {
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
  def apply(node: DomainEntity): ValidationProfile = {
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