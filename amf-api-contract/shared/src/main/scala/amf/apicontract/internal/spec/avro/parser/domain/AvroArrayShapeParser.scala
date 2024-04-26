package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroWebAPIContext
import amf.core.internal.metamodel.domain.LinkableElementModel
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, NodeShape, UnresolvedShape}
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, Fields, SearchScope}

case class AvroArrayShapeParser(map: YMap)(implicit ctx: AvroWebAPIContext)
    extends AvroSyntacticShapeParser[ArrayShape](map, "array", "items") {

  // TODO: parse defaults
  override val shape: ArrayShape = ArrayShape(map)

  override def setMembers(anyShape: AnyShape): Unit = shape.withItems(anyShape)

  override def parseMembers(e: YMapEntry): AnyShape = AvroTextParser(e.value).parse()
}

case class AvroTextParser(node: YNode)(implicit ctx: AvroWebAPIContext) {
  def parse(): AnyShape = {
    val name = node.as[YScalar].text
    if (AvroShapeParser.isPrimitive(name)) AvroScalarShapeParser(name, None).parse()
    else AvroReferenceParser(name, node).parse()
  }
}

case class AvroReferenceParser(ref: String, value: YNode)(implicit ctx: AvroWebAPIContext) {
  def parse() = ctx.findType(ref, SearchScope.Named).fold(parseUnresolved())(linkResolved)

  def linkResolved(anyShape: AnyShape): AnyShape = anyShape.link(ref, Annotations(value))

  def parseUnresolved(): AnyShape = {
    val anyShape = AnyShape()
    val unresolve = UnresolvedShape(
      Fields(),
      Annotations(value),
      ref,
      None,
      Some((k: String) => anyShape.set(LinkableElementModel.TargetId, k)),
      shouldLink = false
    ).withName(ref, Annotations(value))
    unresolve.withContext(ctx)
    anyShape.withLinkTarget(unresolve).withLinkLabel(ref)
  }
}

case class AvroMapShapeParser(map: YMap) extends AvroSyntacticShapeParser[NodeShape](map, "map", "values") {

  // TODO: parse defauls
  override val shape: NodeShape = NodeShape(map)

  override def setMembers(anyShape: AnyShape): Unit = shape.withAdditionalPropertiesSchema(anyShape)
}

abstract class AvroSyntacticShapeParser[T <: AnyShape](map: YMap, name: String, membersKey: String) {

  val shape: T

  protected def setMembers(anyShape: AnyShape): Unit

  def parse(): T = {
    shape.withName(name)
    map.key(membersKey).map(parseMembers).foreach(setMembers)
    shape
  }

  protected def parseMembers(e: YMapEntry): AnyShape = AvroScalarShapeParser(e.value.as[String], None).parse()
}
