package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{ExplicitField, SynthesizedField}
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{AmfScalar, Shape}
import amf.core.parser.{FieldEntry, Fields, Position}
import amf.plugins.document.webapi.contexts.{
  OasSpecEmitterContext,
  RamlScalarEmitter,
  RamlSpecEmitterContext,
  SpecEmitterContext
}
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.raml.CommentEmitter
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import amf.plugins.domain.shapes.models.{AnyShape, ArrayShape, FileShape, ScalarShape}
import amf.plugins.domain.webapi.metamodel.{ParameterModel, PayloadModel}
import amf.plugins.domain.webapi.models.{Parameter, Payload}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.YType

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  *
  */
case class RamlParametersEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
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
      .filter(!_.annotations.contains(classOf[SynthesizedField]))
      .foreach(e => result += spec.factory.parameterEmitter(e.asInstanceOf[Parameter], ordering, references))

    ordering.sorted(result)
  }

  override def position(): Position = pos(f.value.annotations)
}

case class Raml10ParameterEmitter(parameter: Parameter, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlParameterEmitter(parameter, ordering, references) {

  override protected def emitParameter(b: EntryBuilder): Unit = {
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

          fs.entry(ParameterModel.Description).map(f => result += RamlScalarEmitter("description", f))

          fs.entry(ParameterModel.Required)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(f => result += RamlScalarEmitter("required", f))

          Option(parameter.schema) match {
            case Some(shape: AnyShape) if shape.isLink =>
              Raml10TypeEmitter(shape, ordering, Seq(AnyShapeModel.Description), references)
                .emitters()
                .headOption
                .foreach { h =>
                  result += EntryPartEmitter("type", h.asInstanceOf[PartEmitter])
                }
            case Some(shape: AnyShape) =>
              result ++= Raml10TypeEmitter(shape, ordering, Seq(AnyShapeModel.Description), references).entries()
            case Some(_) => throw new Exception("Cannot emit parameter for a non WebAPI shape")
            // Emit annotations for parameter only if those have not been emitted by shape
            case None => result ++= AnnotationsEmitter(parameter, ordering).emitters
          }

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

  override protected def emitParameterKey(fs: Fields, b: PartBuilder): Unit = {
    val explicit = fs
      .entry(ParameterModel.Required)
      .exists(_.value.annotations.contains(classOf[ExplicitField]))

    if (!explicit && !parameter.required) {
      ScalarEmitter(AmfScalar(parameter.name + "?")).emit(b)
    } else {
      ScalarEmitter(fs.entry(ParameterModel.Name).get.scalar).emit(b)
    }
  }

}

case class Raml08ParameterEmitter(parameter: Parameter, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlParameterEmitter(parameter, ordering, references) {

  override protected def emitParameter(builder: EntryBuilder): Unit = {
    builder.complexEntry(
      emitParameterKey(parameter.fields, _),
      parameter.schema match {
        case anyShape: AnyShape =>
          Raml08TypePartEmitter(anyShape, ordering, references).emit
        case other => CommentEmitter(other, s"Cannot emit ${other.getClass.toString} type of shape in raml 08").emit
      }
    )

  }

  override protected def emitParameterKey(fields: Fields, builder: PartBuilder): Unit =
    ScalarEmitter(fields.entry(ParameterModel.Name).get.scalar).emit(builder)
}

abstract class RamlParameterEmitter(parameter: Parameter, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  protected def emitParameter(builder: EntryBuilder): Unit
  protected def emitParameterKey(fields: Fields, builder: PartBuilder): Unit

  override def emit(b: EntryBuilder): Unit = {
    sourceOr(
      parameter.annotations,
      if (parameter.isLink) emitLink(b) else emitParameter(b)
    )
  }

  private def emitLink(b: EntryBuilder): Unit = {
    val fs = parameter.linkTarget.get.fields

    b.complexEntry(
      emitParameterKey(fs, _),
      b => {
        parameter.linkTarget.foreach(l =>
          spec.factory.tagToReferenceEmitter(l, parameter.linkLabel, references).emit(b))
      }
    )
  }

  override def position(): Position = pos(parameter.annotations)
}

case class OasParametersEmitter(key: String,
                                parameters: Seq[Parameter],
                                ordering: SpecOrdering,
                                payloadOption: Option[Payload] = None,
                                references: Seq[BaseUnit])(implicit val spec: OasSpecEmitterContext) {

  def ramlEndpointEmitters(): Seq[EntryEmitter] = Seq(OasParameterEmitter(parameters, references))

  def oasEndpointEmitters(): Seq[EntryEmitter] = {
    val results = ListBuffer[EntryEmitter]()
    val (oasParameters, ramlParameters) =
      parameters.partition(
        p =>
          Option(p.schema).isEmpty || p.schema.isInstanceOf[ScalarShape] || p.schema
            .isInstanceOf[ArrayShape] || p.schema.isInstanceOf[FileShape])

    if (oasParameters.nonEmpty || payloadOption.isDefined)
      results += OasParameterEmitter(oasParameters, references)

    if (ramlParameters.nonEmpty) {
      val path = ramlParameters.filter(_.isPath)
      if (path.nonEmpty)
        results += XRamlParameterEmitter("x-uriParameters", path)
    }
    results
  }

  def emitters(): Seq[EntryEmitter] = {
    val results = ListBuffer[EntryEmitter]()
    val (oasParameters, ramlParameters) =
      parameters.partition(
        p =>
          Option(p.schema).isEmpty || p.schema.isInstanceOf[ScalarShape] || p.schema
            .isInstanceOf[ArrayShape] || p.schema.isInstanceOf[FileShape])

    if (oasParameters.nonEmpty || payloadOption.isDefined)
      results += OasParameterEmitter(oasParameters, references)

    if (ramlParameters.nonEmpty) {
      val query  = ramlParameters.filter(_.isQuery)
      val header = ramlParameters.filter(_.isHeader)
      if (query.nonEmpty)
        results += XRamlParameterEmitter("x-queryParameters", query)
      if (header.nonEmpty)
        results += XRamlParameterEmitter("x-headers", header)
    }
    results
  }

  case class OasParameterEmitter(oasParameters: Seq[Parameter], references: Seq[BaseUnit]) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      if (oasParameters.nonEmpty || payloadOption.isDefined)
        b.entry(
          key,
          _.list(traverse(parameters(oasParameters, ordering, references), _))
        )
    }

    override def position(): Position =
      oasParameters.headOption
        .map(p => pos(p.annotations))
        .getOrElse(payloadOption.map(p => pos(p.annotations)).getOrElse(Position.ZERO))
  }

  case class XRamlParameterEmitter(key: String, ramlParameters: Seq[Parameter]) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      if (ramlParameters.nonEmpty)
        b.entry(
          key,
          _.obj(traverse(ramlParameters.map(p => Raml10ParameterEmitter(p, ordering, Nil)), _))
        )
    }

    override def position(): Position =
      ramlParameters.headOption.map(p => pos(p.annotations)).getOrElse(Position.ZERO)
  }

  private def parameters(parameters: Seq[Parameter],
                         ordering: SpecOrdering,
                         references: Seq[BaseUnit]): Seq[PartEmitter] = {
    val result = ListBuffer[PartEmitter]()
    parameters.foreach(e => result += ParameterEmitter(e, ordering, references))
    payloadOption.foreach(payload => result += PayloadAsParameterEmitter(payload, ordering, references))
    ordering.sorted(result)
  }

}

