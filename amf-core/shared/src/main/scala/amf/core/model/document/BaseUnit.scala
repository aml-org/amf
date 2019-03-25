package amf.core.model.document

import amf.core.annotations.SourceVendor
import amf.core.emitter.RenderOptions
import amf.core.metamodel.document.BaseUnitModel.{Location, Usage}
import amf.core.metamodel.document.DocumentModel
import amf.core.metamodel.document.DocumentModel.References
import amf.core.metamodel.domain.DomainElementModel
import amf.core.metamodel.{DynamicObj, MetaModelTypeMapping, Obj}
import amf.core.model.StrField
import amf.core.model.domain._
import amf.core.parser.{DefaultParserSideErrorHandler, ErrorHandler, FieldEntry, ParserContext, Value}
import amf.core.rdf.{RdfModel, RdfModelParser}
import amf.core.remote.Vendor
import amf.core.unsafe.PlatformSecrets
import amf.plugins.features.validation.ResolutionSideValidations.RecursiveShapeSpecification

import scala.collection.mutable.ListBuffer

/** Any parseable unit, backed by a source URI. */
trait BaseUnit extends AmfObject with MetaModelTypeMapping with PlatformSecrets {

  // We store the parser run here to be able to find runtime validations for this model
  var parserRun: Option[Int] = None

  /** Raw text  used to generated this unit */
  var raw: Option[String] = None

  /** Meta data for the document */
  def meta: Obj

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  def references: Seq[BaseUnit]

  /** Returns the file location for the document that has been parsed to generate this model */
  override def location(): Option[String] = {
    val fieldValue: StrField = fields.field(Location)
    fieldValue.option().orElse(super.location())
  }

  /** Returns the usage. */
  def usage: StrField = fields.field(Usage)

  /** Set the raw value for the base unit */
  def withRaw(raw: String): BaseUnit = {
    this.raw = Some(raw)
    this
  }

  def withReferences(references: Seq[BaseUnit]): this.type = setArrayWithoutId(References, references)

  def withLocation(location: String): this.type = set(Location, location)

  def withUsage(usage: String): this.type = set(Usage, usage)

  /**
    * finds in the nested model structure an AmfObject with the requested Id
    * @param id URI of the model element
    */
  def findById(id: String): Option[DomainElement] = findById(id, Set.empty)
  def findById(id: String, cycles: Set[String]): Option[DomainElement] = {
    val predicate = (element: DomainElement) => element.id == id
    findInEncodedModel(predicate, this, first = true, ListBuffer.empty, cycles).headOption.orElse(
      findInDeclaredModel(predicate, this, first = true, ListBuffer.empty, cycles).headOption.orElse(
        findInReferencedModels(id, this.references, cycles).headOption
      )
    )
  }

  /** Finds in the nested model structure AmfObjects with the requested types. */
  def findByType(shapeType: String, cycles: Set[String] = Set.empty): Seq[DomainElement] = {
    val predicate = { element: DomainElement =>
      metaModel(element).`type`.map(t => t.iri()).contains(shapeType)
    }
    findInDeclaredModel(predicate, this, first = false, ListBuffer.empty, cycles) ++
      findInEncodedModel(predicate, this, first = false, ListBuffer.empty, cycles)
  }

  def findBy(predicate: DomainElement => Boolean, cycles: Set[String] = Set.empty): Seq[DomainElement] = {
    findInDeclaredModel(predicate, this, first = false, ListBuffer.empty, cycles) ++
      findInEncodedModel(predicate, this, first = false, ListBuffer.empty, cycles)
  }

  def transform(selector: DomainElement => Boolean, transformation: (DomainElement, Boolean) => Option[DomainElement])(
      implicit errorHandler: ErrorHandler): BaseUnit = {
    val domainElementAdapter = (o: AmfObject) => {
      o match {
        case e: DomainElement => selector(e)
        case _                => false
      }
    }
    val transformationAdapter = (o: AmfObject, isCycle: Boolean) => {
      o match {
        case e: DomainElement => transformation(e, isCycle)
        case _                => Some(o)
      }
    }
    transformByCondition(this,
                         domainElementAdapter,
                         transformationAdapter,
                         cycleRecoverer = defaultCycleRecoverer(errorHandler))
    this
  }

  // Private lookup methods

  protected def findInEncodedModel(predicate: DomainElement => Boolean,
                                   encoder: BaseUnit,
                                   first: Boolean = false,
                                   acc: ListBuffer[DomainElement] = ListBuffer.empty: ListBuffer[DomainElement],
                                   cycles: Set[String]): ListBuffer[_ <: DomainElement] = {
    encoder match {
      case _ if cycles.contains(encoder.id) => ListBuffer.empty
      case encoder: EncodesModel if Option(encoder.encodes).isDefined =>
        findModelByCondition(predicate, encoder.encodes, first, acc, cycles + encoder.id)
      case _ => ListBuffer.empty
    }
  }

