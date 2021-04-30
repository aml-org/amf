package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.{EntryPartEmitter, MapEntryEmitter, ValueEmitter, pos}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.metamodel.domain.ShapeModel
import amf.plugins.document.webapi.contexts.emitter.raml.{RamlScalarEmitter, RamlSpecEmitterContext}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.raml.RamlCommonOASFieldsEmitter
import amf.plugins.document.webapi.parser.spec.domain.SingleExampleEmitter
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel
import amf.plugins.domain.shapes.models.ScalarShape
import amf.plugins.domain.shapes.parser.TypeDefXsdMapping
import amf.plugins.domain.webapi.annotations.TypePropertyLexicalInfo

import scala.collection.mutable.ListBuffer

// TODO is this for RAML only?
case class SimpleTypeEmitter(shape: ScalarShape, ordering: SpecOrdering)(implicit spec: RamlSpecEmitterContext)
    extends RamlCommonOASFieldsEmitter {

  def emitters(): Seq[EntryEmitter] = {
    val fs = shape.fields

    val result = ListBuffer[EntryEmitter]()
    fs.entry(ScalarShapeModel.DisplayName).map(f => result += ValueEmitter("displayName", f))

    val typeDef = shape.dataType.option().map(TypeDefXsdMapping.type08Def)

    fs.entry(ScalarShapeModel.DataType)
      .map { f =>
        val rawTypeDef = TypeDefXsdMapping.typeDef08(shape.dataType.value())
        shape.annotations.find(classOf[TypePropertyLexicalInfo]) match {
          case Some(lexicalInfo) =>
            result += MapEntryEmitter("type", rawTypeDef, position = lexicalInfo.range.start)
          case _ =>
            result += MapEntryEmitter("type", rawTypeDef, position = pos(f.value.annotations))
        }
      }

    fs.entry(ScalarShapeModel.Description).map(f => result += ValueEmitter("description", f))

    fs.entry(ScalarShapeModel.Values)
      .map(f => result += EnumValuesEmitter("enum", f.value, ordering))

    fs.entry(ScalarShapeModel.Pattern).map { f =>
      result += RamlScalarEmitter("pattern", processRamlPattern(f))
    }

    fs.entry(ScalarShapeModel.MinLength).map(f => result += ValueEmitter("minLength", f))

    fs.entry(ScalarShapeModel.MaxLength).map(f => result += ValueEmitter("maxLength", f))

    fs.entry(ScalarShapeModel.Minimum)
      .map(f => result += ValueEmitter("minimum", f, Some(NumberTypeToYTypeConverter.convert(typeDef))))

    fs.entry(ScalarShapeModel.Maximum)
      .map(f => result += ValueEmitter("maximum", f, Some(NumberTypeToYTypeConverter.convert(typeDef))))

    shape.examples.headOption.foreach(e => result += SingleExampleEmitter("example", e, ordering))

    fs.entry(ShapeModel.Default)
      .map(f => {
        result += EntryPartEmitter("default",
                                   DataNodeEmitter(shape.default, ordering)(spec.eh),
                                   position = pos(f.value.annotations))
      })

    result
  }

}
