package amf.shapes.internal.spec.contexts.emitter.oas

import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.internal.utils.IdCounter
import amf.shapes.internal.spec.contexts.emitter.oas.AliasDefinitions.{Id, Label}

import scala.collection.mutable
import scala.util.matching.Regex

trait CompactableEmissionContext {
  //  regex used to validate if the name of the shape is a valid label for referencing and declaring in definitions
  def nameRegex: Regex = """^[^/]+$""".r

  val definitionsQueue: DefinitionsQueue = DefinitionsQueue()(this)

  var _forceEmission: Option[String] = None
  def forceEmission: Option[String]  = _forceEmission

  def setForceEmission(id: Option[String]): Unit = {
    _forceEmission = id
  }

  def removeForceEmission = {
    _forceEmission = None
  }

  // oas emission emits schemas to the definitions, so we need the schemas to emit all their examples
  def filterLocal[T <: DomainElement](elements: Seq[T]): Seq[T] = elements
}

case class DefinitionsQueue(
    pendingEmission: mutable.Queue[LabeledShape] = new mutable.Queue(),
    queuedIdsWithLabel: mutable.Map[Id, Label] = mutable.Map[String, String]())(ctx: CompactableEmissionContext) {

  def enqueue(shape: Shape): String =
    queuedIdsWithLabel.getOrElse( // if the shape has already been queued the assigned label is returned
      shape.id, {
        val label        = createLabel(shape)
        val labeledShape = LabeledShape(label, shape)
        pendingEmission += labeledShape
        queuedIdsWithLabel += labeledShape.shape.id -> labeledShape.label
        labeledShape.label
      }
    )

  def labelOfShape(id: String): Option[String] = queuedIdsWithLabel.get(id)
  def nonEmpty(): Boolean                      = pendingEmission.nonEmpty
  def dequeue(): LabeledShape                  = pendingEmission.dequeue

  val counter = new IdCounter()

  private def createLabel(shape: Shape): String = {
    val name: String = normalizeName(shape.name.option())
    if (queuedIdsWithLabel.valuesIterator.contains(name)) counter.genId(name) else name
  }
  def normalizeName(name: Option[String]): String = name.filter(isValidName).getOrElse("default")
  private def isValidName(s: String): Boolean     = ctx.nameRegex.pattern.matcher(s).matches()
}

case class LabeledShape(label: String, shape: Shape)

object AliasDefinitions {
  type Label = String
  type Id    = String
}
