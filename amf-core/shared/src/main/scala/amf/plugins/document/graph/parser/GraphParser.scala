package amf.plugins.document.graph.parser

import amf.core.annotations.DomainExtensionAnnotation
import amf.core.metamodel.Type.{Array, Bool, Iri, RegExp, SortedArray, Str}
import amf.core.metamodel.document.BaseUnitModel.Location
import amf.core.metamodel.document._
import amf.core.metamodel.domain._
import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.metamodel._
import amf.core.model.document._
import amf.core.model.domain._
import amf.core.model.domain.extensions.{CustomDomainProperty, DomainExtension}
import amf.core.parser.{Annotations, _}
import amf.core.registries.AMFDomainRegistry
import amf.core.remote.Platform
import amf.core.unsafe.TrunkPlatform
import amf.core.vocabulary.Namespace
import amf.plugins.features.validation.ParserSideValidations.{
  NodeNotFound,
  NotLinkable,
  UnableToParseDocument,
  UnableToParseNode
}
import org.mulesoft.common.time.SimpleDateTime
import org.yaml.convert.YRead.SeqNodeYRead
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * AMF Graph parser
  */
class GraphParser(platform: Platform)(implicit val ctx: ParserContext) extends GraphParserHelpers {

  def parse(document: YDocument, location: String): BaseUnit = {
    val parser = Parser(Map())
    parser.parse(document, location)
  }

