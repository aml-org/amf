package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{ExplicitField, SynthesizedField}
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{AmfScalar, Shape}
import amf.core.parser.{FieldEntry, Fields, Position, Value}
import amf.plugins.document.webapi.contexts.{
  OasSpecEmitterContext,
  RamlScalarEmitter,
  RamlSpecEmitterContext,
  SpecEmitterContext
}
import amf.plugins.document.webapi.contexts._
import amf.plugins.document.webapi.parser.spec.{OasDefinitions, toRaml}
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.raml.CommentEmitter
import amf.plugins.domain.shapes.metamodel.{AnyShapeModel, FileShapeModel}
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.webapi.metamodel.{ParameterModel, PayloadModel}
import amf.plugins.domain.webapi.models.{Parameter, Payload}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.YType
import amf.core.utils.Strings
import amf.plugins.document.webapi.annotations.FormBodyParameter
import amf.plugins.domain.webapi.annotations.{InvalidBinding, ParameterBindingInBodyLexicalInfo}
import org.yaml.model.YType.Bool

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  *
  */
case class RamlParametersEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val params = parameters(f, ordering, references)
    if (params.nonEmpty) {
      sourceOr(
        f.value.annotations,
        b.entry(
          key,
          _.obj(traverse(params, _))
        )
      )
    }
  }

  private def parameters(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit]): Seq[EntryEmitter] = {
    val result = mutable.ListBuffer[EntryEmitter]()
    f.array.values
      .filter(!_.annotations.contains(classOf[SynthesizedField]))
      .foreach(e => result += spec.factory.headerEmitter(e.asInstanceOf[Parameter], ordering, references))

    ordering.sorted(result)
  }

  override def position(): Position = pos(f.value.annotations)
}

case class Raml10ParameterEmitter(parameter: Parameter, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
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
                    result += ValueEmitter("binding".asRamlAnnotation, f)
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

    if (!explicit && !parameter.required.value()) {
      ScalarEmitter(AmfScalar(parameter.name.value() + "?")).emit(b)
    } else {
      ScalarEmitter(fs.entry(ParameterModel.Name).get.scalar).emit(b)
    }
  }

}

case class Raml08ParameterEmitter(parameter: Parameter, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
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
          spec.factory.tagToReferenceEmitter(l, parameter.linkLabel.option(), references).emit(b))
      }
    )
  }

  override def position(): Position = pos(parameter.annotations)
}

