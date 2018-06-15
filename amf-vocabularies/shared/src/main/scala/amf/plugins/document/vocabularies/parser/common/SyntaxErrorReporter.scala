package amf.plugins.document.vocabularies.parser.common

import amf.core.annotations.LexicalInformation
import amf.core.parser.Position.ZERO
import amf.core.parser.{Annotations, ErrorHandler}
import amf.plugins.document.vocabularies.metamodel.domain.PropertyMappingModel
import amf.plugins.document.vocabularies.model.domain.PropertyMapping
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model.{YNode, YPart}


trait SyntaxErrorReporter { this: ErrorHandler =>


  def missingTermViolation(term: String, node: String, ast: YPart) = {
    violation(ParserSideValidations.MissingTermSpecification.id, node, s"Cannot find vocabulary term $term", ast)
  }

  def missingFragmentViolation(fragment: String, node: String, ast: YPart) = {
    violation(ParserSideValidations.MissingFragmentSpecification.id, node, s"Cannot find fragment $fragment", ast)
  }

  def missingPropertyRangeViolation(term: String, node: String, lexical: Option[LexicalInformation]) = {
    violation(
      ParserSideValidations.MissingPropertyRangeSpecification.id,
      node,
      Some(PropertyMappingModel.ObjectRange.value.iri()),
      s"Cannot find property range term $term",
      lexical)
  }

  def inconsistentPropertyRangeValueViolation(node: String, property: PropertyMapping, expected: String, found: String, valueNode: YNode) = {
    violation(
      ParserSideValidations.InconsistentPropertyRangeValueSpecification.id,
      node,
      Some(property.nodePropertyMapping().value()),
      s"Cannot find expected range for property ${property.nodePropertyMapping().value()} (${property.name().value()}). Found '$found', expected '$expected'",
      Annotations(valueNode).find(classOf[LexicalInformation]))
  }

  def closedNodeViolation(id: String, property: String, nodeType: String, ast: YPart) = {
    violation(
      ParserSideValidations.ClosedShapeSpecification.id,
      id,
      s"Property: '$property' not supported in a $nodeType node",
      ast
    )
  }

  def missingPropertyViolation(id: String, property: String, nodeType: String, ast: YPart) = {
    violation(
      ParserSideValidations.MissingPropertySpecification.id,
      id,
      s"Property: '$property' mandatory in a $nodeType node",
      ast
    )
  }
}

object RamlVocabulariesParsersideValidations {

}
