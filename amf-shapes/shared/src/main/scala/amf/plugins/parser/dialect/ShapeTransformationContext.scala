package amf.plugins.parser.dialect

import amf.core.model.domain.DomainElement
import amf.plugins.document.vocabularies.model.domain.NodeMapping
import amf.plugins.domain.shapes.models.SemanticContext

import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable

class ShapeTransformationContext(val shapeMap: mutable.Map[String, DomainElement] = mutable.Map(),
                                 val idCounter: AtomicReference[Int] = new AtomicReference(0),
                                 val shapeDeclarationNames: mutable.Set[String] = mutable.Set(),
                                 val semantics: SemanticContext = SemanticContext()) {

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
    new ShapeTransformationContext(shapeMap, idCounter, shapeDeclarationNames, semantics.merge(newSemantics))
  }
}

object ShapeTransformationContext {
  def apply(): ShapeTransformationContext = new ShapeTransformationContext()
}