case class OasParametersEmitter(key: String,
                                parameters: Seq[Parameter],
                                ordering: SpecOrdering,
                                payloads: Seq[Payload] = Nil,
                                references: Seq[BaseUnit])(implicit val spec: OasSpecEmitterContext) {

  def ramlEndpointEmitters(): Seq[EntryEmitter] = Seq(OasParameterEmitter(parameters, references))

  def oasEndpointEmitters(): Seq[EntryEmitter] = {
    val results = ListBuffer[EntryEmitter]()
    val (oasParameters, ramlParameters) =
      parameters.partition(
        p =>
          Option(p.schema).isEmpty || p.schema.isInstanceOf[ScalarShape] || p.schema
            .isInstanceOf[ArrayShape] || p.schema.isInstanceOf[FileShape])

    if (oasParameters.nonEmpty || payloads.nonEmpty)
      results += OasParameterEmitter(oasParameters, references)

    if (ramlParameters.nonEmpty) {
      val path = ramlParameters.filter(_.isPath)
      if (path.nonEmpty)
        results += XRamlParameterEmitter("uriParameters".asOasExtension, path)
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

    if (oasParameters.nonEmpty || payloads.nonEmpty)
      results += OasParameterEmitter(oasParameters, references)

    if (ramlParameters.nonEmpty) {
      val query  = ramlParameters.filter(_.isQuery)
      val header = ramlParameters.filter(_.isHeader)
      val path   = ramlParameters.filter(_.isPath)
      if (query.nonEmpty)
        results += XRamlParameterEmitter("queryParameters".asOasExtension, query)
      if (header.nonEmpty)
        results += XRamlParameterEmitter("headers".asOasExtension, header)
      if (path.nonEmpty)
        results += XRamlParameterEmitter("baseUriParameters".asOasExtension, path)
    }
    results
  }

  case class OasParameterEmitter(oasParameters: Seq[Parameter], references: Seq[BaseUnit]) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      if (oasParameters.nonEmpty || payloads.nonEmpty)
        b.entry(
          key,
          _.list(traverse(parameters(oasParameters, ordering, references), _))
        )
    }

    override def position(): Position =
      oasParameters.headOption
        .map(p => pos(p.annotations))
        .getOrElse(payloads.headOption.map(p => pos(p.annotations)).getOrElse(Position.ZERO))
  }

  case class XRamlParameterEmitter(key: String, ramlParameters: Seq[Parameter]) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      if (ramlParameters.nonEmpty)
        b.entry(
          key,
          _.obj(
            traverse(ramlParameters.map(p =>
                       Raml10ParameterEmitter(p, ordering, Nil)(new XRaml10SpecEmitterContext())),
                     _))
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
    payloads.foreach(payload => result += PayloadAsParameterEmitter(payload, ordering, references))
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
        spec.ref(b, OasDefinitions.appendParameterDefinitionsPrefix(parameter.linkLabel.value()))
      } else {
        val result = mutable.ListBuffer[EntryEmitter]()
        val fs     = parameter.fields

        fs.entry(ParameterModel.ParameterName)
          .orElse(fs.entry(ParameterModel.Name))
          .map(f => result += ValueEmitter("name", f))

        fs.entry(ParameterModel.Description).map(f => result += ValueEmitter("description", f))

        fs.entry(ParameterModel.Required)
          .filter(_.value.annotations.contains(classOf[ExplicitField]) || parameter.required.value())
          .map(f => result += ValueEmitter("required", f))

        fs.entry(ParameterModel.Binding)
          .map(f => result += RawValueEmitter("in", ParameterModel.Binding, binding(f), f.value.annotations))

        fs.entry(ParameterModel.Schema)
          .foreach { f =>
            if (parameter.isBody) {
              result += OasSchemaEmitter(f, ordering, references)
            } else {
              result ++= OasTypeEmitter(f.value.value.asInstanceOf[Shape],
                                        ordering,
                                        Seq(ShapeModel.Description),
                                        references).entries()
            }
          }

        result ++= AnnotationsEmitter(parameter, ordering).emitters

        b.obj(traverse(ordering.sorted(result), _))
      }
    )
  }

  def binding(f: FieldEntry): String = {
    f.value.annotations.find(classOf[InvalidBinding]) match {
      case Some(invalid) => invalid.value
      case None          => f.value.toString
    }
  }

  override def position(): Position = pos(parameter.annotations)
}

