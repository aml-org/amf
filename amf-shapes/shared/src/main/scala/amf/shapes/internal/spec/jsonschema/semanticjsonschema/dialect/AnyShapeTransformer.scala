package amf.shapes.internal.spec.jsonschema.semanticjsonschema.dialect

import amf.aml.client.scala.model.domain.{NodeMapping, UnionNodeMapping}
import amf.core.internal.parser.domain.Fields
import amf.shapes.client.scala.model.domain.AnyShape

case class AnyShapeTransformer(shape: AnyShape, ctx: ShapeTransformationContext) {

  val nodeMapping: UnionNodeMapping = UnionNodeMapping(Fields(),shape.annotations).withId(shape.id)

  def transform(): UnionNodeMapping = {
    updateContext()
    nameShape()

    val members = shape.xone.flatMap { case member: AnyShape =>
      ShapeTransformer(member, ctx).transform() match {
        case nm: NodeMapping => Seq(nm.id)
        case unm: UnionNodeMapping => unm.objectRange().map(_.value())
      }
    }
    if (shape.or.nonEmpty) {
      throw new Error("Or constraint not supported")
    }
    if (Option(shape.not).nonEmpty) {
      throw new Error("Not constraint not supported")
    }

    nodeMapping.withObjectRange(members)
  }

  private def checkInheritance(): Unit = {
    val superSchemas = shape.and
    if (superSchemas.nonEmpty) { // @TODO: support more than 1 super schema
      val hierarchy = superSchemas.map { case s: AnyShape =>
        val transformed = ShapeTransformer(s, ctx).transform()
        transformed match {
          case nm: NodeMapping       => nm.link[NodeMapping](nm.name.value())
          case unm: UnionNodeMapping => unm.link[UnionNodeMapping](unm.name.value())
        }
      }
      nodeMapping.withExtends(hierarchy)
    }
  }

  private def nameShape() {
    shape.displayName.option() match {
      case Some(name) => nodeMapping.withName(name.replaceAll(" ", ""))
      case _          => ctx.genName(nodeMapping)
    }
  }

  private def updateContext(): Unit = {
    ctx.registerNodeMapping(nodeMapping)
  }

}
