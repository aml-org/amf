package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.document.Vocabulary
import amf.aml.client.scala.model.domain.{NodeMapping, PropertyMapping, UnionNodeMapping}
import amf.aml.internal.metamodel.domain.NodeMappableModel
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.{AnyShape, CuriePrefix, SemanticContext}
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform.SchemaTransformerOptions.DEFAULT
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform.SemanticOps.{expandIri, findPrefix}

import scala.collection.mutable

case class TransformationResult(encoded: DomainElement,
                                declared: Seq[DomainElement],
                                externals: Map[String, String],
                                vocab: Option[Vocabulary] = None)

object SchemaTransformerOptions {
  val DEFAULT = SchemaTransformerOptions("semantic_vocabulary",
                                         "https://a.ml/semantic-json-schema#",
                                         "Semantic JSON Schema Vocabulary",
                                         "semantics")
}

case class SchemaTransformerOptions(vocabId: String, vocabBase: String, vocabName: String, termPrefix: String)

case class SchemaTransformer(shape: AnyShape, options: SchemaTransformerOptions)(
    implicit errorHandler: AMFErrorHandler) {

  def transform[T <: NodeMappableModel](): TransformationResult = {
    val ctx         = ShapeTransformationContext(options)
    val transformed = ShapeTransformation(shape, ctx).transform()
    val fixer       = DuplicateTermFixer(ctx.vocabBuilder, ctx.externals.toMap, DEFAULT.termPrefix)
    val declared    = ctx.transformed()
    declared.map(fixer.fix)
    val vocab   = createVocab(ctx)
    val aliases = getDialectExternals(ctx.externals.toMap, vocab)
    TransformationResult(transformed, declared, aliases, vocab)
  }

  private def getDialectExternals(externals: Map[String, String], vocab: Option[Vocabulary]): Map[String, String] = {
    vocab.fold(externals.filterKeys(prefix => prefix != options.termPrefix)) { _ =>
      externals
    }
  }

  private def createVocab(ctx: ShapeTransformationContext) = {
    if (ctx.vocabBuilder.shouldBuild)
      Some(ctx.vocabBuilder.build(options.vocabId, options.vocabBase, options.vocabName, ctx.externals.toMap))
    else None
  }
}

case class DuplicateTermFixer(vocabBuilder: VocabularyBuilder, externals: Map[String, String], vocabPrefix: String) {
  def fix[T <: DomainElement](element: T): DomainElement = {
    element match {
      case node: NodeMapping => fixDuplicatePropertyTerms(node)
      case other             => other
    }
  }

  private def fixDuplicatePropertyTerms(mapping: NodeMapping): NodeMapping = {
    val mappingsByTerm = detectDuplicateTerms(mapping)
    extractDuplicateTermsToHierarchy(mappingsByTerm)
    mapping
  }

  private def extractDuplicateTermsToHierarchy(mappingsByTerm: Map[String, Seq[PropertyMapping]]): Unit = {
    mappingsByTerm.foreach {
      case (expandedTerm, mappings) =>
        mappings.foreach { mapping =>
          loadPrefix(expandedTerm)
          val expandedIri    = SemanticOps.expandIri(s"$vocabPrefix:${mapping.name().value()}", externals, None)
          val registeredTerm = vocabBuilder.withVocabExtension(expandedIri, List(expandedTerm))
          mapping.withNodePropertyMapping(registeredTerm)
        }
    }
  }

  private def loadPrefix(term: String): Unit = prefixOf(term).foreach(prefix => vocabBuilder.withPrefixes(Set(prefix)))

  private def detectDuplicateTerms(mapping: NodeMapping) = {
    mapping
      .propertiesMapping()
      .flatMap { p =>
        p.nodePropertyMapping().option().map(term => term -> p)
      }
      .groupBy { case (term, _) => term }
      .mapValues(_.map(_._2))
      .filter { case (_, properties) => properties.size > 1 }
  }

  private def prefixOf(term: String): Option[String] = findPrefix(term, externals)
}
