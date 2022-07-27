package amf.shapes.internal.spec.oas.emitter

import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{Linkable, RecursiveShape, Shape}
import amf.core.internal.annotations.{DeclaredElement, NilUnion}
import amf.core.internal.metamodel.Field
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{Emitter, EntryEmitter}
import amf.shapes.internal.annotations.{BooleanSchema, ExternalJsonSchemaShape}
import amf.shapes.client.scala.model.domain.{
  AnyShape,
  ArrayShape,
  FileShape,
  InheritanceChain,
  MatrixShape,
  NilShape,
  NodeShape,
  ScalarShape,
  SchemaShape,
  TupleShape,
  UnionShape
}
import amf.shapes.internal.spec.common.emitter.{OasLikeShapeEmitterContext, OasShapeReferenceEmitter}
import amf.shapes.internal.spec.oas.emitter
import org.yaml.model.{YDocument, YNode}

case class OasTypeEmitter(
    shape: Shape,
    ordering: SpecOrdering,
    ignored: Seq[Field] = Nil,
    references: Seq[BaseUnit],
    pointer: Seq[String] = Nil,
    schemaPath: Seq[(String, String)] = Nil,
    isHeader: Boolean = false
)(implicit spec: OasLikeShapeEmitterContext) {
  def emitters(): Seq[Emitter] = {

    // Adjusting JSON Schema  pointer
    val nextPointerStr = s"#${pointer.map(p => s"/$p").mkString}"
    var updatedSchemaPath: Seq[(String, String)] = {
      schemaPath :+ (shape.id, nextPointerStr)
    }

    shape match {
      // Only will add to the list if the shape is a declaration
      case chain: InheritanceChain if shape.annotations.contains(classOf[DeclaredElement]) =>
        updatedSchemaPath ++= chain.inheritedIds.map((_, nextPointerStr))
      case _ => // ignore
    }

    shape match {
      case l: Linkable if l.isLink     => Seq(OasShapeReferenceEmitter(l))
      case _ if isBooleanSchema(shape) => Seq(BooleanSchemaEmitter(shape))
      case _ if shape.annotations.contains(classOf[ExternalJsonSchemaShape]) =>
        Seq(ExternalJsonSchemaShapeEmitter(shape))
      case schema: SchemaShape =>
        val copiedNode =
          schema.copy(fields = schema.fields.filter(f => !ignored.contains(f._1))) // node (amf object) id get loses
        OasSchemaShapeEmitter(copiedNode, ordering).emitters()
      case node: NodeShape =>
        val copiedNode =
          node.copy(fields = node.fields.filter(f => !ignored.contains(f._1))) // node (amf object) id get loses
        OasNodeShapeEmitter(copiedNode, ordering, references, pointer, updatedSchemaPath, isHeader).emitters()
      case union: UnionShape if nilUnion(union) =>
        OasTypeEmitter(union.anyOf.head, ordering, ignored, references).emitters()
      case union: UnionShape =>
        val copiedNode = union.copy(fields = union.fields.filter(f => !ignored.contains(f._1)))
        OasUnionShapeEmitter(copiedNode, ordering, references, pointer, updatedSchemaPath).emitters()
      case array: ArrayShape =>
        val copiedArray = array.copy(fields = array.fields.filter(f => !ignored.contains(f._1)))
        OasArrayShapeEmitter(copiedArray, ordering, references, pointer, updatedSchemaPath, isHeader)
          .emitters()
      case matrix: MatrixShape =>
        val copiedMatrix = matrix.copy(fields = matrix.fields.filter(f => !ignored.contains(f._1))).withId(matrix.id)
        emitter
          .OasArrayShapeEmitter(copiedMatrix.toArrayShape, ordering, references, pointer, updatedSchemaPath, isHeader)
          .emitters()
      case array: TupleShape =>
        val copiedArray = array.copy(fields = array.fields.filter(f => !ignored.contains(f._1)))
        OasTupleShapeEmitter(copiedArray, ordering, references, pointer, updatedSchemaPath, isHeader).emitters()
      case nil: NilShape =>
        val copiedNil = nil.copy(fields = nil.fields.filter(f => !ignored.contains(f._1)))
        Seq(OasNilShapeEmitter(copiedNil, ordering))
      case file: FileShape =>
        if (spec.isJsonSchema) {
          val scalar = ScalarShape
            .apply(file.fields, file.annotations)
            .withDataType(DataType.String)
            .copy(fields = file.fields.filter(f => !ignored.contains(f._1)))
          OasScalarShapeEmitter(scalar, ordering, references, isHeader).emitters()
        } else {
          val copiedScalar = file.copy(fields = file.fields.filter(f => !ignored.contains(f._1)))
          OasFileShapeEmitter(copiedScalar, ordering, references, isHeader).emitters()
        }
      case scalar: ScalarShape =>
        val copiedScalar = scalar.copy(fields = scalar.fields.filter(f => !ignored.contains(f._1)))
        emitter.OasScalarShapeEmitter(copiedScalar, ordering, references, isHeader).emitters()
      case recursive: RecursiveShape =>
        Seq(spec.recursiveShapeEmitter(recursive, ordering, schemaPath))
      case any: AnyShape =>
        val copiedNode =
          any.copyAnyShape(fields = any.fields.filter(f => !ignored.contains(f._1))) // node (amf object) id get loses
        OasAnyShapeEmitter(copiedNode, ordering, references, pointer, updatedSchemaPath, isHeader).emitters()
      case _ => Seq()
    }
  }

  def entries(): Seq[EntryEmitter] = emitters() collect { case e: EntryEmitter => e }

  def nilUnion(union: UnionShape): Boolean =
    union.anyOf.size == 1 && union.anyOf.head.annotations.contains(classOf[NilUnion])

  private def isBooleanSchema(shape: Shape): Boolean = shape.annotations.contains(classOf[BooleanSchema])

}

case class ExternalJsonSchemaShapeEmitter(shape: Shape) extends EntryEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    shape.annotations.find(classOf[ExternalJsonSchemaShape]).foreach {
      case ExternalJsonSchemaShape(entry) =>
        b.entry(nodeToText(entry.key), nodeToText(entry.value))
      case _ => // ignore
    }
  }
  private def nodeToText(node: YNode) = node.asScalar.map(_.text).getOrElse("")

  override def position(): Position = pos(shape.annotations)
}
