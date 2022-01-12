package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.NodeMappable
import amf.aml.internal.metamodel.domain.NodeMappableModel
import amf.aml.internal.render.emitters.common.IdCounter
import amf.core.client.scala.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.SemanticContext

import scala.collection.mutable

class ShapeTransformationContext(val shapeMap: mutable.Map[String, DomainElement] = mutable.Map(),
                                 val externals: mutable.Map[String, String] = mutable.Map(),
                                 val idCounter: IdCounter = new IdCounter,
                                 val shapeDeclarationNames: mutable.Set[String] = mutable.Set(),
                                 val semantics: SemanticContext = SemanticContext()) {

  // when we create it, we update the externals
  updateExternals()

  def transformed(): Seq[DomainElement] = shapeMap.values.toSeq

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
                                   semantics.merge(newSemantics).normalize())
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
