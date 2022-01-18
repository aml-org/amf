package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{AmfScalar, Shape}
import amf.core.client.scala.vocabulary.Namespace
import amf.core.internal.annotations.{LexicalInformation, NilUnion}
import amf.core.internal.datanode.DataNodeEmitter
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Bool
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.internal.parser.domain.{Annotations, FieldEntry, Value}
import amf.core.internal.render.BaseEmitters.{EntryPartEmitter, ValueEmitter, pos}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.CreativeWork
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.spec.common
import amf.shapes.internal.spec.common.emitter.annotations.{AnnotationsEmitter, FacetsEmitter}
import amf.shapes.internal.spec.common.emitter.{
  OasEntryCreativeWorkEmitter,
  OasLikeShapeEmitterContext,
  XMLSerializerEmitter
}
import amf.shapes.internal.spec.common.{JSONSchemaDraft7SchemaVersion, OAS30SchemaVersion}
import amf.shapes.internal.spec.oas.emitter

import scala.collection.mutable.ListBuffer

abstract class OasShapeEmitter(shape: Shape,
                               ordering: SpecOrdering,
                               references: Seq[BaseUnit],
                               pointer: Seq[String] = Nil,
                               schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasLikeShapeEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {

    val emitDocumentation = spec.options.isWithDocumentation

    val result = ListBuffer[EntryEmitter]()
    val fs     = shape.fields

    if (emitDocumentation) {
      fs.entry(ShapeModel.DisplayName).map(f => result += ValueEmitter("title", f))

      fs.entry(ShapeModel.Description).map(f => result += ValueEmitter("description", f))

      fs.entry(ShapeModel.Default) match {
        case Some(f) =>
          result += EntryPartEmitter("default",
                                     DataNodeEmitter(shape.default, ordering)(spec.eh),
                                     position = pos(f.value.annotations))
        case None => fs.entry(ShapeModel.DefaultValueString).map(dv => result += ValueEmitter("default", dv))
      }

      fs.entry(AnyShapeModel.Documentation)
        .map(f =>
          result += OasEntryCreativeWorkEmitter("externalDocs", f.value.value.asInstanceOf[CreativeWork], ordering))
    }

    fs.entry(ShapeModel.Values).map(f => result += common.emitter.EnumValuesEmitter("enum", f.value, ordering))

    fs.entry(AnyShapeModel.XMLSerialization).map(f => result += XMLSerializerEmitter("xml", f, ordering))

    emitNullable(result)

    result ++= AnnotationsEmitter(shape, ordering).emitters

    result ++= FacetsEmitter(shape, ordering).emitters

    fs.entry(ShapeModel.CustomShapePropertyDefinitions)
      .map(f => {
        result += spec.customFacetsEmitter(f, ordering, references)
      })

    if (Option(shape.and).isDefined && shape.and.nonEmpty)
      result += OasAndConstraintEmitter(shape, ordering, references, pointer, schemaPath)
    if (Option(shape.or).isDefined && shape.or.nonEmpty)
      result += OasOrConstraintEmitter(shape, ordering, references, pointer, schemaPath)
    if (Option(shape.xone).isDefined && shape.xone.nonEmpty)
      result += OasXoneConstraintEmitter(shape, ordering, references, pointer, schemaPath)
    if (Option(shape.not).isDefined)
      result += emitter.OasEntryShapeEmitter("not", shape.not, ordering, references, pointer, schemaPath)

    fs.entry(ShapeModel.ReadOnly).map(fe => result += ValueEmitter("readOnly", fe))

    if (spec.schemaVersion.isInstanceOf[OAS30SchemaVersion] || spec.schemaVersion.isBiggerThanOrEqualTo(
          JSONSchemaDraft7SchemaVersion)) {
      fs.entry(ShapeModel.Deprecated).map(f => result += ValueEmitter("deprecated", f))
      fs.entry(ShapeModel.WriteOnly).map(fe => result += ValueEmitter("writeOnly", fe))
    }

    if (spec.schemaVersion isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion) {
      if (Option(shape.ifShape).isDefined)
        result += emitter.OasEntryShapeEmitter("if", shape.ifShape, ordering, references, pointer, schemaPath)
      if (Option(shape.thenShape).isDefined)
        result += emitter.OasEntryShapeEmitter("then", shape.thenShape, ordering, references, pointer, schemaPath)
      if (Option(shape.elseShape).isDefined)
        result += emitter.OasEntryShapeEmitter("else", shape.elseShape, ordering, references, pointer, schemaPath)
    }
    result
  }

  def emitNullable(result: ListBuffer[EntryEmitter]): Unit = {
    shape.annotations.find(classOf[NilUnion]) match {
      case Some(NilUnion(rangeString)) =>
        result += ValueEmitter(
          "nullable",
          FieldEntry(
            Field(Bool,
                  Namespace.Shapes + "nullable",
                  ModelDoc(ModelVocabularies.Shapes, "nullable", "This field can accept a null value")),
            Value(AmfScalar(true), Annotations(LexicalInformation(rangeString)))
          )
        )

      case _ => // ignore
    }
  }
}
