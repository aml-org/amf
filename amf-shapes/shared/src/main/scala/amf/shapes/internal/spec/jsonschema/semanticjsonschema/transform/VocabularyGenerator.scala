package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.AMLConfiguration
import amf.aml.client.scala.model.document.{Dialect, Vocabulary}
import amf.aml.client.scala.model.domain.{DatatypePropertyTerm, NodeMapping, PropertyMapping, PropertyTerm}
import amf.aml.internal.render.emitters.common.IdCounter
import amf.aml.internal.transform.steps.DialectCombiningMappingStage
import amf.core.client.scala.errorhandling.DefaultErrorHandler
import amf.core.client.scala.model.domain.DomainElement

import scala.collection.mutable

case class CandidateProperty(property: PropertyMapping, termToReplace: Seq[String])

class VocabularyGenerator(dialect: Dialect, multipleTerms: Seq[CandidateProperty], options: SchemaTransformerOptions) {

  def generateVocabulary(): Option[Vocabulary] = {
    calculateCombiningMappings()
    val termsToExtract = dialect.declares.flatMap {
      case mapping: NodeMapping => analyzeSemantics(mapping)
      case _                    => Nil
    }
    val vocabulary = generateVocabulary(termsToExtract)
    removeCombiningMappings()
    vocabulary
  }

  private def analyzeSemantics(mapping: NodeMapping): Seq[CandidateProperty] =
    collectTermDuplicationsCases(mapping) ++ collectMultiTermCases(mapping)

  // This method resolve the problem of multiple properties at same level with the same term
  private def collectTermDuplicationsCases(mapping: NodeMapping): Seq[CandidateProperty] = {
    val finalProperties = mapping.propertiesMapping() ++ collectInheritsProperties(mapping)
    val termToPropsMap: Map[String, Seq[PropertyMapping]] = generateMapPropToTerms(finalProperties)
    val duplicatedTerms                                   = termToPropsMap.filter(_._2.size > 1)
    generateCandidates(duplicatedTerms)
  }

  private def generateMapPropToTerms(properties: Seq[PropertyMapping]): Map[String, Seq[PropertyMapping]] =
    properties
      .filter(_.nodePropertyMapping().option().nonEmpty)
      .groupBy(_.nodePropertyMapping().value())

  private def generateCandidates(duplicatedTerms: Map[String, Seq[PropertyMapping]]): Seq[CandidateProperty] =
    duplicatedTerms.flatMap { case (term, properties) =>
      properties.map(property => CandidateProperty(property, Seq(term)))
    }.toSeq

  // This method resolve the problem of properties with more than 1 term. This cases are collected in conversion
  private def collectMultiTermCases(mapping: NodeMapping): Seq[CandidateProperty] = multipleTerms

  private def collectInheritsProperties(mapping: NodeMapping): Seq[PropertyMapping] = mapping.extend.flatMap {
    case extension: NodeMapping if extension.isLink =>
      extension.effectiveLinkTarget().asInstanceOf[NodeMapping].propertiesMapping()
    case _ => Nil
  }

  private def calculateCombiningMappings(): Unit =
    new DialectCombiningMappingStage()
      .transform(dialect, new DefaultErrorHandler, AMLConfiguration.predefined())

  private def removeCombiningMappings(): Unit = {
    val clearDeclarations: Seq[DomainElement] = dialect.declares.filterNot {
      case declaration: NodeMapping => declaration.name.value().startsWith("CombiningMapping")
      case _                        => false
    }
    dialect.withDeclares(clearDeclarations)
  }

  private def generateVocabulary(termsToExtract: Seq[CandidateProperty]): Option[Vocabulary] =
    if (termsToExtract.isEmpty) None
    else {
      val counter: IdCounter = new IdCounter
      val visited: Visited   = new Visited // Necessary because a property could be duplicated in many combinations
      val terms: Seq[PropertyTerm] = termsToExtract.flatMap { case CandidateProperty(property, terms) =>
        if (!visited.alreadyVisited(property.id)) {
          val parentTerm = counter.genId(property.name().value())
          property.withNodePropertyMapping(options.vocabBase + parentTerm)
          val term = DatatypePropertyTerm()
            .withId(parentTerm)
            .withSubClassOf(terms)
          Some(term)
        } else None
      }

      val vocabulary = Vocabulary()
        .withId(options.vocabId)
        .withExternals(dialect.externals)
        .withBase(options.vocabBase)
        .withName(options.vocabName)
        .withDeclares(terms)

      Some(vocabulary)
    }

}

object VocabularyGenerator {
  def apply(dialect: Dialect, multipleTerms: Seq[CandidateProperty], options: SchemaTransformerOptions) =
    new VocabularyGenerator(dialect, multipleTerms, options)
}

class Visited {
  private val visited: mutable.Set[String] = mutable.Set()
  def alreadyVisited(element: String): Boolean =
    if (visited.contains(element)) true
    else {
      visited += element
      false
    }
}
