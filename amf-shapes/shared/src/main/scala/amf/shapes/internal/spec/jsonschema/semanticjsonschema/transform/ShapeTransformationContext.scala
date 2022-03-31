package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.document.Vocabulary
import amf.aml.client.scala.model.domain.{DatatypePropertyTerm, External, NodeMappable}
import amf.aml.internal.metamodel.domain.NodeMappableModel
import amf.aml.internal.render.emitters.common.IdCounter
import amf.core.client.scala.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.{CuriePrefix, SemanticContext}
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform.SemanticOps.getPrefixes

import scala.collection.mutable

class VocabularyBuilder(private val extractedTermPrefix: String) {
  private var vocabExtensions: Map[String, List[String]] = Map()
  private var propertyCounters: Map[String, IdCounter]   = Map()
  private var prefixes: Set[String]                      = Set()

  def withVocabExtension(propertyName: String, characteristics: List[String]): String = {
    (vocabExtensions.get(propertyName), propertyCounters.get(propertyName)) match {
      case (Some(_), Some(counter)) =>
        val name = counter.genId(propertyName)
        vocabExtensions += (name -> characteristics)
        name
      case (Some(_), _) =>
        val counter = new IdCounter()
        val name    = counter.genId(propertyName)
        vocabExtensions += (name          -> characteristics)
        propertyCounters += (propertyName -> counter)
        name
      case (None, _) =>
        vocabExtensions += propertyName  -> characteristics
        propertyCounters += propertyName -> new IdCounter()
        propertyName
    }
  }

  def withPrefixes(prefixes: Set[String]) = this.prefixes = this.prefixes ++ prefixes

  def loadIriSet(propertyName: String, iris: Set[String], ctx: SemanticContext): String = {
    val prefixes = getPrefixes(iris)
    withPrefixes(prefixes)
    val iriTerm = ctx.expand(s"$extractedTermPrefix:$propertyName")
    withVocabExtension(iriTerm, iris.map(ctx.expand).toList)
  }

  def isEmpty: Boolean     = vocabExtensions.isEmpty
  def shouldBuild: Boolean = vocabExtensions.nonEmpty

  def build(id: String, base: String, name: String, externals: Map[String, String]): Vocabulary = {
    val terms = vocabExtensions.map {
      case (term, subProperties) =>
        DatatypePropertyTerm()
          .withId(term)
          .withSubClassOf(subProperties)
    }.toList
    val externalObjs = externals
      .filter(x => prefixes.contains(x._1))
      .map(entry => External().withId(entry._1).withAlias(entry._1).withBase(entry._2))
    Vocabulary()
      .withId(id)
      .withExternals(externalObjs.toList)
      .withBase(base)
      .withName(name)
      .withDeclares(terms)
  }
}

class ShapeTransformationContext(val shapeMap: mutable.Map[String, DomainElement] = mutable.Map(),
                                 val externals: mutable.Map[String, String] = mutable.Map(),
                                 val idCounter: IdCounter = new IdCounter,
                                 val shapeDeclarationNames: mutable.Set[String] = mutable.Set(),
                                 val semantics: SemanticContext = SemanticContext(),
                                 val vocabBuilder: VocabularyBuilder,
                                 val options: SchemaTransformerOptions) {

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
    new ShapeTransformationContext(shapeMap,
                                   externals,
                                   idCounter,
                                   shapeDeclarationNames,
                                   semantics.merge(newSemantics).normalize(),
                                   vocabBuilder,
                                   options)
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
    val ctx = new ShapeTransformationContext(semantics = semanticContext,
                                             vocabBuilder = new VocabularyBuilder(options.termPrefix),
                                             options = options)
    ctx
  }

  private def auxiliaryVocabCurie(options: SchemaTransformerOptions): CuriePrefix = {
    CuriePrefix()
      .withId(s"${options.termPrefix}_default")
      .withAlias(options.termPrefix)
      .withIri(options.vocabBase)
  }
}
