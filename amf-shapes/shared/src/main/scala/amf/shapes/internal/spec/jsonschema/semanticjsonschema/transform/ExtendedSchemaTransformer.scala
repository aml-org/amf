package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.{ConditionalNodeMapping, NodeMapping, UnionNodeMapping}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.DomainElement
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.domain.metamodel.AnyShapeModel

abstract class ExtendedSchemaTransformer(shape: AnyShape, ctx: ShapeTransformationContext)(
    implicit eh: AMFErrorHandler) {

  val extendedSchema: Option[NodeMapping] = detectExtendedSchema()

  private def detectExtendedSchema(): Option[NodeMapping] = {
    shape match {
      case ns: NodeShape if ns.properties.nonEmpty =>
        // Is a NodeShape with properties, this will be a NodeMapping
        val extension = ShapeTransformation(copyExtendedShape(ns), ctx).transform().asInstanceOf[NodeMapping]
        Some(extension)
      case _ => None // Not extended schema to apply
    }
  }

  private def copyExtendedShape(nodeShape: NodeShape): NodeShape = {
    // Clone the shape to remove composition fields and have only the properties
    val clone = nodeShape.copyElement().asInstanceOf[NodeShape]
    removeSchemaCompositionFields(clone)
    val name = s"extension_${nodeShape.displayName}"
    clone.withDisplayName(name)
    val id = {
      val paths = nodeShape.id.split('/')
      paths.slice(0, paths.length - 1).mkString("/") + s"/${name}"
    }
    clone.withId(id)
  }

  private def removeSchemaCompositionFields(nodeShape: NodeShape): Unit = {
    nodeShape.fields.removeField(AnyShapeModel.Xone)
    nodeShape.fields.removeField(AnyShapeModel.And)
    nodeShape.fields.removeField(AnyShapeModel.Or)
    nodeShape.fields.removeField(AnyShapeModel.Not)
    nodeShape.fields.removeField(AnyShapeModel.If)
    nodeShape.fields.removeField(AnyShapeModel.Then)
    nodeShape.fields.removeField(AnyShapeModel.Else)
  }

  def toLink(mapping: DomainElement): DomainElement = mapping match {
    case nm: NodeMapping             => nm.link[NodeMapping](nm.name.value())
    case unm: UnionNodeMapping       => unm.link[UnionNodeMapping](unm.name.value())
    case cnm: ConditionalNodeMapping => cnm.link[ConditionalNodeMapping](cnm.name.value())
  }

  def addExtendedSchema(element: DomainElement): Unit =
    extendedSchema.foreach(es => element.withExtends(Seq(toLink(es))))

  def addExtendedSchema(element: DomainElement, otherExtensions: Seq[DomainElement]): Unit = {
    val finalExtensions = otherExtensions ++ extendedSchema.map(toLink)
    element.withExtends(finalExtensions)
  }

  // TODO review inheritance of non NodeMapping elements
  def getIri(element: DomainElement): Seq[String] = element match {
    case nm: NodeMapping             => Seq(nm.id)
    case unm: UnionNodeMapping       => unm.objectRange().map(_.value())
    case cnm: ConditionalNodeMapping => Seq(cnm.id)
  }

}