case class ParameterEmitter(parameter: Parameter, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit val spec: OasSpecEmitterContext)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    sourceOr(
      parameter.annotations,
      if (parameter.isLink) {
        spec.ref(b, OasDefinitions.appendParameterDefinitionsPrefix(parameter.linkLabel.get))
      } else {
        val result = mutable.ListBuffer[EntryEmitter]()
        val fs     = parameter.fields

        fs.entry(ParameterModel.ParameterName).orElse(fs.entry(ParameterModel.Name)).map(f => result += ValueEmitter("name", f))

        fs.entry(ParameterModel.Description).map(f => result += ValueEmitter("description", f))

        fs.entry(ParameterModel.Required)
          .filter(_.value.annotations.contains(classOf[ExplicitField]) || parameter.required)
          .map(f => result += ValueEmitter("required", f))

        fs.entry(ParameterModel.Binding).map(f => result += ValueEmitter("in", f))

        fs.entry(ParameterModel.Schema)
          .map(
            f =>
              result ++= OasTypeEmitter(f.value.value.asInstanceOf[Shape],
                                        ordering,
                                        Seq(ShapeModel.Description),
                                        references)
                .entries())

        result ++= AnnotationsEmitter(parameter, ordering).emitters

        b.obj(traverse(ordering.sorted(result), _))
      }
    )
  }

  override def position(): Position = pos(parameter.annotations)
}

case class PayloadAsParameterEmitter(payload: Payload, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit val spec: OasSpecEmitterContext)
    extends PartEmitter {

  override def emit(b: PartBuilder): Unit = {
    b.obj { b =>
      val result = mutable.ListBuffer[EntryEmitter]()

      payload.fields
        .entry(PayloadModel.Schema)
        .map(f => result += OasSchemaEmitter(f, ordering, references))

      payload.fields.entry(PayloadModel.MediaType).map(f => result += ValueEmitter("x-media-type", f))

      result += MapEntryEmitter("in", "body")

      result ++= AnnotationsEmitter(payload, ordering).emitters

      traverse(ordering.sorted(result), b)
    }
  }

  override def position(): Position = pos(payload.annotations)
}