  protected def findInDeclaredModel(predicate: DomainElement => Boolean,
                                    encoder: BaseUnit,
                                    first: Boolean,
                                    acc: ListBuffer[DomainElement],
                                    cycles: Set[String]): ListBuffer[DomainElement] = {
    encoder match {
      case _ if cycles.contains(encoder.id) => ListBuffer.empty
      case encoder: DeclaresModel =>
        findModelByConditionInSeq(predicate, encoder.declares, first, acc, cycles + encoder.id)
      case _ => ListBuffer.empty
    }
  }

  def findInReferences(id: String): Option[BaseUnit] = references.find(_.id == id)

  protected def findInReferencedModels(id: String,
                                       units: Seq[BaseUnit],
                                       cycles: Set[String]): ListBuffer[DomainElement] = {
    if (units.isEmpty) {
      ListBuffer.empty
    } else if (cycles.contains(units.head.id)) {
      findInReferencedModels(id, units.tail, cycles)
    } else {
      units.head.findById(id, cycles) match {
        case Some(element) => ListBuffer(element)
        case None          => findInReferencedModels(id, units.tail, cycles + units.head.id)
      }
    }
  }

  protected def findModelByCondition(predicate: DomainElement => Boolean,
                                     element: DomainElement,
                                     first: Boolean,
                                     acc: ListBuffer[DomainElement],
                                     cycles: Set[String]): ListBuffer[DomainElement] = {
    if (!cycles.contains(element.id)) {
      val found = predicate(element)
      if (found) {
        acc += element
      }
      if (found && first) {
        acc
      } else {
        // elements are the values in the properties for the not found object
        val elements = element.meta match {
          case _: DynamicObj =>
            val values =
              (element.meta.fields :+ DomainElementModel.CustomDomainProperties)
                .flatMap(f => element.asInstanceOf[DynamicDomainElement].valueForField(f))
                .map(_.value)
            val effectiveValues = values.map {
              case d: DomainElement => Seq(d) // set(
              case a: AmfArray =>
                a.values.filter(_.isInstanceOf[DomainElement]).asInstanceOf[Seq[DomainElement]] // setArray(
              case _ => Seq() // ignore literals
            }
            effectiveValues.flatten
          case _ =>
            element.fields.fields().map(_.element).toSeq
        }
        findModelByConditionInSeq(predicate, elements, first, acc, cycles + element.id)
      }
    } else {
      acc
    }
  }

  private def findModelByConditionInSeq(predicate: DomainElement => Boolean,
                                        elements: Seq[AmfElement],
                                        first: Boolean,
                                        acc: ListBuffer[DomainElement],
                                        cycles: Set[String]): ListBuffer[DomainElement] = {
    if (elements.isEmpty) {
      acc
    } else {
      elements.head match {
        case obj: DomainElement if !cycles.contains(obj.id) =>
          val res = findModelByCondition(predicate, obj, first, acc, cycles) // must not be added to cycles here, findModelByCondition will do it
          if (first && res.nonEmpty) {
            res
          } else {
            findModelByConditionInSeq(predicate, elements.tail, first, res, cycles)
          }

        case _: DomainElement => findModelByConditionInSeq(predicate, elements.tail, first, acc, cycles)

        case arr: AmfArray =>
          val res = findModelByConditionInSeq(predicate, arr.values, first, acc, cycles)
          if (first && res.nonEmpty) {
            res
          } else {
            findModelByConditionInSeq(predicate, elements.tail, first, res, cycles)
          }

        case _ => findModelByConditionInSeq(predicate, elements.tail, first, acc, cycles)
      }
    }
  }

  protected def defaultCycleRecoverer(errorHandler: ErrorHandler = DefaultParserSideErrorHandler(this))(
      old: AmfObject,
      transformed: AmfObject): Option[AmfObject] = {
    transformed match {
      case s: Shape =>
        Some(RecursiveShape(s))
      case _ =>
        errorHandler.violation(
          RecursiveShapeSpecification,
          old.id,
          s"Recursive loop generated in reference expansion: ${old.id} => ${transformed.id}",
          old.annotations
        )
        None
    }
  }

