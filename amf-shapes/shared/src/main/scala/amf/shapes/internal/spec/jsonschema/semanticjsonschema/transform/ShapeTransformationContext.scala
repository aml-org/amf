package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.NodeMappable
import amf.aml.internal.metamodel.domain.NodeMappableModel
import amf.aml.internal.render.emitters.common.IdCounter
import amf.core.client.scala.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.{CuriePrefix, SemanticContext}

import scala.collection.mutable

class ShapeTransformationContext(
    val shapeMap: mutable.Map[String, DomainElement] = mutable.Map(),
    val externals: mutable.Map[String, String] = mutable.Map(),
    val idCounter: IdCounter = new IdCounter,
    val shapeDeclarationNames: mutable.Set[String] = mutable.Set(),
    val semantics: SemanticContext = SemanticContext(),
    val termsToExtract: mutable.Set[CandidateProperty] = mutable.Set()
) {

  // when we create it, we update the externals
  updateExternals()

  def transformed(): List[DomainElement] = shapeMap.values.toList

  def registerNodeMapping[T <: NodeMappableModel](nodeMapping: NodeMappable[T]): Unit =
    shapeMap(nodeMapping.id) = nodeMapping

  def genName[T <: NodeMappableModel](nodeMapping: NodeMappable[T]): NodeMappable[T] = {
    val nodeMappingName = nodeMapping.name.option().getOrElse("SchemaNode")
    val name =
      if (shapeDeclarationNames.contains(nodeMappingName))
        idCounter.genId(nodeMappingName)
      else nodeMappingName
    nodeMapping.withName(name)
    shapeDeclarationNames.add(nodeMapping.name.value())
    nodeMapping
  }

  def updateSemanticContext(newSemantics: SemanticContext): ShapeTransformationContext = {
    new ShapeTransformationContext(
      shapeMap,
      externals,
      idCounter,
      shapeDeclarationNames,
      semantics.merge(newSemantics).normalize(),
      termsToExtract
    )
  }

  private def updateExternals(): Unit = {
    semantics.vocab.flatMap(_.iri.option()).map(addToExternals)

    semantics.curies.foreach { curie =>
      if (externals.contains(curie.alias.value()) && externals(curie.alias.value()) != curie.iri.value()) {
        val nextKey = computeExternalKey(curie)
        externals += (nextKey -> curie.iri.value())
      } else {
        externals += (curie.alias.value() -> curie.iri.value())
      }
    }

    semantics.typeMappings.foreach { mapping =>
      val iri = iriBase(mapping.value())
      addToExternals(iri)
    }

    semantics.mapping
      .flatMap(_.iri.option())
      .map { iri =>
        val adaptedIri = iriBase(iri)
        addToExternals(adaptedIri)
      }
  }

  private def computeExternalKey(curie: CuriePrefix) = {
    s"${curie.alias.value()}${externals.keys.count(k => k.startsWith(curie.alias.value()))}"
  }

  private def addToExternals(iri: String) = {
    if (!externals.values.toSeq.contains(iri)) {
      val nextKey = computeExternalKey
      externals += (nextKey -> iri)
    }
  }

  private def computeExternalKey = s"ns${externals.keys.count(k => k.startsWith("ns"))}"

  private def iriBase(iri: String): String = {
    if (iri.contains("#")) {
      iri.split("#").head + "#"
    } else {
      val parts = iri.split("/")
      parts.dropRight(1).mkString("/") + "/"
    }
  }
}

object ShapeTransformationContext {
  def apply(options: SchemaTransformerOptions): ShapeTransformationContext = createContext(options)

  private def createContext(options: SchemaTransformerOptions) = {
    val semanticContext = SemanticContext().withId("context").withCuries(Seq(auxiliaryVocabCurie(options)))
    val ctx             = new ShapeTransformationContext(semantics = semanticContext)
    ctx
  }

  private def auxiliaryVocabCurie(options: SchemaTransformerOptions): CuriePrefix = {
    CuriePrefix()
      .withId(s"${options.termPrefix}_default")
      .withAlias(options.termPrefix)
      .withIri(options.vocabBase)
  }
}
