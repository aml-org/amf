package amf.graph

import amf.domain.Annotations
import amf.domain.extensions.{ArrayNode, DataNode, ObjectNode, ScalarNode}
import amf.metadata.Type.ObjType
import amf.metadata.domain.DomainElementModel
import amf.model.{AmfElement, AmfObject}
import amf.parser.{YMapOps, YValueOps}
import org.yaml.model.{YMap, YSequence, YValue}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class DynamicGraphParser(var nodes: Map[String, AmfElement]) extends GraphParserHelpers {

  def retrieveType(map: YMap): Option[(Annotations) => AmfObject] = {
    ts(map).find({ t =>
      dynamicBuilders.get(t).isDefined
    }) match {
      case Some(t) => Some(dynamicBuilders(t))
      case _       => None
    }
  }

  def parseDynamicType(map: YMap): DataNode = {
    val id      = retrieveId(map)
    val sources = retrieveSources(id, map)
    val builder = retrieveType(map).get

    builder(annotations(nodes, sources, id)) match {

      case obj: ObjectNode =>
        obj.withId(id)
        map.entries.foreach { entry =>
          val uri = entry.key.value.toScalar.text
          val v   = entry.value.value
          if (uri != "@type" && uri != "@id" && uri != DomainElementModel.Sources.value.iri()) {
            val dataNode = v match {
              case _ if isJSONLDScalar(v) => parseJSONLDScalar(v)
              case _ if isJSONLDArray(v)  => parseJSONLDArray(v)
              case _                      => parseDynamicType(value(ObjType, v).toMap)
            }
            obj.addProperty(uri, dataNode)
          }
        }

        obj

      case scalar: ScalarNode =>
        scalar.withId(id)
        map.entries.foreach { entry =>
          val uri = entry.key.value.toScalar.text
          uri match {
            case _ if uri == scalar.Range.value.iri() =>
              scalar.dataType = Some(value(scalar.Range.`type`, entry.value.value).toScalar.text)
            case _ if uri == scalar.Value.value.iri() =>
              scalar.value = parseJSONLDScalar(entry.value.value).value
            case _ => // ignore
          }
        }
        scalar

      case array: ArrayNode =>
        array.withId(id)
        map.entries.foreach { entry =>
          val uri = entry.key.value.toScalar.text
          uri match {
            case _ if uri == array.Member.value.iri() =>
              array.members =
                entry.value.value.toSequence.values.map(e => parseDynamicType(value(ObjType, e).toMap)).to[ListBuffer]
            case _ => // ignore
          }
        }
        array

      case other =>
        throw new Exception(s"Cannot parse object data node from non object JSON structure $other")
    }

  }

  def isJSONLDScalar(value: YValue): Boolean = value match {
    case sequence: YSequence if sequence.values.length == 1 =>
      sequence.values.head match {
        case map: YMap => map.key("@value").isDefined
        case _         => false
      }
    case _ => false
  }

  def parseJSONLDScalar(node: YValue): ScalarNode = {
    val scalar = node.toSequence.values.head.toMap
    val result = ScalarNode()
    scalar
      .key("@value")
      .foreach(entry => {
        result.value = entry.value.value.toScalar.text
      })
    scalar
      .key("@type")
      .foreach(entry => {
        result.dataType = Some(entry.value.value.toScalar.text)
      })
    result
  }

  def isJSONLDArray(value: YValue): Boolean = value match {
    case sequence: YSequence if sequence.values.length == 1 =>
      sequence.values.head match {
        case map: YMap => map.key("@list").isDefined
        case _         => false
      }
    case _ => false
  }

  def parseJSONLDArray(node: YValue): ArrayNode = {
    val array   = node.toSequence.values.head.toMap
    val maybeId = array.key("@id").map(_ => retrieveId(array))

    val nodeAnnotations: Annotations = maybeId match {
      case Some(id) =>
        val sources = retrieveSources(id, array)
        annotations(nodes, sources, id)
      case None => Annotations()
    }

    val arrayNode = ArrayNode(nodeAnnotations)
    array.entries.foreach { entry =>
      val member = parseDynamicType(entry.value.value.toMap)
      arrayNode.addMember(member)
    }
    arrayNode
  }

  // TODO
  // use ObjectNode as the default value for the map
  private val dynamicBuilders: mutable.Map[String, (Annotations) => AmfObject] = mutable.Map(
    amf.domain.extensions.ArrayNode.builderType.iri()  -> amf.domain.extensions.ArrayNode.apply,
    amf.domain.extensions.ScalarNode.builderType.iri() -> amf.domain.extensions.ScalarNode.apply,
    amf.domain.extensions.ObjectNode.builderType.iri() -> amf.domain.extensions.ObjectNode.apply
  )
}