  case class Parser(var nodes: Map[String, AmfElement]) {
    private val unresolvedReferences       = mutable.Map[String, Seq[DomainElement]]()
    private val unresolvedExtReferencesMap = mutable.Map[String, ExternalSourceElement]()

    private val referencesMap = mutable.Map[String, DomainElement]()

    val dynamicGraphParser = new DynamicGraphParser(nodes, referencesMap, unresolvedReferences)

    def parse(document: YDocument, location: String): BaseUnit = {
      val maybeMaps        = document.node.toOption[Seq[YMap]]
      val maybeMap         = maybeMaps.flatMap(s => s.headOption)
      val maybeMaybeObject = maybeMap.flatMap(parse)

      maybeMaybeObject match {
        case Some(unit: BaseUnit) => unit.set(Location, location)
        case _ =>
          ctx.violation(UnableToParseDocument, location, s"Unable to parse $document", document)
          Document()
      }
    }

    private def retrieveType(id: String, map: YMap): Option[Obj] = {
      val stringTypes = ts(map, ctx, id)
      stringTypes.find(findType(_).isDefined) match {
        case Some(t) => findType(t)
        case None =>
          ctx.violation(UnableToParseNode, id, s"Error parsing JSON-LD node, unknown @types $stringTypes", map)
          None
      }
    }

    private def parseList(id: String, listElement: Type, node: YMap): Seq[AmfElement] = {
      val buffer = ListBuffer[YNode]()
      node.entries.sortBy(_.key.as[String]).foreach { entry =>
        if (entry.key.as[String].startsWith((Namespace.Rdfs + "_").iri())) {
          buffer += entry.value.as[Seq[YNode]].head
        }
      }
      buffer.flatMap { n =>
        listElement match {
          case _: DynamicObj => dynamicGraphParser.parseDynamicType(n.as[YMap])
          case _: Obj        => parse(n.as[YMap])
          case _             => try { Some(str(value(listElement, n))) } catch { case _: Exception => None }
        }
      }
    }

    private def parse(map: YMap): Option[AmfObject] = { // todo fix uses
      retrieveId(map, ctx)
        .flatMap(value => retrieveType(value, map).map(value2 => (value, value2)))
        .flatMap {
          case (id, model) =>
            val sources = retrieveSources(id, map)
            buildType(id, map, model)(annotations(nodes, sources, id)) match {
              case Some(builder) =>
                val instance: AmfObject = builder
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
                  val k = f.value.iri()
                  map.key(k) match {
                    case Some(entry) => traverse(instance, f, value(f.`type`, entry.value), sources, k)
                    case _           =>
                  }
                })

                // parsing custom extensions
                instance match {
                  case l: DomainElement with Linkable => parseLinkableProperties(map, l)
                  case ex: ExternalDomainElement if unresolvedExtReferencesMap.get(ex.id).isDefined =>
                    unresolvedExtReferencesMap.get(ex.id).foreach { element =>
                      ex.raw.option().foreach(element.set(ExternalSourceElementModel.Raw, _))
                    }
                    unresolvedExtReferencesMap.remove(ex.id)
                  case _ => // ignore
                }
                instance match {
                  case elm: DomainElement => parseCustomProperties(map, elm)
                  case _                  => // ignore
                }

                nodes = nodes + (id -> instance)
                Some(instance)
              case _ => None
            }

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
            case _ =>
              ctx.violation(NotLinkable, instance.id, "Only linkable elements can be linked", instance.annotations)
          }
          unresolvedReferences.update(link.id, Nil)
        case ref: ExternalSourceElement =>
          unresolvedExtReferencesMap += (ref.referenceId.value -> ref) // process when parse the references node
        case _ => // ignore
      }
    }

    private def setLinkTarget(instance: DomainElement with Linkable, targetId: String) = {
      referencesMap.get(targetId) match {
        case Some(target) => instance.withLinkTarget(target)
        case None =>
          val unresolved: Seq[DomainElement] = unresolvedReferences.getOrElse(targetId, Nil)
          unresolvedReferences += (targetId -> (unresolved ++ Seq(instance)))
      }
    }

    private def parseLinkableProperties(map: YMap, instance: DomainElement with Linkable): Unit = {
      map
        .key(LinkableElementModel.TargetId.value.iri())
        .flatMap(entry => {
          retrieveId(entry.value.as[Seq[YMap]].head, ctx)
        })
        .foreach { targetId =>
          setLinkTarget(instance, targetId)
        }

      map
        .key(LinkableElementModel.Label.value.iri())
        .flatMap(entry => {
          entry.value
            .toOption[Seq[YNode]]
            .flatMap(nodes => nodes.head.toOption[YMap])
            .flatMap(map => map.key("@value"))
            .flatMap(_.value.toOption[YScalar].map(_.text))
        })
        .foreach(s => instance.withLinkLabel(s))
    }

    private def parseCustomProperties(map: YMap, instance: DomainElement): Unit = {
      val properties = map
        .key(DomainElementModel.CustomDomainProperties.value.iri())
        .map(_.value.as[Seq[YNode]].map(value(Iri, _).as[YScalar].text))
        .getOrElse(Nil)

      val extensions = properties
        .flatMap { uri =>
          map
            .key(uri)
            .map(entry => {
              val extension = DomainExtension()
              val obj       = entry.value.as[YMap]

              parseScalarProperty(obj, DomainExtensionModel.Name).map(extension.withName)
              parseScalarProperty(obj, DomainExtensionModel.Element).map(extension.withElement)

              val definition = CustomDomainProperty()
              definition.id = uri
              extension.withDefinedBy(definition)

              dynamicGraphParser.parseDynamicType(obj).foreach { pn =>
                extension.withId(pn.id)
                extension.withExtension(pn)
              }

              val sources = retrieveSources(extension.id, map)
              extension.annotations ++= annotations(nodes, sources, extension.id)

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

    private def traverse(instance: AmfObject, f: Field, node: YNode, sources: SourceMap, key: String) = {
      f.`type` match {
        case DataNodeModel => // dynamic nodes parsed here
          dynamicGraphParser.parseDynamicType(node.as[YMap]) match {
            case Some(parsed) => instance.set(f, parsed, annotations(nodes, sources, key))
            case _            =>
          }
        case _: Obj =>
          parse(node.as[YMap]).foreach(n => instance.set(f, n, annotations(nodes, sources, key)))
          instance
        case Str | RegExp | Iri => instance.set(f, str(node), annotations(nodes, sources, key))
        case Bool               => instance.set(f, bool(node), annotations(nodes, sources, key))
        case Type.Int           => instance.set(f, int(node), annotations(nodes, sources, key))
        case Type.Float         => instance.set(f, float(node), annotations(nodes, sources, key))
        case Type.Double        => instance.set(f, double(node), annotations(nodes, sources, key))
        case Type.DateTime      => instance.set(f, date(node), annotations(nodes, sources, key))
        case Type.Date          => instance.set(f, date(node), annotations(nodes, sources, key))
        case Type.Any           => instance.set(f, any(node), annotations(nodes, sources, key))
        case l: SortedArray =>
          instance.setArray(f, parseList(instance.id, l.element, node.as[YMap]), annotations(nodes, sources, key))
        case a: Array =>
          val items = node.as[Seq[YNode]]
          val values: Seq[AmfElement] = a.element match {
            case _: Obj    => items.flatMap(n => parse(n.as[YMap]))
            case Str | Iri => items.map(n => str(value(a.element, n)))
          }
          a.element match {
            case _: DomainElementModel if f == DocumentModel.Declares =>
              instance.setArrayWithoutId(f, values, annotations(nodes, sources, key))
            case _: BaseUnitModel => instance.setArrayWithoutId(f, values, annotations(nodes, sources, key))
            case _                => instance.setArray(f, values, annotations(nodes, sources, key))
          }
      }
    }
  }

  private def parseScalarProperty(definition: YMap, field: Field) =
    definition
      .key(field.value.iri())
      .map(entry => value(field.`type`, entry.value).as[YScalar].text)

  private def str(node: YNode) = {
    val value = node.tagType match {
      case YType.Map =>
        node.as[YMap].entries.find(_.key.as[String] == "@value") match {
          case Some(entry) => entry.value.as[YScalar].text
          case _           => node.as[YScalar].text
        }
      case _ => node.as[YScalar].text
    }
    AmfScalar(value)
  }

  private def bool(node: YNode) = {
    val value = node.tagType match {
      case YType.Map =>
        node.as[YMap].entries.find(_.key.as[String] == "@value") match {
          case Some(entry) => entry.value.as[YScalar].text.toBoolean
          case _           => node.as[YScalar].text.toBoolean
        }
      case _ => node.as[YScalar].text.toBoolean
    }
    AmfScalar(value)
  }

  private def int(node: YNode) = {
    val value = node.tagType match {
      case YType.Map =>
        node.as[YMap].entries.find(_.key.as[String] == "@value") match {
          case Some(entry) => entry.value.as[YScalar].text.toInt
          case _           => node.as[YScalar].text.toInt
        }
      case _ => node.as[YScalar].text.toInt
    }
    AmfScalar(value)
  }

  private def double(node: YNode) = {
    val value = node.tagType match {
      case YType.Map =>
        node.as[YMap].entries.find(_.key.as[String] == "@value") match {
          case Some(entry) => entry.value.as[YScalar].text.toDouble
          case _           => node.as[YScalar].text.toDouble
        }
      case _ => node.as[YScalar].text.toDouble
    }
    AmfScalar(value)
  }

  private def date(node: YNode) = {
    val value = node.tagType match {
      case YType.Map =>
        node.as[YMap].entries.find(_.key.as[String] == "@value") match {
          case Some(entry) => SimpleDateTime.parse(entry.value.as[YScalar].text).right.get
          case _           => SimpleDateTime.parse(node.as[YScalar].text).right.get
        }
      case _ => SimpleDateTime.parse(node.as[YScalar].text).right.get
    }
    AmfScalar(value)
  }

  private val xsdString: String   = (Namespace.Xsd + "string").iri()
  private val xsdInteger: String  = (Namespace.Xsd + "integer").iri()
  private val xsdFloat: String    = (Namespace.Xsd + "float").iri()
  private val amlNumber: String   = (Namespace.Shapes + "number").iri()
  private val xsdDouble: String   = (Namespace.Xsd + "double").iri()
  private val xsdBoolean: String  = (Namespace.Xsd + "boolean").iri()
  private val xsdDateTime: String = (Namespace.Xsd + "dateTime").iri()
  private val xsdDate: String     = (Namespace.Xsd + "date").iri()

  private def any(node: YNode) = {
    node.tagType match {
      case YType.Map =>
        val nodeValue = node.as[YMap].entries.find(_.key.as[String] == "@value") match {
          case Some(entry) => entry.value.as[YScalar].text
          case _           => node.as[YScalar].text
        }
        node.as[YMap].entries.find(_.key.as[String] == "@type") match {
          case Some(typeEntry) =>
            val typeUri = typeEntry.value.as[YScalar].text
            typeUri match {
              case s: String if s == xsdBoolean  => AmfScalar(nodeValue.toBoolean)
              case s: String if s == xsdInteger  => AmfScalar(nodeValue.toInt)
              case s: String if s == xsdFloat    => AmfScalar(nodeValue.toFloat)
              case s: String if s == xsdDouble   => AmfScalar(nodeValue.toDouble)
              case s: String if s == xsdDateTime => AmfScalar(SimpleDateTime.parse(nodeValue).right.get)
              case s: String if s == xsdDate     => AmfScalar(SimpleDateTime.parse(nodeValue).right.get)
              case _                             => AmfScalar(nodeValue)
            }
          case _ => AmfScalar(nodeValue)
        }
      case _ => AmfScalar(node.as[YScalar].text)
    }
  }

  private def float(node: YNode) = {
    val value = node.tagType match {
      case YType.Map =>
        node.as[YMap].entries.find(_.key.as[String] == "@value") match {
          case Some(entry) =>
            entry.value.as[YScalar].text.toDouble
          case _ => node.as[YScalar].text.toDouble
        }
      case _ => node.as[YScalar].text.toDouble
    }
    AmfScalar(value)
  }

  private val types: Map[String, Obj] = Map.empty ++ AMFDomainRegistry.metadataRegistry

  private def findType(typeString: String): Option[Obj] = {
    types.get(typeString).orElse(AMFDomainRegistry.findType(typeString))
  }

  private def buildType(id: String, map: YMap, modelType: Obj): Annotations => Option[AmfObject] = {
    AMFDomainRegistry.metadataRegistry.get(modelType.`type`.head.iri()) match {
      case Some(modelType: ModelDefaultBuilder) =>
        (annotations: Annotations) =>
          val instance = modelType.modelInstance
          instance.annotations ++= annotations
          Some(instance)
      case _ =>
        AMFDomainRegistry.buildType(modelType) match {
          case Some(builder) =>
            (a: Annotations) =>
              Some(builder(a))
          case _ =>
            ctx.violation(NodeNotFound, id, s"Cannot find builder for node type $modelType", map)
            (_: Annotations) =>
              None
        }
    }
  }
}

object GraphParser {
  def apply: GraphParser                     = GraphParser(TrunkPlatform(""))
  def apply(platform: Platform): GraphParser = new GraphParser(platform)(ParserContext())
}
