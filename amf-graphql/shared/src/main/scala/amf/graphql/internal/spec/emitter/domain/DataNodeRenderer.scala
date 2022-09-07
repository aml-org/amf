package amf.graphql.internal.spec.emitter.domain

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.{ArrayNode, DataNode, ObjectNode, ScalarNode}

object DataNodeRenderer {
  def render(d: DataNode): String = {
    d match {
      case s: ScalarNode => renderScalar(s)
      case a: ArrayNode  => renderArray(a)
      case o: ObjectNode => renderObject(o)
      case _ => ""
    }
  }

  private def renderScalar(scalarNode: ScalarNode): String = {
    val rawValue = scalarNode.value.value()
    scalarNode.dataType.value() match {
      case DataType.String => "\"%s\"".format(rawValue)
      case _               => rawValue
    }
  }

  private def renderArray(arrayNode: ArrayNode): String = {
    val members = arrayNode.members.map(render).mkString(", ")
    s"[ $members ]"
  }

  private def renderObject(objectNode: ObjectNode): String = {
    val fields = objectNode
      .allPropertiesWithName()
      .toSeq
      .map { case (name, value) =>
        s"$name: ${render(value)}"
      }
      .mkString(", ")
    s"{ $fields }"
  }

//  private def renderEnum()
//  private def renderNull()
}
