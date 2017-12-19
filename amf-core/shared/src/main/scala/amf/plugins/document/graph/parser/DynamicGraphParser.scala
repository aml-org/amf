package amf.plugins.document.graph.parser

import amf.core.metamodel.Type.ObjType
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain
import amf.core.model.domain._
import amf.core.parser.{Annotations, ParserContext, YMapOps}
import amf.core.vocabulary.Namespace
import org.yaml.model.{YMap, YNode}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class DynamicGraphParser(var nodes: Map[String, AmfElement])(implicit ctx: ParserContext) extends GraphParserHelpers {

  def retrieveType(id: String, map: YMap): Option[(Annotations) => AmfObject] = {
    ts(map, ctx, id).find({ t =>
      dynamicBuilders.get(t).isDefined
    }) match {
      case Some(t) => Some(dynamicBuilders(t))
      case _       => None
    }
  }

  def parseDynamicType(map: YMap): Option[DataNode] = {
    retrieveId(map, ctx).map(id => {
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
                  uri != (Namespace.Document + "name").iri()) {  // we do this to prevent parsing name of annotations

                val dataNode = v match {
                  case _ if isJSONLDScalar(v) => parseJSONLDScalar(v)
                  case _ if isJSONLDArray(v) => parseJSONLDArray(v)
                  case _ =>
                    parseDynamicType(value(ObjType, v).as[YMap]).getOrElse(ObjectNode()) // todo fix this, its wrong
                }
                obj.addProperty(uri, dataNode)

              }
          }

          obj

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
                case _ if uri == scalar.Value.value.iri() =>
                  val parsedScalar = parseJSONLDScalar(entry.value)
                  scalar.value = parsedScalar.value
                  scalar.dataType = parsedScalar.dataType
                case _ => // ignore
              }
          }
          scalar

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
          array

        case other =>
          throw new Exception(s"Cannot parse object data node from non object JSON structure $other")
      }

    })
  }

  private def isJSONLDScalar(node: YNode): Boolean = node.to[Seq[YMap]] match {
    case Right(sequence) if sequence.length == 1 =>
      sequence.head.key("@value").isDefined
    case _ => false
  }

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

  def isJSONLDArray(node: YNode): Boolean = node.to[Seq[YMap]] match {
    case Right(sequence) if sequence.length == 1 =>
      sequence.head.key("@list").isDefined
    case _ => false
  }

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
  private val dynamicBuilders: mutable.Map[String, (Annotations) => AmfObject] = mutable.Map(
    ArrayNode.builderType.iri()  -> domain.ArrayNode.apply,
    ScalarNode.builderType.iri() -> domain.ScalarNode.apply,
    ObjectNode.builderType.iri() -> domain.ObjectNode.apply
  )
}
