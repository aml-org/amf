package amf.plugins.parser.dialect

import amf.core.model.domain.DomainElement
import amf.plugins.document.vocabularies.model.domain.NodeMapping
import amf.plugins.domain.shapes.models.SemanticContext

import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable

class ShapeTransformationContext(val shapeMap: mutable.Map[String, DomainElement] = mutable.Map(),
                                 val externals: mutable.Map[String, String] = mutable.Map(),
                                 val idCounter: AtomicReference[Int] = new AtomicReference(0),
                                 val shapeDeclarationNames: mutable.Set[String] = mutable.Set(),
                                 val semantics: SemanticContext = SemanticContext()) {

  // when we create it, we update the externals
  updateExternals()

  def transformed(): Seq[DomainElement] = {
    shapeMap.values.toSeq
  }

  def registerNodeMapping(nodeMapping: NodeMapping): Unit = {
    shapeMap(nodeMapping.id) = nodeMapping
  }

  def genName(nodeMapping: NodeMapping): NodeMapping = {
    val nodeMappingName = nodeMapping.name.option().getOrElse("SchemaNode")
    val name =  if (shapeDeclarationNames.contains(nodeMappingName)) {
      s"${nodeMappingName}_${idCounter.updateAndGet((v) => v + 1)}"
    } else {
      nodeMappingName
    }
    nodeMapping.withName(name)
    shapeDeclarationNames.add(nodeMapping.name.value())
    nodeMapping
  }

  def updateSemanticContext(newSemantics: SemanticContext): ShapeTransformationContext = {
    new ShapeTransformationContext(shapeMap, externals, idCounter, shapeDeclarationNames, semantics.merge(newSemantics))
  }

  private def updateExternals(): Unit = {
    semantics.vocab.foreach { vocab =>
      vocab.iri.option().foreach { iri =>
        if (!externals.values.toSeq.contains(iri)) {
          val nextKey = s"ns${externals.keys.count(k => k.startsWith("ns"))}"
          externals += (nextKey -> iri)
        }
      }
    }

    semantics.curies.foreach { curie =>
      if (externals.contains(curie.alias.value()) && externals(curie.alias.value()) != curie.iri.value()) {
        val nextKey = s"${curie.alias.value()}${externals.keys.count(k => k.startsWith(curie.alias.value()))}"
        externals += (nextKey -> curie.iri.value())
      } else {
        externals += (curie.alias.value() -> curie.iri.value())
      }
    }

    semantics.typeMappings.foreach { mapping =>
      val iri = iriBase(mapping.value())
      if (!externals.values.toSeq.contains(iri)) {
        val nextKey = s"ns${externals.keys.count(k => k.startsWith("ns"))}"
        externals += (nextKey -> iri)
      }
    }

    semantics.mapping.foreach { mapping =>
      mapping.iri.option().foreach { mappingIri =>
        val iri = iriBase(mappingIri)
        if (!externals.values.toSeq.contains(iri)) {
          val nextKey = s"ns${externals.keys.count(k => k.startsWith("ns"))}"
          externals += (nextKey -> iri)
        }
      }
    }
  }

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
  def apply(): ShapeTransformationContext = new ShapeTransformationContext()
}