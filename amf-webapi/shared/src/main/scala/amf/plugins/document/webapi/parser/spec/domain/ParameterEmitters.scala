package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.ExplicitField
import amf.core.model.document.BaseUnit
import amf.core.model.domain.AmfScalar
import amf.core.parser.{FieldEntry, Fields, Position}
import amf.plugins.document.webapi.annotations.SynthesizedField
import amf.plugins.document.webapi.parser.spec.common.BaseEmitters._
import amf.plugins.document.webapi.parser.spec.common.SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.{AnnotationsEmitter, RamlTypeEmitter}
import amf.plugins.document.webapi.parser.spec.{EntryEmitter, SpecOrdering}
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.models.Parameter
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.YType

import scala.collection.mutable

/**
  *
  */
case class RamlParametersEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    sourceOr(
      f.value.annotations,
      b.entry(
        key,
        _.obj(traverse(parameters(f, ordering, references), _))
      )
    )
  }

  private def parameters(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit]): Seq[EntryEmitter] = {
    val result = mutable.ListBuffer[EntryEmitter]()
    f.array.values
      .foreach(e => result += RamlParameterEmitter(e.asInstanceOf[Parameter], ordering, references))

    ordering.sorted(result)
  }

  override def position(): Position = pos(f.value.annotations)
}

case class RamlParameterEmitter(parameter: Parameter, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    sourceOr(
      parameter.annotations,
      if (parameter.isLink) emitLink(b) else emitParameter(b)
    )
  }

  private def emitLink(b: EntryBuilder) = {
    val fs = parameter.linkTarget.get.fields

    b.complexEntry(
      emitParameterKey(fs, _),
      b => {
        parameter.linkTarget.foreach(l => spec.tagToReference(l, parameter.linkLabel, references).emit(b))
      }
    )
  }

  private def emitParameter(b: EntryBuilder) = {
    val fs = parameter.fields
    if (Option(parameter.schema).isDefined && parameter.schema.annotations.contains(classOf[SynthesizedField])) {
      b.complexEntry(
        emitParameterKey(fs, _),
        raw(_, "", YType.Null)
      )
    } else {
      b.complexEntry(
        emitParameterKey(fs, _),
        _.obj { b =>
          val result = mutable.ListBuffer[EntryEmitter]()

          fs.entry(ParameterModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(ParameterModel.Required)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(f => result += ValueEmitter("required", f))

          parameter.schema match {
            case shape: AnyShape =>
              result ++= RamlTypeEmitter(shape, ordering, Seq(AnyShapeModel.Description), references).entries()
            case _ => throw new Exception("Cannot emit parameter for a non WebAPI shape")
          }


          result ++= AnnotationsEmitter(parameter, ordering).emitters

          Option(parameter.fields.getValue(ParameterModel.Binding)) match {
            case Some(v) =>
              v.annotations.find(classOf[ExplicitField]) match {
                case Some(_) =>
                  fs.entry(ParameterModel.Binding).map { f =>
                    result += ValueEmitter("(binding)", f)
                  }
                case None => // ignore
              }
            case _ => // ignore
          }

          traverse(ordering.sorted(result), b)
        }
      )
    }
  }

  private def emitParameterKey(fs: Fields, b: PartBuilder) = {
    val explicit = fs
      .entry(ParameterModel.Required)
      .exists(_.value.annotations.contains(classOf[ExplicitField]))

    if (!explicit && !parameter.required) {
      ScalarEmitter(AmfScalar(parameter.name + "?")).emit(b)
    } else {
      ScalarEmitter(fs.entry(ParameterModel.Name).get.scalar).emit(b)
    }
  }

  override def position(): Position = pos(parameter.annotations)
}
