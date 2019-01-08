package amf.plugins.document.graph.parser

import amf.core.metamodel.Type.ObjType
import amf.core.metamodel.domain.{DomainElementModel, ScalarNodeModel}
import amf.core.model.domain
import amf.core.model.domain._
import amf.core.parser.{Annotations, ParserContext, YMapOps}
import amf.core.vocabulary.Namespace
import amf.plugins.features.validation.ParserSideValidations.UnableToParseNode
import org.yaml.model.{YMap, YNode}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Parser that can take a dynamic portion of a model encoded as a generic
  * JSON-Ld graph and reconstruct the model
  * @param nodes entry nodes for the parser
  * @param ctx parsing context
  */
class DynamicGraphParser(var nodes: Map[String, AmfElement],
                         referencesMap: mutable.Map[String, DomainElement],
                         unresolvedReferences: mutable.Map[String, Seq[DomainElement]])(implicit ctx: ParserContext)
    extends GraphParserHelpers {

  /**
    * Finds the type of dynamic node model to build based on the JSON-LD @type information
    * @param id JSON-LD @type
    * @param map incoming node map
    * @return builder function that returns the type of dynamic node
    */
  def retrieveType(id: String, map: YMap): Option[Annotations => AmfObject] = {
    ts(map, ctx, id).find({ t =>
      dynamicBuilders.get(t).isDefined
    }) match {
      case Some(t) => Some(dynamicBuilders(t))
      case _       => None
    }
  }

  /**
    * Parsed the next dynamic node
    * @param map syntax node map
    * @return the parsed dynamic node model
    */
  def parseDynamicType(map: YMap): Option[DataNode] = {
    retrieveId(map, ctx).flatMap(id => {
      val sources = retrieveSources(id, map)
      val builder = retrieveType(id, map).get

      builder(annotations(nodes, sources, id)) match {

        case obj: ObjectNode =>
          obj.withId(id)
          map.entries.foreach {
            entry =>
              val uri = entry.key.as[String]
              val v   = entry.value
              if (uri != "@type" && uri != "@id" && uri != DomainElementModel.Sources.value.iri() &&
                  uri != (Namespace.Document + "name").iri()) { // we do this to prevent parsing name of annotations

                val dataNode = v match {
                  case _ if isJSONLDScalar(v) => parseJSONLDScalar(v)
                  case _ if isJSONLDArray(v)  => parseJSONLDArray(v)
                  case _ =>
                    parseDynamicType(value(ObjType, v).as[YMap]).getOrElse(ObjectNode()) // todo fix this, its wrong
                }
                obj.addProperty(uri, dataNode)

              }
          }

          Some(obj)

        case scalar: ScalarNode =>
          scalar.withId(id)
          map.entries.foreach {
            entry =>
              val uri = entry.key.as[String]
              uri match {
                /*
              case _ if uri == scalar.Range.value.iri() =>
                scalar.dataType = Some(value(scalar.Range.`type`, entry.value.value).toScalar.text)
                 */
                case _ if uri == ScalarNodeModel.Value.value.iri() =>
                  val parsedScalar = parseJSONLDScalar(entry.value)
                  scalar.value = parsedScalar.value
                  scalar.dataType = parsedScalar.dataType
                case _ => // ignore
              }
          }
          Some(scalar)

        case link: LinkNode =>
          link.withId(id)
          map.entries.foreach { entry =>
            val uri = entry.key.as[String]
            uri match {

              case _ if uri == link.Alias.value.iri() =>
                val parsedScalar = parseJSONLDScalar(entry.value)
                link.alias = parsedScalar.value

              case _ if uri == link.Value.value.iri() =>
                val parsedScalar = parseJSONLDScalar(entry.value)
                link.value = parsedScalar.value

              case _ => // ignore
            }
          }
          referencesMap.get(link.alias) match {
            case Some(target) => link.withLinkedDomainElement(target)
            case None =>
              val unresolved: Seq[DomainElement] = unresolvedReferences.getOrElse(link.alias, Nil)
              unresolvedReferences += (link.alias -> (unresolved ++ Seq(link)))
          }
          Some(link)

        case array: ArrayNode =>
          array.withId(id)
          map.entries.foreach { entry =>
            val uri = entry.key.as[String]
            uri match {
              case _ if uri == array.Member.value.iri() =>
                array.members =
                  entry.value.as[Seq[YNode]].flatMap(e => parseDynamicType(value(ObjType, e).as[YMap])).to[ListBuffer]
              case _ => // ignore
            }
          }
          Some(array)

        case other =>
          ctx.violation(UnableToParseNode,
                        id,
                        s"Cannot parse object data node from non object JSON structure $other",
                        map)
          None
      }
    })
  }

  /**
    * Checks if the node encodes a JSON-LD scalar
    * @param node
    * @return
    */
  private def isJSONLDScalar(node: YNode): Boolean = node.to[Seq[YMap]] match {
    case Right(sequence) if sequence.length == 1 =>
      sequence.head.key("@value").isDefined
    case _ => false
  }

  /**
    * Parsed a JSON-LD scalar into a dynamic ScalarNode
    * @param node
    * @return
    */
  private def parseJSONLDScalar(node: YNode): ScalarNode = {
    val scalar = node.as[Seq[YMap]].head
    val result = ScalarNode()
    scalar
      .key("@value")
      .foreach(entry => {
        result.value = entry.value
      })
    scalar
      .key("@type")
      .foreach(entry => {
        result.dataType = Some(entry.value)
      })
    result
  }

  /**
    * Checks if the node encodes a JSON-LD list
    * @param node
    * @return
    */
  def isJSONLDArray(node: YNode): Boolean = node.to[Seq[YMap]] match {
    case Right(sequence) if sequence.length == 1 =>
      sequence.head.key("@list").isDefined
    case _ => false
  }

  /**
    * Parsed a JSON-LD @list into an ArrayNode
    * @param node
    * @return
    */
  def parseJSONLDArray(node: YNode): ArrayNode = {
    val array   = node.as[Seq[YNode]].head.as[YMap]
    val maybeId = array.key("@id").flatMap(_ => retrieveId(array, ctx))

    val nodeAnnotations: Annotations = maybeId match {
      case Some(id) =>
        val sources = retrieveSources(id, array)
        annotations(nodes, sources, id)
      case None => Annotations()
    }

    val arrayNode: ArrayNode = ArrayNode(nodeAnnotations)
    array.entries.foreach { entry =>
      val member = parseDynamicType(entry.value.as[YMap])
      member.foreach { arrayNode.addMember }
    }
    arrayNode
  }

  // TODO
  // use ObjectNode as the default value for the map
  /**
    * Mapping fro @type URI values to dynamic node builders
    */
  private val dynamicBuilders: mutable.Map[String, Annotations => AmfObject] = mutable.Map(
    LinkNode.builderType.iri()        -> domain.LinkNode.apply,
    ArrayNode.builderType.iri()       -> domain.ArrayNode.apply,
    ScalarNodeModel.`type`.head.iri() -> domain.ScalarNode.apply,
    ObjectNode.builderType.iri()      -> domain.ObjectNode.apply
  )
}
