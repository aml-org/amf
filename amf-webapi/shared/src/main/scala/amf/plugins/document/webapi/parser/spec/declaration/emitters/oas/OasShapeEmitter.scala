package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.annotations.{LexicalInformation, NilUnion}
import amf.core.emitter.BaseEmitters.{EntryPartEmitter, ValueEmitter, pos}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.metamodel.Type.Bool
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{AmfScalar, Shape}
import amf.core.parser.{Annotations, FieldEntry, Value}
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.{
  AnnotationsEmitter,
  DataNodeEmitter,
  FacetsEmitter
}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{EnumValuesEmitter, XMLSerializerEmitter, oas}
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import amf.plugins.domain.shapes.models.CreativeWork

import scala.collection.mutable.ListBuffer

abstract class OasShapeEmitter(shape: Shape,
                               ordering: SpecOrdering,
                               references: Seq[BaseUnit],
                               pointer: Seq[String] = Nil,
                               schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasLikeSpecEmitterContext) {
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

    fs.entry(ShapeModel.Values).map(f => result += EnumValuesEmitter("enum", f.value, ordering))

    fs.entry(AnyShapeModel.XMLSerialization).map(f => result += XMLSerializerEmitter("xml", f, ordering))

    emitNullable(result)

    result ++= AnnotationsEmitter(shape, ordering).emitters

    result ++= FacetsEmitter(shape, ordering).emitters

    fs.entry(ShapeModel.CustomShapePropertyDefinitions)
      .map(f => {
        result += spec.factory.customFacetsEmitter(f, ordering, references)
      })

    if (Option(shape.and).isDefined && shape.and.nonEmpty)
      result += OasAndConstraintEmitter(shape, ordering, references, pointer, schemaPath)
    if (Option(shape.or).isDefined && shape.or.nonEmpty)
      result += OasOrConstraintEmitter(shape, ordering, references, pointer, schemaPath)
    if (Option(shape.xone).isDefined && shape.xone.nonEmpty)
      result += OasXoneConstraintEmitter(shape, ordering, references, pointer, schemaPath)
    if (Option(shape.not).isDefined)
      result += oas.OasEntryShapeEmitter("not", shape.not, ordering, references, pointer, schemaPath)

    fs.entry(ShapeModel.ReadOnly).map(fe => result += ValueEmitter("readOnly", fe))

    if (spec.schemaVersion.isInstanceOf[OAS30SchemaVersion] || spec.schemaVersion.isBiggerThanOrEqualTo(JSONSchemaDraft7SchemaVersion)) {
      fs.entry(ShapeModel.Deprecated).map(f => result += ValueEmitter("deprecated", f))
      fs.entry(ShapeModel.WriteOnly).map(fe => result += ValueEmitter("writeOnly", fe))
    }

    if (spec.schemaVersion isBiggerThanOrEqualTo JSONSchemaDraft7SchemaVersion) {
      if (Option(shape.ifShape).isDefined)
        result += oas.OasEntryShapeEmitter("if", shape.ifShape, ordering, references, pointer, schemaPath)
      if (Option(shape.thenShape).isDefined)
        result += oas.OasEntryShapeEmitter("then", shape.thenShape, ordering, references, pointer, schemaPath)
      if (Option(shape.elseShape).isDefined)
        result += oas.OasEntryShapeEmitter("else", shape.elseShape, ordering, references, pointer, schemaPath)
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
