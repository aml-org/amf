package amf.core.rdf

import amf.core.annotations.DomainExtensionAnnotation
import amf.core.metamodel.Type.{Array, Bool, Iri, RegExp, SortedArray, Str}
import amf.core.metamodel.document.{BaseUnitModel, DocumentModel, SourceMapModel}
import amf.core.metamodel.domain._
import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.metamodel.{Field, ModelDefaultBuilder, Obj, Type}
import amf.core.model.document._
import amf.core.model.domain
import amf.core.model.domain._
import amf.core.model.domain.extensions.{CustomDomainProperty, DomainExtension}
import amf.core.parser.{Annotations, FieldEntry, ParserContext}
import amf.core.registries.AMFDomainRegistry
import amf.core.remote.Platform
import amf.core.vocabulary.Namespace
import amf.plugins.document.graph.parser.GraphParserHelpers
import amf.plugins.features.validation.ParserSideValidations.{UnableToParseNode, UnableToParseRdfDocument}
import org.mulesoft.common.time.SimpleDateTime

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class RdfModelParser(platform: Platform)(implicit val ctx: ParserContext) extends GraphParserHelpers {

  private val unresolvedReferences       = mutable.Map[String, Seq[DomainElement]]()
  private val unresolvedExtReferencesMap = mutable.Map[String, ExternalSourceElement]()

  private val referencesMap = mutable.Map[String, DomainElement]()

  private var collected: ListBuffer[Annotation] = ListBuffer()

  private var nodes: Map[String, AmfElement] = Map()
  private var graph: Option[RdfModel]        = None

  def parse(model: RdfModel, location: String): BaseUnit = {
    graph = Some(model)

    val unit = model.findNode(location) match {
      case Some(rootNode) =>
        parse(rootNode, findBaseUnit = true) match {
          case Some(unit: BaseUnit) => unit.set(BaseUnitModel.Location, location.split("#").head)
          case _ =>
            ctx.violation(UnableToParseRdfDocument,
                          location,
                          s"Unable to parse RDF model for location root node: $location")
            Document()
        }
      case _ =>
        ctx.violation(UnableToParseRdfDocument,
                      location,
                      s"Unable to parse RDF model for location root node: $location")
        Document()
    }

    // Resolve annotations after parsing entire graph
    collected.collect({ case r: ResolvableAnnotation => r }) foreach (_.resolve(nodes))

    unit
  }

  def parse(node: Node, findBaseUnit: Boolean = false): Option[AmfObject] = {
    val id = node.subject
    retrieveType(id, node, findBaseUnit) map { model =>
      val sources  = retrieveSources(id, node)
      val instance = buildType(model)(annots(sources, id))
      instance.withId(id)

      checkLinkables(instance)

      // workaround for lazy values in shape
      val modelFields = model match {
        case shapeModel: ShapeModel =>
          shapeModel.fields ++ Seq(
            ShapeModel.CustomShapePropertyDefinitions,
            ShapeModel.CustomShapeProperties
          )
        case _ => model.fields
      }

      modelFields.foreach(f => {
        val k          = f.value.iri()
        val properties = key(node, k)
        traverse(instance, f, properties, sources, k)
      })

      // parsing custom extensions
      instance match {
        case l: DomainElement with Linkable => parseLinkableProperties(node, l)
        case ex: ExternalDomainElement if unresolvedExtReferencesMap.get(ex.id).isDefined =>
          unresolvedExtReferencesMap.get(ex.id).foreach { element =>
            ex.raw.option().foreach(element.set(ExternalSourceElementModel.Raw, _))
          }
          unresolvedExtReferencesMap.remove(ex.id)
        case _ => // ignore
      }
      instance match {
        case elm: DomainElement => parseCustomProperties(node, elm)
        case _                  => // ignore
      }

      nodes = nodes + (id -> instance)
      instance
    }
  }

  protected def key(node: Node, property: String): Seq[PropertyObject] =
    node.getProperties(property).getOrElse(Nil)

  private def parseLinkableProperties(node: Node, instance: DomainElement with Linkable): Unit = {
    node
      .getProperties(LinkableElementModel.TargetId.value.iri())
      .flatMap(entries => {
        entries.headOption match {
          case Some(Uri(id)) => Some(id)
          case _             => None
        }
      })
      .foreach { targetId =>
        setLinkTarget(instance, targetId)
      }

    node
      .getProperties(LinkableElementModel.Label.value.iri())
      .flatMap(entries => {
        entries.headOption match {
          case Some(Literal(v, _)) => Some(v)
          case _                   => None
        }
      })
      .foreach(s => instance.withLinkLabel(s))
  }

  private def setLinkTarget(instance: DomainElement with Linkable, targetId: String) = {
    referencesMap.get(targetId) match {
      case Some(target) => instance.withLinkTarget(target)
      case None =>
        val unresolved: Seq[DomainElement] = unresolvedReferences.getOrElse(targetId, Nil)
        unresolvedReferences += (targetId -> (unresolved ++ Seq(instance)))
    }
  }

  def parseDynamicLiteral(l: Literal): ScalarNode = {
    val result = ScalarNode()
    result.value = l.value
    l.literalType.foreach(t => result.dataType = Some(t))
    result
  }

  def parseDynamicType(id: PropertyObject): Option[DataNode] = {
    findLink(id).map { node =>
      val sources = retrieveSources(id.value, node)
      val builder = retrieveDynamicType(node.subject, node).get

      builder(annots(sources, id.value)) match {
        case obj: ObjectNode =>
          obj.withId(node.subject)
          node.getKeys().foreach { uri =>
            if (uri != "@type" && uri != "@id" && uri != DomainElementModel.Sources.value.iri() &&
                uri != (Namespace.Document + "name").iri()) { // we do this to prevent parsing name of annotations

              val dataNode = node.getProperties(uri).get.head match {
                case l @ Literal(_, _) =>
                  parseDynamicLiteral(l)
                case entry if isRDFArray(entry) =>
                  parseDynamicArray(entry)
                case nestedNode @ Uri(_) =>
                  parseDynamicType(nestedNode).getOrElse(ObjectNode())
                case _ => ObjectNode()
              }
              obj.addProperty(uri, dataNode)
            }
          }
          obj

        case scalar: ScalarNode =>
          scalar.withId(node.subject)
          node.getKeys().foreach { k =>
            val entries = node.getProperties(k).get
            if (k == ScalarNodeModel.Value.value.iri() && entries.head.isInstanceOf[Literal]) {
              val parsedScalar = parseDynamicLiteral(entries.head.asInstanceOf[Literal])
              scalar.value = parsedScalar.value
              scalar.dataType = parsedScalar.dataType
            }
          }
          scalar

        case link: LinkNode =>
          link.withId(node.subject)
          node.getKeys().foreach { k =>
            val entries = node.getProperties(k).get
            if (k == LinkNodeModel.Alias.value.iri() && entries.head.isInstanceOf[Literal]) {
              val parsedScalar = parseDynamicLiteral(entries.head.asInstanceOf[Literal])
              link.alias = parsedScalar.value
            } else if (k == LinkNodeModel.Value.value.iri() && entries.head.isInstanceOf[Literal]) {
              val parsedScalar = parseDynamicLiteral(entries.head.asInstanceOf[Literal])
              link.value = parsedScalar.value
            }
          }
          referencesMap.get(link.alias) match {
            case Some(target) => link.withLinkedDomainElement(target)
            case _ =>
              val unresolved: Seq[DomainElement] = unresolvedReferences.getOrElse(link.alias, Nil)
              unresolvedReferences += (link.alias -> (unresolved ++ Seq(link)))
          }
          link

        case array: ArrayNode =>
          array.withId(node.subject)
          node.getKeys().foreach { k =>
            if (k == array.Member.value.iri()) {
              array.members = node.getProperties(k).getOrElse(Nil).flatMap(parseDynamicType).to[ListBuffer]
            }
          }
          array

        case other =>
          throw new Exception(s"Cannot parse object data node from non object JSON structure $other")
      }
    }
  }

  def isRDFArray(entry: PropertyObject): Boolean = {
    entry match {
      case id @ Uri(_) =>
        findLink(id) match {
          case Some(node) =>
            node.getProperties((Namespace.Rdf + "first").iri()).isDefined ||
              node.getProperties((Namespace.Rdf + "rest").iri()).isDefined
          case _ => false
        }
      case _ => false
    }
  }

  def parseDynamicArray(propertyObject: PropertyObject): ArrayNode = {
    val nodeAnnotations = findLink(propertyObject) match {
      case Some(node) =>
        val sources = retrieveSources(node.subject, node)
        annots(sources, node.subject)
      case None => Annotations()
    }
    val nodes = parseDynamicArrayInner(propertyObject)
    val array = ArrayNode(nodeAnnotations)
    nodes.foreach { array.addMember }
    array
  }

  def parseDynamicArrayInner(entry: PropertyObject, acc: Seq[DataNode] = Nil): Seq[DataNode] = {
    findLink(entry) match {
      case Some(n) =>
        val nextNode  = n.getProperties((Namespace.Rdf + "next").iri()).getOrElse(Nil).headOption
        val firstNode = n.getProperties((Namespace.Rdf + "first").iri()).getOrElse(Nil).headOption
        val updatedAcc = firstNode match {
          case Some(id @ Uri(_)) =>
            parseDynamicType(id) match {
              case Some(member) => acc ++ Seq(member)
              case _            => acc
            }
          case _ => acc
        }
        nextNode match {
          case Some(nextNodeProp @ Uri(id)) if id != (Namespace.Rdf + "nil").iri() =>
            parseDynamicArrayInner(nextNodeProp, updatedAcc)
          case _ =>
            updatedAcc
        }
      case None => acc
    }
  }

  private def traverse(instance: AmfObject,
                       f: Field,
                       properties: Seq[PropertyObject],
                       sources: SourceMap,
                       key: String) = {
    if (properties.nonEmpty) {
      val property = properties.head
      f.`type` match {
        case DataNodeModel => // dynamic nodes parsed here
          parseDynamicType(property) match {
            case Some(parsed) => instance.set(f, parsed, annots(sources, key))
            case _            =>
          }
        case _: Obj =>
          findLink(property) match {
            case Some(node) =>
              parse(node) match {
                case Some(parsed) =>
                  instance.set(f, parsed, annots(sources, key))
                  instance
                case _ => // ignore
              }
            case _ =>
              ctx.violation(
                UnableToParseRdfDocument,
                instance.id,
                s"Error parsing RDF graph node, unknown linked node for property $key in node ${instance.id}")
          }

        case Iri           => instance.set(f, strCoercion(property), annots(sources, key))
        case Str | RegExp  => instance.set(f, str(property), annots(sources, key))
        case Bool          => instance.set(f, bool(property), annots(sources, key))
        case Type.Int      => instance.set(f, int(property), annots(sources, key))
        case Type.Float    => instance.set(f, float(property), annots(sources, key))
        case Type.Double   => instance.set(f, double(property), annots(sources, key))
        case Type.DateTime => instance.set(f, date(property), annots(sources, key))
        case Type.Date     => instance.set(f, date(property), annots(sources, key))
        case Type.Any      => instance.set(f, any(property), annots(sources, key))
        case l: SortedArray if properties.length == 1 =>
          instance.setArray(f, parseList(instance.id, l.element, findLink(properties.head)), annots(sources, key))
        case _: SortedArray =>
          ctx.violation(
            UnableToParseRdfDocument,
            instance.id,
            s"Error, more than one sorted array values found in node for property $key in node ${instance.id}")
        case a: Array =>
          val items = properties
          val values: Seq[AmfElement] = a.element match {
            case _: Obj =>
              val shouldParseUnit = f.value.iri() == (Namespace.Document + "references")
                .iri() // this is for self-encoded documents
              items.flatMap(n =>
                findLink(n) match {
                  case Some(o) => parse(o, shouldParseUnit)
                  case _       => None
              })
            case Str | Iri => items.map(n => strCoercion(n))
          }
          a.element match {
            case _: DomainElementModel if f == DocumentModel.Declares =>
              instance.setArrayWithoutId(f, values, annots(sources, key))
            case _: BaseUnitModel => instance.setArrayWithoutId(f, values, annots(sources, key))
            case _                => instance.setArrayWithoutId(f, values, annots(sources, key))
          }
      }
    } else {
      // ignore
    }
  }

  private def parseList(id: String, listElement: Type, maybeCollection: Option[Node]): Seq[AmfElement] = {
    val buffer = ListBuffer[PropertyObject]()
    maybeCollection.foreach { collection =>
      collection.getKeys().foreach { entry =>
        if (entry.startsWith((Namespace.Rdfs + "_").iri())) {
          buffer ++= collection.getProperties(entry).get
        }
      }
    }

    val res = buffer.map { n =>
      listElement match {
        case DataNodeModel => // dynamic nodes parsed here
          parseDynamicType(n)
        case _: Obj =>
          findLink(n) match {
            case Some(node) => parse(node)
            case _          => None
          }
        case Str | RegExp | Iri => try { Some(strCoercion(n)) } catch { case _: Exception => None }
        case Bool               => try { Some(bool(n)) } catch { case _: Exception => None }
        case Type.Int           => try { Some(int(n)) } catch { case _: Exception => None }
        case Type.Float         => try { Some(float(n)) } catch { case _: Exception => None }
        case Type.Double        => try { Some(double(n)) } catch { case _: Exception => None }
        case Type.Date          => try { Some(date(n)) } catch { case _: Exception => None }
        case Type.Any           => try { Some(any(n)) } catch { case _: Exception => None }
        case _                  => throw new Exception(s"Unknown list element type: ${listElement}")
      }
    }
    res collect { case Some(x) => x }
  }

  private def findLink(property: PropertyObject) = {
    property match {
      case Uri(v) => graph.flatMap(_.findNode(v))
      case _      => None
    }
  }

  private def checkLinkables(instance: AmfObject): Unit = {
    instance match {
      case link: DomainElement with Linkable =>
        referencesMap += (link.id -> link)
        unresolvedReferences.getOrElse(link.id, Nil).foreach {
          case unresolved: Linkable =>
            unresolved.withLinkTarget(link)
          case unresolved: LinkNode =>
            unresolved.withLinkedDomainElement(link)
          case _ => throw new Exception("Only linkable elements can be linked")
        }
        unresolvedReferences.update(link.id, Nil)
      case ref: ExternalSourceElement =>
        unresolvedExtReferencesMap += (ref.referenceId.value -> ref) // process when parse the references node
      case _ => // ignore
    }
  }

  private def isUnitModel(typeModel: Obj): Boolean =
    typeModel.isInstanceOf[DocumentModel] || typeModel.isInstanceOf[EncodesModel] || typeModel
      .isInstanceOf[DeclaresModel] || typeModel.isInstanceOf[BaseUnitModel]

  private def retrieveType(id: String, node: Node, findBaseUnit: Boolean = false): Option[Obj] = {
    val stringTypes = ts(node, ctx, id)
    val foundType = stringTypes.find { t =>
      val maybeFoundType = findType(t)
      // this is just for self-encoding documents
      maybeFoundType match {
        case Some(typeModel) if !findBaseUnit && !isUnitModel(typeModel) => true
        case Some(typeModel) if findBaseUnit && isUnitModel(typeModel)   => true
        case _                                                           => false
      }
    }
    foundType match {
      case Some(t) => findType(t)
      case None =>
        ctx.violation(UnableToParseNode,
                      id,
                      s"Error parsing JSON-LD node, unknown @types $stringTypes",
                      ctx.rootContextDocument)
        None
    }
  }

  def retrieveDynamicType(id: String, node: Node): Option[Annotations => AmfObject] = {
    ts(node, ctx, id).find({ t =>
      dynamicBuilders.get(t).isDefined
    }) match {
      case Some(t) => Some(dynamicBuilders(t))
      case _       => None
    }
  }

  private def types: Map[String, Obj] = AMFDomainRegistry.metadataRegistry.toMap

  private def findType(typeString: String): Option[Obj] = {
    types.get(typeString).orElse(AMFDomainRegistry.findType(typeString))
  }

  private val deferredTypesSet = Set(
    (Namespace.Document + "Document").iri(),
    (Namespace.Document + "Fragment").iri(),
    (Namespace.Document + "Module").iri(),
    (Namespace.Document + "Unit").iri(),
    (Namespace.Shacl + "Shape").iri(),
    (Namespace.Shapes + "Shape").iri()
  )

  private def ts(node: Node, ctx: ParserContext, id: String): Seq[String] = {
    node.classes.partition(deferredTypesSet.contains) match {
      case (deferred, others) => others ++ deferred.sorted // we just use the fact that lexical order is correct
    }
  }

  private def buildType(modelType: Obj): Annotations => AmfObject = {
    AMFDomainRegistry.metadataRegistry.get(modelType.`type`.head.iri()) match {
      case Some(modelType: ModelDefaultBuilder) =>
        (annotations: Annotations) =>
          val instance = modelType.modelInstance
          instance.annotations ++= annotations
          instance
      case _ =>
        AMFDomainRegistry.buildType(modelType) match {
          case Some(builder) => builder
          case _             => throw new Exception(s"Cannot find builder for node type $modelType")
        }
    }
  }

  private def strCoercion(property: PropertyObject) = AmfScalar(s"${property.value}")

  private val xsdString: String   = (Namespace.Xsd + "string").iri()
  private val xsdInteger: String  = (Namespace.Xsd + "integer").iri()
  private val xsdFloat: String    = (Namespace.Xsd + "float").iri()
  private val amlNumber: String   = (Namespace.Shapes + "number").iri()
  private val xsdDouble: String   = (Namespace.Xsd + "double").iri()
  private val xsdBoolean: String  = (Namespace.Xsd + "boolean").iri()
  private val xsdDateTime: String = (Namespace.Xsd + "dateTime").iri()
  private val xsdDate: String     = (Namespace.Xsd + "date").iri()

  private def any(property: PropertyObject) = {
    property match {
      case Literal(v, typed) =>
        typed match {
          case Some(s: String) if s == xsdBoolean  => AmfScalar(v.toBoolean)
          case Some(s: String) if s == xsdInteger  => AmfScalar(v.toInt)
          case Some(s: String) if s == xsdFloat    => AmfScalar(v.toFloat)
          case Some(s: String) if s == xsdDouble   => AmfScalar(v.toDouble)
          case Some(s: String) if s == xsdDateTime => AmfScalar(SimpleDateTime.parse(v).right.get)
          case Some(s: String) if s == xsdDate     => AmfScalar(SimpleDateTime.parse(v).right.get)
          case _                                   => AmfScalar(v)
        }
      case Uri(v) => throw new Exception(s"Expecting String literal found URI $v")
    }
  }

  private def str(property: PropertyObject) = {
    property match {
      case Literal(v, _) => AmfScalar(v)
      case Uri(v) => {
        throw new Exception(s"Expecting String literal found URI $v")
      }
    }
  }

  private def bool(property: PropertyObject) = {
    property match {
      case Literal(v, _) => AmfScalar(v.toBoolean)
      case Uri(v)        => throw new Exception(s"Expecting Boolean literal found URI $v")
    }
  }

  private def int(property: PropertyObject) = {
    property match {
      case Literal(v, _) => AmfScalar(v.toInt)
      case Uri(v)        => throw new Exception(s"Expecting Int literal found URI $v")
    }
  }

  private def double(property: PropertyObject) = {
    property match {
      case Literal(v, _) => AmfScalar(v.toDouble)
      case Uri(v)        => throw new Exception(s"Expecting Double literal found URI $v")
    }
  }

  private def date(property: PropertyObject) = {
    property match {
      case Literal(v, _) =>
        SimpleDateTime.parse(v) match {
          case Right(value) => AmfScalar(value)
          case Left(error)  => throw new Exception(error.message)
        }
      case Uri(v) => throw new Exception(s"Expecting Date literal found URI $v")
    }
  }

  private def float(property: PropertyObject) = {
    property match {
      case Literal(v, _) => AmfScalar(v.toFloat)
      case Uri(v)        => throw new Exception(s"Expecting Float literal found URI $v")
    }
  }

  private val dynamicBuilders: mutable.Map[String, Annotations => AmfObject] = mutable.Map(
    LinkNode.builderType.iri()        -> domain.LinkNode.apply,
    ArrayNode.builderType.iri()       -> domain.ArrayNode.apply,
    ScalarNodeModel.`type`.head.iri() -> domain.ScalarNode.apply,
    ObjectNode.builderType.iri()      -> domain.ObjectNode.apply
  )

  protected def retrieveSources(id: String, node: Node): SourceMap = {
    node
      .getProperties(DomainElementModel.Sources.value.iri())
      .flatMap { properties =>
        if (properties.nonEmpty) {
          findLink(properties.head) match {
            case Some(sourceNode) => Some(parseSourceNode(sourceNode))
            case _                => None
          }
        } else {
          None
        }
      }
      .getOrElse(SourceMap.empty)
  }

  private def parseSourceNode(node: Node): SourceMap = {
    val result = SourceMap()
    node.getKeys().foreach {
      case key @ AnnotationName(annotation) =>
        val consumer = result.annotation(annotation)
        node.getProperties(key) match {
          case Some(properties) =>
            properties.foreach { property =>
              findLink(property) match {
                case Some(linkedNode) =>
                  val k: PropertyObject = linkedNode.getProperties(SourceMapModel.Element.value.iri()).get.head
                  val v: PropertyObject = linkedNode.getProperties(SourceMapModel.Value.value.iri()).get.head
                  consumer(k.value, v.value)
                case _ => //
              }
            }

          case _ => // ignore
        }
      case _ => // Unknown annotation identifier
    }
    result
  }

  def parseCustomProperties(node: Node, instance: DomainElement): Unit = {
    val properties: Seq[String] = node
      .getProperties(DomainElementModel.CustomDomainProperties.value.iri())
      .getOrElse(Nil)
      .filter(_.isInstanceOf[Uri])
      .map(_.asInstanceOf[Uri].value)

    val extensions: Seq[DomainExtension] = properties.flatMap { uri =>
      node
        .getProperties(uri)
        .map(entries => {
          val extension = DomainExtension()
          if (entries.nonEmpty) {
            findLink(entries.head) match {
              case Some(obj) =>
                obj.getProperties(DomainExtensionModel.Name.value.iri()) match {
                  case Some(es) if es.nonEmpty && es.head.isInstanceOf[Literal] =>
                    extension.withName(value(DomainExtensionModel.Name.`type`, es.head.asInstanceOf[Literal].value))
                  case _ => // ignore
                }

                obj.getProperties(DomainExtensionModel.Element.value.iri()) match {
                  case Some(es) if es.nonEmpty && es.head.isInstanceOf[Literal] =>
                    extension.withName(value(DomainExtensionModel.Element.`type`, es.head.asInstanceOf[Literal].value))
                  case _ => // ignore
                }

                val definition = CustomDomainProperty()
                definition.id = uri
                extension.withDefinedBy(definition)

                parseDynamicType(entries.head).foreach { pn =>
                  extension.withId(pn.id)
                  extension.withExtension(pn)
                }

                val sources = retrieveSources(extension.id, node)
                extension.annotations ++= annots(sources, extension.id)

              case _ => // ignore
            }
          }
          extension
        })
    }

    if (extensions.nonEmpty) {
      extensions.partition(_.isScalarExtension) match {
        case (scalars, objects) =>
          instance.withCustomDomainProperties(objects)
          applyScalarDomainProperties(instance, scalars)
      }
    }
  }

  private def applyScalarDomainProperties(instance: DomainElement, scalars: Seq[DomainExtension]): Unit = {
    scalars.foreach { e =>
      instance.fields
        .fieldsMeta()
        .find(f => e.element.is(f.value.iri()))
        .foreach(f => {
          instance.fields.entry(f).foreach {
            case FieldEntry(_, value) => value.value.annotations += DomainExtensionAnnotation(e)
          }
        })
    }
  }

  private def annots(sources: SourceMap, key: String) =
    annotations(nodes, sources, key).into(collected, _.isInstanceOf[ResolvableAnnotation])
}