  protected def transformByCondition(element: AmfObject,
                                     predicate: AmfObject => Boolean,
                                     transformation: (AmfObject, Boolean) => Option[AmfObject],
                                     cycles: Set[String] = Set.empty,
                                     cycleRecoverer: (AmfObject, AmfObject) => Option[AmfObject]): AmfObject = {
    if (!cycles.contains(element.id)) {
      // not visited yet
      if (predicate(element)) { // matches predicate, we transform
        transformation(element, false) match {
          case Some(transformed: AmfObject) =>
            if (cycles.contains(transformed.id)) {
              cycleRecoverer(element, transformed).orNull
            } else {
              transformed
            }
          case other => other.orNull
        }
      } else {
        // not matches the predicate, we traverse

        element match {
          case dataNode: ObjectNode =>
            dataNode.properties.foreach {
              case (prop, value) =>
                Option(transformByCondition(value, predicate, transformation, cycles + element.id, cycleRecoverer)) match {
                  case Some(transformed: DataNode) =>
                    dataNode.properties.put(prop, transformed)
                    dataNode
                  case Some(_) => dataNode
                  case _ =>
                    dataNode.properties.remove(prop)
                    dataNode
                }
            }

          case arrayNode: ArrayNode =>
            arrayNode.members = arrayNode.members
              .flatMap { elem: DataNode =>
                Option(
                  transformByCondition(elem,
                                       predicate,
                                       transformation,
                                       cycles + element.id,
                                       cycleRecoverer = cycleRecoverer))
              }
              .map(_.adopted(arrayNode.id))
              .collect { case d: DataNode => d }

          case _ =>
            // we first process declarations, then the encoding
            val effectiveFields: Iterable[FieldEntry] = element match {
              case doc: DeclaresModel =>
                doc.fields.fields().filter(f => f.field == DocumentModel.References) ++
                  doc.fields.fields().filter(f => f.field == DocumentModel.Declares) ++
                  doc.fields
                    .fields()
                    .filter(f => f.field != DocumentModel.Declares && f.field != DocumentModel.References)
              case _ => element.fields.fields()
            }
            effectiveFields
              .map { entry =>
                (entry.field, entry.value)
              }
              .foreach {
                case (f, v: Value) if v.value.isInstanceOf[AmfObject] =>
                  Option(
                    transformByCondition(v.value.asInstanceOf[AmfObject],
                                         predicate,
                                         transformation,
                                         cycles + element.id,
                                         cycleRecoverer = cycleRecoverer)) match {
                    case Some(transformedValue: AmfObject) =>
                      element.fields.setWithoutId(f, transformedValue)
                      element match {
                        case s: Shape if transformedValue.isInstanceOf[RecursiveShape] =>
                          transformedValue
                            .asInstanceOf[RecursiveShape]
                            .fixpointTarget
                            .foreach(t => s.closureShapes += t)
                        case _ => // ignore
                      }
                    case Some(_) => // ignore
                    case None    => element.fields.removeField(f)
                  }
                case (f, v: Value) if v.value.isInstanceOf[AmfArray] =>
                  val newElements = v.value
                    .asInstanceOf[AmfArray]
                    .values
                    .map {
                      case elem: AmfObject =>
                        val transformedValue =
                          transformByCondition(elem,
                                               predicate,
                                               transformation,
                                               cycles + element.id,
                                               cycleRecoverer = cycleRecoverer)
                        element match {
                          case s: Shape if transformedValue.isInstanceOf[RecursiveShape] =>
                            transformedValue
                              .asInstanceOf[RecursiveShape]
                              .fixpointTarget
                              .foreach(t => s.closureShapes += t)
                          case _ => // ignore
                        }
                        Some(transformedValue)
                      case other =>
                        Some(other)
                    }
                    .filter(_.isDefined)
                    .map(_.get)
                  element.fields.setWithoutId(f, AmfArray(newElements), v.annotations)

                case _ => // ignore
              }
        }
        element
      }

    } else
      element match {
        // target of the link has been traversed, we still visit the link in case a transformer wants to
        // transform links/references, but we will not traverse to avoid loops
        case linkable: Linkable if linkable.isLink =>
          if (predicate(element)) {
            transformation(element, true).orNull // passing the cycle boolean flat!
          } else {
            element
          }
        // traversed and not visited
        case _ => element
      }
  }

  def toNativeRdfModel(renderOptions: RenderOptions = new RenderOptions()): RdfModel = {
    platform.rdfFramework match {
      case Some(rdf) => rdf.unitToRdfModel(this, renderOptions)
      case None      => throw new Exception("RDF Framework not registered cannot export to native RDF model")
    }
  }

  def sourceVendor: Option[Vendor] = this match {
    case e: EncodesModel if Option(e.encodes).isDefined =>
      e.encodes.annotations.find(classOf[SourceVendor]).map(a => a.vendor)
    case d: DeclaresModel => d.annotations.find(classOf[SourceVendor]).map(a => a.vendor)
    case _                => None
  }
}

object BaseUnit extends PlatformSecrets {
  def fromNativeRdfModel(id: String, rdfModel: RdfModel, ctx: ParserContext = ParserContext()): BaseUnit =
    new RdfModelParser(platform)(ctx).parse(rdfModel, id)
}