case class OasHeaderEmitter(parameter: Parameter, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {

  protected def emitParameter(b: EntryBuilder): Unit = {
    b.entry(
      parameter.name.option().get,
      _.obj { b =>
        val result = mutable.ListBuffer[EntryEmitter]()
        val fs     = parameter.fields
        if (Option(parameter.schema).isDefined && Option(parameter.schema.description).isEmpty)
          fs.entry(ParameterModel.Description).map(f => result += RamlScalarEmitter("description", f))

        fs.entry(ParameterModel.Required)
          .filter(_.value.annotations.contains(classOf[ExplicitField]))
          .map(f => result += RamlScalarEmitter("required", f))

        result ++= OasTypeEmitter(parameter.schema, ordering, references = references).entries()
        traverse(ordering.sorted(result), b)
      }
    )
  }

  override def emit(b: EntryBuilder): Unit = {
    sourceOr(
      parameter.annotations,
      if (parameter.isLink) emitLink(b) else emitParameter(b)
    )
  }

  protected def emitParameterKey(fs: Fields, b: PartBuilder): Unit = {
    ScalarEmitter(fs.entry(ParameterModel.Name).get.scalar).emit(b)
  }

  private def emitLink(b: EntryBuilder): Unit = {
    val fs = parameter.linkTarget.get.fields

    b.complexEntry(
      emitParameterKey(fs, _),
      b => {
        parameter.linkTarget.foreach(l =>
          spec.factory.tagToReferenceEmitter(l, parameter.linkLabel.option(), references).emit(b))
      }
    )
  }

  override def position(): Position = pos(parameter.annotations)
}

case class PayloadAsParameterEmitter(payload: Payload, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit val spec: OasSpecEmitterContext)
    extends PartEmitter {

  override def emit(b: PartBuilder): Unit = {
    payload.schema match {
      case file: FileShape => fileShape(file, b)
      case ns: NodeShape if payload.annotations.find(classOf[FormBodyParameter]).isDefined =>
        ns.properties.foreach { formDataParameter(_, b) }
      case _ => defaultPayload(b)
    }
  }

  private def defaultPayload(b: PartBuilder): Unit = {
    b.obj { b =>
      val result = mutable.ListBuffer[EntryEmitter]()

      payload.fields.entry(PayloadModel.MediaType).map(f => result += ValueEmitter("mediaType".asOasExtension, f))
      result += MapEntryEmitter("in", binding(), position = bindingPos(payload.schema))
      payload.fields
        .entry(PayloadModel.Schema)
        .map(f => result += OasSchemaEmitter(f, ordering, references))
      result ++= AnnotationsEmitter(payload, ordering).emitters

      traverse(ordering.sorted(result), b)
    }
  }

  private def fileShape(file: FileShape, b: PartBuilder): Unit = {
    b.obj { b =>
      val result = mutable.ListBuffer[EntryEmitter]()

      val fs = file.fields
      fs.entry(FileShapeModel.Name).map(f => result += ValueEmitter("name", f))
      fs.entry(FileShapeModel.Description).map(f => result += ValueEmitter("description", f))
      result += MapEntryEmitter("in", "formData", position = bindingPos(file))
      result ++= OasTypeEmitter(file, ordering, Seq(ShapeModel.Description), references).entries()
      result ++= AnnotationsEmitter(payload, ordering).emitters

      traverse(ordering.sorted(result), b)
    }
  }

  private def formDataParameter(property: PropertyShape, b: PartBuilder): Unit = {
    b.obj { b =>
      val result = mutable.ListBuffer[EntryEmitter]()

      property.fields
        .entry(PropertyShapeModel.MinCount)
        .filter(_.scalar.annotations.contains(classOf[ExplicitField]))
        .map(f =>
          result += ValueEmitter("required",
                                 FieldEntry(PropertyShapeModel.MinCount,
                                            Value(AmfScalar(f.scalar.toNumber.intValue() != 0), f.scalar.annotations)),
                                 Some(Bool)))

      val schema = property.range

      val fs = schema.fields

      fs.entry(ShapeModel.Name).map(f => result += ValueEmitter("name", f))

      result += MapEntryEmitter("in", "formData", position = bindingPos(schema))
      result ++= OasTypeEmitter(schema, ordering, references = references).entries()
      result ++= AnnotationsEmitter(payload, ordering).emitters

      traverse(ordering.sorted(result), b)
    }
  }

  private def bindingPos(schema: Shape) = {
    Option(schema)
      .flatMap(_.annotations.find(classOf[ParameterBindingInBodyLexicalInfo]))
      .map(_.range.start)
      .getOrElse(Position.ZERO)
  }

  def binding(): String = payload.annotations.find(classOf[InvalidBinding]) match {
    case Some(invalid) => invalid.value
    case None          => "body"
  }

  override def position(): Position = pos(payload.annotations)
}
