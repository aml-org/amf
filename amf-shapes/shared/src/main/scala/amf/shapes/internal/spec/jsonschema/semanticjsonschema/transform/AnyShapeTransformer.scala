package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.{NodeMapping, UnionNodeMapping}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.SemanticJsonSchemaValidations.UnsupportedConstraint

case class AnyShapeTransformer(shape: AnyShape, ctx: ShapeTransformationContext)(
    implicit errorHandler: AMFErrorHandler) {

  val nodeMapping: UnionNodeMapping = UnionNodeMapping(shape.annotations).withId(shape.id)

  def transform(): UnionNodeMapping = {
    setMappingName()
    setMappingID()
    updateContext()

    val members = shape.xone.flatMap {
      case member: AnyShape =>
        ShapeTransformation(member, ctx).transform() match {
          case nm: NodeMapping       => Seq(nm.id)
          case unm: UnionNodeMapping => unm.objectRange().map(_.value())
        }
    }

    if (shape.or.nonEmpty) {
      errorHandler.violation(UnsupportedConstraint, shape.id, "Or constraint is not supported")
    }

    if (Option(shape.not).nonEmpty) {
      errorHandler.violation(UnsupportedConstraint, shape.id, "Not constraint is not supported")
    }

    nodeMapping.withObjectRange(members)
  }

  private def setMappingName(): Unit = {
    shape.displayName.option() match {
      case Some(name) => nodeMapping.withName(name.replaceAll(" ", ""))
      case _          => ctx.genName(nodeMapping)
    }
  }

  private def setMappingID(): Unit = {
    val id = s"#/declarations/${nodeMapping.name}"
    nodeMapping.withId(id)
  }

  private def updateContext(): Unit = {
    ctx.registerNodeMapping(nodeMapping)
  }
}
