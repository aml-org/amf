package amf.graph

import amf.common.core._
import amf.common.AMFAST
import amf.common.AMFToken.{Entry, MapToken, SequenceToken}
import amf.domain.Annotations
import amf.domain.extensions.{ArrayNode, DataNode, ObjectNode, ScalarNode}
import amf.metadata.domain.DomainElementModel
import amf.model.{AmfElement, AmfObject}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class DynamicGraphParser(var nodes: Map[String, AmfElement]) extends GraphParserHelpers {

  def retrieveType(ast: AMFAST, ctx: GraphContext): Option[(Annotations) => AmfObject] = {
    ts(ast).find({ t => dynamicBuilders.get(ctx.expand(t)).isDefined }) match {
      case Some(t) => Some(dynamicBuilders(t))
      case _       => None
    }
  }

  def parseDynamicType(node: AMFAST, ctx: GraphContext): DataNode = {
    val id      = retrieveId(node)
    val sources = retrieveSources(id, node)
    val builder = retrieveType(node, ctx).get
    val children = node.children

    builder(annotations(nodes, sources, id)) match {
      case objectInitialValue: ObjectNode =>
        val instance = objectInitialValue.withId(id)
        children.foreach { entry =>
          entry.`type` match {
            case Entry =>
              val propertyUri = entry.head.content.unquote
              val entryValue = entry.last
              if (propertyUri != "@type" && propertyUri != "@id" && propertyUri != DomainElementModel.Sources.value.iri()) {
                val dataNode = entryValue match {
                  case _ if isJSONLDScalar(entryValue) => parseJSONLDScalar(entryValue, ctx)
                  case _ if isJSONLDArray(entryValue) => parseJSONLDArray(entryValue, ctx)
                  case _ => parseDynamicType(entryValue.children.head, ctx)
                }
                instance.addProperty(propertyUri, dataNode)
              }
            case _ =>
              throw new Exception(s"Unexpected AST token to pare $entry")
          }
        }

        instance

      case scalarInitialValue: ScalarNode =>
        var scalarInstance = scalarInitialValue.withId(id).asInstanceOf[ScalarNode]
        children.foreach { entry =>
          entry.`type` match {
            case Entry if entry.head.content.unquote == scalarInstance.Range.value.iri() =>
              scalarInstance.dataType = Some(retrieveId(entry.last.head))

            case Entry if entry.head.content.unquote == scalarInstance.Value.value.iri() =>
              scalarInstance.value = parseJSONLDScalar(entry.last, ctx).value
            case _ => // ignore
          }
        }
        scalarInstance

      case arrayInitialValue: ArrayNode =>
        val arrayInstance = arrayInitialValue.withId(id)
        children.foreach { entry =>
          entry.`type` match {
            case Entry if entry.head.content.unquote == arrayInstance.Member.value.iri() =>
              arrayInstance.members = entry.last.children.map(e => parseDynamicType(e, ctx)).to[ListBuffer]
            case _ => // ignore
          }
        }
        arrayInstance

      case other =>
        throw new Exception(s"Cannot parse object data node from non object JSON structure $other")
    }

  }

  def isJSONLDScalar(entry: AMFAST): Boolean = entry.`type` match {
    case SequenceToken if entry.children.length == 1 =>
      val propertyValue = entry.children.head
      propertyValue.`type` match {
        case MapToken => propertyValue.children.exists(_.head.content.unquote == "@value")
        case _        => false
      }
    case _ => false
  }

  def parseJSONLDScalar(node: AMFAST, context: GraphContext): ScalarNode = {
    val scalar = node.children.head
    val scalarNode = ScalarNode()
    scalar.children.foreach { entry =>
      entry.`type` match {
        case Entry if entry.head.content.unquote == "@value" =>
          scalarNode.value = entry.last.content.unquote
        case Entry if entry.head.content.unquote == "@type" =>
          scalarNode.dataType = Some(entry.last.content.unquote)
        case _  => // ignore
      }
    }
    scalarNode
  }

  def isJSONLDArray(entry: AMFAST): Boolean = entry.`type` match {
    case SequenceToken if entry.children.length == 1 =>
      val propertyValue = entry.children.head
      propertyValue.`type` match {
        case MapToken => propertyValue.children.exists(_.head.content.unquote == "@list")
        case _        => false
      }
    case _ => false
  }

  def parseJSONLDArray(node: AMFAST, context: GraphContext): ArrayNode = {
    val array = node.children.head
    val maybeId      = try {
      Some(retrieveId(array))
    } catch {
      case _: Exception => None
    }
    val nodeAnnotations: Annotations = maybeId match {
      case Some(id) =>
        val sources = retrieveSources(id, array)
        annotations(nodes, sources, id)
      case None     => Annotations()
    }

    val arrayNode = ArrayNode(nodeAnnotations)
    array.children.foreach { entry =>
      val member = parseDynamicType(entry, context)
      arrayNode.addMember(member)
    }
    arrayNode
  }


  // TODO
  // use ObjectNode as the default value for the map
  private val dynamicBuilders: mutable.Map[String, (Annotations) => AmfObject] = mutable.Map(
    amf.domain.extensions.ArrayNode.builderType.iri()  -> {(a: Annotations) => amf.domain.extensions.ArrayNode(a)},
    amf.domain.extensions.ScalarNode.builderType.iri() -> {(a: Annotations) => amf.domain.extensions.ScalarNode(a)},
    amf.domain.extensions.ObjectNode.builderType.iri() -> {(a: Annotations) => amf.domain.extensions.ObjectNode(a)}
  )

}

