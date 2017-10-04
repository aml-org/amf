package amf.document

import amf.domain.DomainElement
import amf.metadata.document.DocumentModel.{Declares, References}
import amf.metadata.document.{BaseUnitModel, FragmentModel}
import amf.model.{AmfArray, AmfElement, AmfObject}

/** Any parseable unit, backed by a source URI. */
trait BaseUnit extends AmfObject {

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  def references: Seq[BaseUnit]

  /** Returns the file location for the document that has been parsed to generate this model */
  def location: String

  /** Returns the usage comment for de element */
  def usage: String

  def withReferences(references: Seq[BaseUnit]): this.type = setArrayWithoutId(References, references)

  def withLocation(location: String): this.type = set(BaseUnitModel.Location, location)

  def withUsage(usage: String): this.type = set(BaseUnitModel.Usage, usage)

  /**
    * finds in the nested model structure an AmfObject with the requested Id
    * @param id URI of the model element
    */
  def findById(id: String): Option[DomainElement] = {
    findInEncodedModel(id, this) match {
      case None  => findInDeclaredModel(id, this)
      case found => found
    }
  }

  // Private lookup methods

  private def findInEncodedModel(id: String, encoder: BaseUnit): Option[DomainElement] = {
    encoder match {
      case encoder: EncodesModel => findModelById(id, encoder.encodes)
      case _                     => None
    }
  }

  private def findInDeclaredModel(id: String, encoder: BaseUnit): Option[DomainElement] = {
    encoder match {
      case encoder: DeclaresModel => findModelByIdInSeq(id, encoder.declares)
      case _                      => None
    }
  }

  private def findModelById(id: String, element: DomainElement): Option[DomainElement] = {
    if (element.id == id) {
      Some(element)
    } else {
      findModelByIdInSeq(id, element.fields.fields().map(_.element).toSeq)
    }
  }

  private def findModelByIdInSeq(id: String, elements: Seq[AmfElement]): Option[DomainElement] = {
    if (elements.isEmpty) {
      None
    } else {
      elements.head match {
        case obj: DomainElement =>
          findModelById(id, obj) match {
            case None         => findModelByIdInSeq(id, elements.tail)
            case foundElement => foundElement
          }

        case arr: AmfArray =>
          findModelByIdInSeq(id, arr.values) match {
            case None         => findModelByIdInSeq(id, elements.tail)
            case foundElement => foundElement
          }

        case _ => findModelByIdInSeq(id, elements.tail)
      }
    }
  }

}
