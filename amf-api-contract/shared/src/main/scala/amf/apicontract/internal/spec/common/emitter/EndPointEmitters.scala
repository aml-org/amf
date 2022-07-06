package amf.apicontract.internal.spec.common.emitter

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation, Parameter}
import amf.apicontract.internal.metamodel.domain.{EndPointModel, ParameterModel}
import amf.apicontract.internal.spec.oas.emitter.domain.Raml10PayloadsEmitter
import amf.apicontract.internal.spec.raml.emitter.context.RamlSpecEmitterContext
import amf.apicontract.internal.spec.raml.emitter.domain.ExtendsEmitter
import amf.apicontract.internal.spec.spec.toOas
import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.annotations.SynthesizedField
import amf.core.internal.parser.domain.{FieldEntry, Fields, Value}
import amf.core.internal.render.BaseEmitters.{ScalarEmitter, pos, sourceOr, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.core.internal.utils.{AmfStrings, TemplateUri}
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import amf.shapes.internal.spec.contexts.emitter.raml.RamlScalarEmitter
import org.yaml.model.YDocument
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/** */
case class Raml10EndPointEmitter(
    endpoint: EndPoint,
    ordering: SpecOrdering,
    children: mutable.ListBuffer[RamlEndPointEmitter] = mutable.ListBuffer(),
    references: Seq[BaseUnit] = Seq()
)(implicit spec: RamlSpecEmitterContext)
    extends RamlEndPointEmitter(ordering, children, references) {

  override protected def keyParameter: String = "uriParameters"

  override protected def emitters(fs: Fields): ListBuffer[EntryEmitter] = {
    val result = super.emitters(fs)

    fs.entry(EndPointModel.Payloads)
      .map(f => result += Raml10PayloadsEmitter("payloads".asRamlAnnotation, f, ordering, references))

    fs.entry(EndPointModel.Parameters)
      .map { f =>
        if (f.array.values.exists(f => !f.annotations.contains(classOf[SynthesizedField]))) {

          val (path, other) = f.array.values.asInstanceOf[Seq[Parameter]].partition(p => p.isPath)

          result ++= OasParametersEmitter("parameters".asRamlAnnotation, other, ordering, references = references)(
            toOas(spec)
          ).ramlEndpointEmitters()

          val explicitParams = getExplicitParams(path)
          if (explicitParams.nonEmpty) {
            // TODO just emit other params in other way
            val entry = FieldEntry(
              EndPointModel.Parameters,
              Value(AmfArray(path, f.value.value.annotations), f.value.annotations)
            )

            result += RamlParametersEmitter(keyParameter, entry, ordering, references)
          }
        }
      }

    result
  }
}

case class Raml08EndPointEmitter(
    endpoint: EndPoint,
    ordering: SpecOrdering,
    children: mutable.ListBuffer[RamlEndPointEmitter] = mutable.ListBuffer(),
    references: Seq[BaseUnit] = Seq()
)(implicit ctx: RamlSpecEmitterContext)
    extends RamlEndPointEmitter(ordering, children, references) {

  // TODO should we differentiate between uri and baseUri parameters at endpoint lvl?
  override protected def keyParameter: String = "uriParameters"

  override protected def emitters(fs: Fields): ListBuffer[EntryEmitter] = {
    val result                 = super.emitters(fs)
    val variables: Seq[String] = TemplateUri.variables(endpoint.path.value())
    fs.entry(EndPointModel.Parameters)
      .map { f =>
        if (f.array.values.exists(f => !f.annotations.contains(classOf[SynthesizedField]))) {
          var uriParameters: Seq[Parameter]  = Nil
          var pathParameters: Seq[Parameter] = Nil
          f.array.values.foreach { case p: Parameter =>
            if (variables.contains(p.name.value()) || variables.contains(p.parameterName.value()))
              pathParameters ++= Seq(p)
            else
              uriParameters ++= Seq(p)
          }

          result += RamlParametersEmitter(
            "uriParameters",
            FieldEntry(
              EndPointModel.Parameters,
              Value(AmfArray(getExplicitParams(pathParameters)), f.value.annotations)
            ),
            ordering,
            references
          )
          result += RamlParametersEmitter(
            "baseUriParameters",
            FieldEntry(EndPointModel.Parameters, Value(AmfArray(uriParameters), f.value.annotations)),
            ordering,
            references
          )
        }
      }

    result
  }
}

abstract class RamlEndPointEmitter(
    ordering: SpecOrdering,
    children: mutable.ListBuffer[RamlEndPointEmitter] = mutable.ListBuffer(),
    references: Seq[BaseUnit] = Seq()
)(implicit val spec: RamlSpecEmitterContext)
    extends EntryEmitter
    with PartEmitter {

  implicit val shapeEmitterCtx = AgnosticShapeEmitterContextAdapter(spec)

  def endpoint: EndPoint

  protected def keyParameter: String

  protected def getExplicitParams(params: Seq[Parameter]): Seq[Parameter] =
    params.filter(p => !p.fields.getValueAsOption(ParameterModel.Name).exists(_.isSynthesized))

  override def emit(b: EntryBuilder): Unit = {
    val fs = endpoint.fields
    sourceOr(
      endpoint.annotations,
      b.complexEntry(
        b => {
          endpoint.parent match {
            case Some(_) => ScalarEmitter(AmfScalar(endpoint.relativePath)).emit(b)
            case None    => ScalarEmitter(fs.entry(EndPointModel.Path).map(_.scalar).getOrElse(AmfScalar(""))).emit(b)
          }
        },
        emit
      )
    )
  }

  override def emit(b: YDocument.PartBuilder): Unit = {
    val fs = endpoint.fields
    b.obj { b =>
      traverse(ordering.sorted(emitters(fs)), b)
    }
  }

  protected def emitters(fs: Fields): ListBuffer[EntryEmitter] = {
    val result = mutable.ListBuffer[EntryEmitter]()

    fs.entry(EndPointModel.Name).map(f => result += RamlScalarEmitter("displayName", f))

    fs.entry(EndPointModel.Description).map(f => result += RamlScalarEmitter("description", f))

    fs.entry(EndPointModel.Extends).map(f => result ++= ExtendsEmitter(f, ordering)(spec.eh).emitters())

    fs.entry(EndPointModel.Operations).map(f => result ++= operations(f, ordering))

    fs.entry(EndPointModel.Security)
      .map(f => result += SecurityRequirementsEmitter("securedBy", f, ordering))

    result ++= AnnotationsEmitter(endpoint, ordering).emitters

    result ++= children
    result
  }

  def +=(child: RamlEndPointEmitter): Unit = children += child

  private def operations(f: FieldEntry, ordering: SpecOrdering): Seq[EntryEmitter] = {
    f.array.values
      .map(e => spec.factory.operationEmitter(e.asInstanceOf[Operation], ordering, references))
  }

  override def position(): Position = pos(endpoint.annotations)
}
