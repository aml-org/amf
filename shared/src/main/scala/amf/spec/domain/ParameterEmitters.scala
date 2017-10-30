package amf.spec.domain

import amf.document.BaseUnit
import amf.domain.Annotation.ExplicitField
import amf.domain.{FieldEntry, Fields, Parameter}
import amf.metadata.domain.ParameterModel
import amf.metadata.shape.ShapeModel
import amf.model.AmfScalar
import amf.parser.Position
import amf.spec.common.BaseEmitters._
import amf.spec.common.SpecEmitterContext
import amf.spec.declaration.{AnnotationsEmitter, RamlTypeEmitter}
import amf.spec.{EntryEmitter, SpecOrdering}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

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

    b.complexEntry(
      emitParameterKey(fs, _),
      _.obj { b =>
        val result = mutable.ListBuffer[EntryEmitter]()

        fs.entry(ParameterModel.Description).map(f => result += ValueEmitter("description", f))

        fs.entry(ParameterModel.Required)
          .filter(_.value.annotations.contains(classOf[ExplicitField]))
          .map(f => result += ValueEmitter("required", f))

        result ++= RamlTypeEmitter(parameter.schema, ordering, Seq(ShapeModel.Description), references).entries()

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
