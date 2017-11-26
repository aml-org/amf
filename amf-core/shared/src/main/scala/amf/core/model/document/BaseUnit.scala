package amf.core.model.document

import amf.core.metamodel.document.BaseUnitModel
import amf.core.metamodel.document.DocumentModel.References
import amf.core.metamodel.{MetaModelTypeMapping, Obj}
import amf.core.model.domain._
import amf.core.parser.Value

import scala.collection.mutable.ListBuffer

/** Any parseable unit, backed by a source URI. */
trait BaseUnit extends AmfObject with MetaModelTypeMapping {

  /** Meta data for the document */
  def meta: Obj

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  def references: Seq[BaseUnit]

  /** Returns the file location for the document that has been parsed to generate this model */
  def location: String

  /** Returns the usage comment for de element */
  def usage: String

  def withReferences(references: Seq[BaseUnit]): this.type = setArrayWithoutId(References, references)

  def withLocation(location: String): this.type = set(BaseUnitModel.Location, location)

  def withUsage(usage: String): this.type = set(BaseUnitModel.Usage, usage)

  // TODO: This has become dissapear now, use the domain plugin @modularization
  /** Resolves the model. */
  // def resolve(profile: String): this.type // = ResolutionPipeline.forProfile(profile).resolve(this)

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

  /** Finds in the nested model structure AmfObjects with the requested types. */
  def findByType(shapeType: String): Seq[DomainElement] = {
    val predicate = { (element: DomainElement) =>
      val types = element match {
        case e: DynamicDomainElement =>
          e.dynamicType.map(t => t.iri()) ++ element.dynamicTypes() ++ metaModel(element).`type`.map(t => t.iri())
        case _ => element.dynamicTypes() ++ metaModel(element).`type`.map(t => t.iri())
      }
      types.contains(shapeType)
    }
    findInDeclaredModel(predicate, this, first = false, ListBuffer.empty) ++ findInEncodedModel(predicate,
                                                                                                this,
                                                                                                first = false)
  }

  def findBy(predicate: (DomainElement) => Boolean): Seq[DomainElement] = {
    findInDeclaredModel(predicate, this, first = false, ListBuffer.empty) ++
      findInEncodedModel(predicate, this, first = false)
  }

  def transform(selector: (DomainElement) => Boolean,
                transformation: (DomainElement) => Option[DomainElement]): BaseUnit = {
    val domainElementAdapter = (o: AmfObject) => {
      o match {
        case e: DomainElement => selector(e)
        case _                => false
      }
    }
    val transformationAdapter = (o: AmfObject) => {
      o match {
        case e: DomainElement => transformation(e)
        case _                => Some(o)
      }
    }
    transformByCondition(this, domainElementAdapter, transformationAdapter)
    this
  }

  // Private lookup methods

  private def findInEncodedModel(predicate: (DomainElement) => Boolean,
                                 encoder: BaseUnit,
                                 first: Boolean = false,
                                 acc: ListBuffer[DomainElement] = ListBuffer.empty: ListBuffer[DomainElement]) = {
    encoder match {
      case encoder: EncodesModel if Option(encoder.encodes).isDefined =>
        findModelByCondition(predicate, encoder.encodes, first, acc)
      case _ => ListBuffer.empty
    }
  }

  private def findInDeclaredModel(predicate: (DomainElement) => Boolean,
                                  encoder: BaseUnit,
                                  first: Boolean,
                                  acc: ListBuffer[DomainElement]): ListBuffer[DomainElement] = {
    encoder match {
      case encoder: DeclaresModel => findModelByConditionInSeq(predicate, encoder.declares, first, acc)
      case _                      => ListBuffer.empty
    }
  }

  def findInReferences(id: String): Option[BaseUnit] = references.find(_.id == id)

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

  private def findModelByCondition(predicate: (DomainElement) => Boolean,
                                   element: DomainElement,
                                   first: Boolean,
                                   acc: ListBuffer[DomainElement]): ListBuffer[DomainElement] = {
    val found = predicate(element)
    if (found) { acc += element }
    if (found && first) {
      acc
    } else {
      val elements = element match {
        case dynamicElement: DynamicDomainElement =>
          val values =
            dynamicElement.dynamicFields.map(f => dynamicElement.valueForField(f)).filter(_.isDefined).map(_.get)
          values.filter(v => v.isInstanceOf[DomainElement]).asInstanceOf[Seq[DomainElement]]
        case _ =>
          element.fields.fields().map(_.element).toSeq
      }
      findModelByConditionInSeq(predicate, elements, first, acc)
    }
  }

  private def findModelByConditionInSeq(predicate: (DomainElement) => Boolean,
                                        elements: Seq[AmfElement],
                                        first: Boolean,
                                        acc: ListBuffer[DomainElement]): ListBuffer[DomainElement] = {
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

  private def transformByCondition(element: AmfObject,
                                   predicate: (AmfObject) => Boolean,
                                   transformation: (AmfObject) => Option[AmfObject]): AmfObject = {
    if (predicate(element)) {
      transformation(element).orNull
    } else {
      element.fields.foreach {
        case (f, v: Value) if v.value.isInstanceOf[AmfObject] =>
          Option(transformByCondition(v.value.asInstanceOf[AmfObject], predicate, transformation)) match {
            case Some(transformedValue: AmfObject) => element.fields.setWithoutId(f, transformedValue)
            case Some(_)                           => // ignore
            case None                              => element.fields.remove(f)
          }
        case (f, v: Value) if v.value.isInstanceOf[AmfArray] =>
          val newElements = v.value
            .asInstanceOf[AmfArray]
            .values
            .map {
              case elem: AmfObject =>
                Option(transformByCondition(elem, predicate, transformation))
              case other =>
                Some(other)
            }
            .filter(_.isDefined)
            .map(_.get)
          element.fields.setWithoutId(f, AmfArray(newElements), v.annotations)
        case _ => // ignore
      }
      element
    }
  }

}
