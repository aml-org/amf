package amf.validations

import amf._
import amf.core.validation.core.ValidationSpecification
import amf.core.validation.core.ValidationSpecification.PARSER_SIDE_VALIDATION
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.AmfParser
import amf.plugins.features.validation.Validations

// noinspection TypeAnnotation
object ShapeParserSideValidations extends Validations {
  override val specification: String = PARSER_SIDE_VALIDATION
  override val namespace: Namespace  = AmfParser

  val UserDefinedFacetMatchesBuiltInFacets = validation(
    "user-defined-facets-matches-built-in",
    "User defined facet name matches built in facet of type"
  )

  val UserDefinedFacetMatchesAncestorsTypeFacets = validation(
    "user-defined-facets-matches-ancestor",
    "User defined facet name matches ancestor type facet"
  )

  val MissingRequiredUserDefinedFacet = validation(
    "missing-user-defined-facet",
    "Type is missing required user defined facet"
  )

  val UnableToParseShapeExtensions = validation(
    "unable-to-parse-shape-extensions",
    "Unable to parse shape extensions"
  )

  val ExceededMaxYamlReferences = validation(
    "max-yaml-references",
    "Exceeded maximum yaml references threshold"
  )

  override val levels: Map[String, Map[ProfileName, String]] = Map(
    )

  override val validations: List[ValidationSpecification] = List(
    UserDefinedFacetMatchesBuiltInFacets,
    UserDefinedFacetMatchesAncestorsTypeFacets,
    MissingRequiredUserDefinedFacet,
    UnableToParseShapeExtensions,
    ExceededMaxYamlReferences
  )
}
