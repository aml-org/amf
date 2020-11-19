package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.annotations.{DeclaredElement, NilUnion}
import amf.core.emitter.{Emitter, EntryEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.DataType
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{Linkable, RecursiveShape, Shape}
import amf.core.parser.Position
import amf.plugins.document.webapi.annotations.ExternalJsonSchemaShape
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.OasTagToReferenceEmitter
import amf.plugins.domain.shapes.models._
import org.yaml.model.{YDocument, YNode}
import amf.core.emitter.BaseEmitters._
import amf.plugins.document.webapi.contexts.emitter.jsonschema.JsonSchemaEmitterContext

case class OasTypeEmitter(shape: Shape,
                          ordering: SpecOrdering,
                          ignored: Seq[Field] = Nil,
                          references: Seq[BaseUnit],
                          pointer: Seq[String] = Nil,
                          schemaPath: Seq[(String, String)] = Nil,
                          isHeader: Boolean = false)(implicit spec: OasLikeSpecEmitterContext) {
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
      case l: Linkable if l.isLink => Seq(OasTagToReferenceEmitter(l))
      case _ if shape.annotations.contains(classOf[ExternalJsonSchemaShape]) =>
        Seq(ExternalJsonSchemaShapeEmitter(shape))
      case schema: SchemaShape =>
        val copiedNode = schema.copy(fields = schema.fields.filter(f => !ignored.contains(f._1))) // node (amf object) id get loses
        OasSchemaShapeEmitter(copiedNode, ordering).emitters()
      case node: NodeShape =>
        val copiedNode = node.copy(fields = node.fields.filter(f => !ignored.contains(f._1))) // node (amf object) id get loses
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
        OasArrayShapeEmitter(copiedMatrix.toArrayShape, ordering, references, pointer, updatedSchemaPath, isHeader)
          .emitters()
      case array: TupleShape =>
        val copiedArray = array.copy(fields = array.fields.filter(f => !ignored.contains(f._1)))
        OasTupleShapeEmitter(copiedArray, ordering, references, pointer, updatedSchemaPath, isHeader).emitters()
      case nil: NilShape =>
        val copiedNil = nil.copy(fields = nil.fields.filter(f => !ignored.contains(f._1)))
        Seq(OasNilShapeEmitter(copiedNil, ordering))
      case file: FileShape =>
        spec match {
          // In JSON-SCHEMA the datatype file is not valid, so we 'convert it' in a string scalar
          case _: JsonSchemaEmitterContext =>
            val scalar = ScalarShape
              .apply(file.fields, file.annotations)
              .withDataType(DataType.String)
              .copy(fields = file.fields.filter(f => !ignored.contains(f._1)))
            OasScalarShapeEmitter(scalar, ordering, references, isHeader).emitters()
          case _ =>
            val copiedScalar = file.copy(fields = file.fields.filter(f => !ignored.contains(f._1)))
            OasFileShapeEmitter(copiedScalar, ordering, references, isHeader).emitters()
        }
      case scalar: ScalarShape =>
        val copiedScalar = scalar.copy(fields = scalar.fields.filter(f => !ignored.contains(f._1)))
        OasScalarShapeEmitter(copiedScalar, ordering, references, isHeader).emitters()
      case recursive: RecursiveShape =>
        Seq(spec.factory.recursiveShapeEmitter(recursive, ordering, schemaPath))
      case any: AnyShape =>
        val copiedNode = any.copyAnyShape(fields = any.fields.filter(f => !ignored.contains(f._1))) // node (amf object) id get loses
        OasAnyShapeEmitter(copiedNode, ordering, references, pointer, updatedSchemaPath, isHeader).emitters()
      case _ => Seq()
    }
  }

  def entries(): Seq[EntryEmitter] = emitters() collect { case e: EntryEmitter => e }

  def nilUnion(union: UnionShape): Boolean =
    union.anyOf.size == 1 && union.anyOf.head.annotations.contains(classOf[NilUnion])

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
