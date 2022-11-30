package amf.shapes.internal.spec.oas.parser

import amf.shapes.internal.spec.common.parser.IgnoreCriteria

object OasLikeIgnoreCriteria extends IgnoreCriteria {

  private val shapesThatDontPermitRef = List("paths", "operation")

  override def shouldIgnore(shape: String, property: String): Boolean = {
    property.startsWith("x-") || (property == "$ref" && !shapesThatDontPermitRef.contains(shape)) || (property
      .startsWith("/") && shape == "paths")
  }
}