package amf.document

import amf.domain.DomainElement
import amf.model.{AmfArray, AmfElement, AmfObject}
import amf.vocabulary.ValueType

import scala.collection.mutable.ListBuffer

/** Any parseable unit, backed by a source URI. */
trait BaseUnit extends AmfObject {

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  def references: Seq[BaseUnit]

  /** Returns the file location for the document that has been parsed to generate this model */
  def location: String

  /** Returns the usage comment for de element */
  def usage: String

  /**
    * finds in the nested model structure an AmfObject with the requested Id
    * @param id URI of the model element
    */
  def findById(id: String): Option[DomainElement] = {
    val predicate = (element: DomainElement) => element.id == id
    findInEncodedModel(predicate, this, first = true).headOption.orElse(
      findInDeclaredModel(predicate, this, first = true, ListBuffer.empty).headOption.orElse(
        findInReferencedModels(id, this.references).headOption
      )
    )
  }


  // Private lookup methods

  private def findInEncodedModel(predicate:(DomainElement) => Boolean, encoder: BaseUnit, first: Boolean = false, acc: ListBuffer[DomainElement] = ListBuffer.empty: ListBuffer[DomainElement]) = {
    encoder match {
      case encoder: EncodesModel => findModelByCondition(predicate, encoder.encodes, first, acc)
      case _                     => ListBuffer.empty
    }
  }

  private def findInDeclaredModel(predicate:(DomainElement) => Boolean, encoder: BaseUnit, first: Boolean, acc: ListBuffer[DomainElement]): ListBuffer[DomainElement] = {
    encoder match {
      case encoder: DeclaresModel => findModelByConditionInSeq(predicate, encoder.declares, first, acc)
      case _                      => ListBuffer.empty
    }
  }

  private def findInReferencedModels(id: String, units: Seq[BaseUnit]): ListBuffer[DomainElement] = {
    if (units.isEmpty) {
      ListBuffer.empty
    } else {
      units.head.findById(id) match {
        case Some(element) => ListBuffer(element)
        case None          => findInReferencedModels(id, units.tail)
      }
    }
  }

  private def findModelByCondition(predicate:(DomainElement) => Boolean, element: DomainElement, first: Boolean, acc: ListBuffer[DomainElement]): ListBuffer[DomainElement] = {
    val found = predicate(element)
    if (found) { acc += element }
    if (found && first) {
      acc
    } else {
      findModelByConditionInSeq(predicate, element.fields.fields().map(_.element).toSeq, first, acc)
    }
  }

  private def findModelByConditionInSeq(predicate:(DomainElement) => Boolean, elements: Seq[AmfElement], first: Boolean, acc: ListBuffer[DomainElement]): ListBuffer[DomainElement] = {
    if (elements.isEmpty) {
      acc
    } else {
      elements.head match {
        case obj: DomainElement =>
          val res = findModelByCondition(predicate, obj, first, acc)
          if (first && res.nonEmpty) {
            res
          } else {
            findModelByConditionInSeq(predicate, elements.tail, first, res)
          }

        case arr: AmfArray =>
          val res = findModelByConditionInSeq(predicate, arr.values, first, acc)
          if (first && res.nonEmpty) {
            res
          } else {
            findModelByConditionInSeq(predicate, elements.tail, first, res)
          }

        case _ => findModelByConditionInSeq(predicate, elements.tail, first, acc)
      }
    }
  }

}