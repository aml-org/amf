package amf.plugins.document.webapi.parser.spec.domain

import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, SpecEmitterContext, SpecOrdering}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.AmfScalar
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.parser.spec.declaration.{AnnotationsEmitter, ExtendsEmitter}
import amf.plugins.domain.webapi.metamodel.EndPointModel
import amf.plugins.domain.webapi.models.{EndPoint, Operation}
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable

/**
  *
  */
case class Raml10EndPointEmitter(endpoint: EndPoint,
                                 ordering: SpecOrdering,
                                 children: mutable.ListBuffer[RamlEndPointEmitter] = mutable.ListBuffer(),
                                 references: Seq[BaseUnit] = Seq())(implicit spec: SpecEmitterContext)
    extends RamlEndPointEmitter(ordering, children, references) {

  override protected def parameterEmitter: (String, FieldEntry, SpecOrdering, Seq[BaseUnit]) => RamlParametersEmitter =
    Raml10ParametersEmitter.apply

  override protected def keyParameter: String = "uriParameters"

  override protected def operationEmitter: (Operation, SpecOrdering, Seq[BaseUnit]) => RamlOperationEmitter =
    Raml10OperationEmitter.apply
}

case class Raml08EndPointEmitter(endpoint: EndPoint,
                                 ordering: SpecOrdering,
                                 children: mutable.ListBuffer[RamlEndPointEmitter] = mutable.ListBuffer(),
                                 references: Seq[BaseUnit] = Seq())(implicit ctx: SpecEmitterContext)
    extends RamlEndPointEmitter(ordering, children, references) {

  override protected def parameterEmitter: (String, FieldEntry, SpecOrdering, Seq[BaseUnit]) => RamlParametersEmitter =
    Raml08ParametersEmitter.apply

  override protected def keyParameter: String = "baseUriParameters"

  override protected def operationEmitter: (Operation, SpecOrdering, Seq[BaseUnit]) => RamlOperationEmitter =
    Raml08OperationEmitter.apply
}

abstract class RamlEndPointEmitter(ordering: SpecOrdering,
                                   children: mutable.ListBuffer[RamlEndPointEmitter] = mutable.ListBuffer(),
                                   references: Seq[BaseUnit] = Seq())(implicit val spec: SpecEmitterContext)
    extends EntryEmitter {

  def endpoint: EndPoint

  protected def parameterEmitter: (String, FieldEntry, SpecOrdering, Seq[BaseUnit]) => RamlParametersEmitter
  protected def keyParameter: String
  protected def operationEmitter: (Operation, SpecOrdering, Seq[BaseUnit]) => RamlOperationEmitter

  override def emit(b: EntryBuilder): Unit = {
    val fs = endpoint.fields

    sourceOr(
      endpoint.annotations,
      b.complexEntry(
        b => {
          endpoint.parent.fold(ScalarEmitter(fs.entry(EndPointModel.Path).get.scalar).emit(b))(_ =>
            ScalarEmitter(AmfScalar(endpoint.relativePath)).emit(b))
        },
        _.obj { b =>
          val result = mutable.ListBuffer[EntryEmitter]()

          fs.entry(EndPointModel.Name).map(f => result += ValueEmitter("displayName", f))

          fs.entry(EndPointModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(EndPointModel.UriParameters)
            .map(f => result += parameterEmitter(keyParameter, f, ordering, references))

          fs.entry(DomainElementModel.Extends).map(f => result ++= ExtendsEmitter("", f, ordering).emitters())

          fs.entry(EndPointModel.Operations).map(f => result ++= operations(f, ordering))

          fs.entry(EndPointModel.Security)
            .map(f => result += ParametrizedSecuritiesSchemeEmitter("securedBy", f, ordering))

          result ++= AnnotationsEmitter(endpoint, ordering).emitters

          result ++= children

          traverse(ordering.sorted(result), b)
        }
      )
    )
  }

  def +=(child: RamlEndPointEmitter): Unit = children += child

  private def operations(f: FieldEntry, ordering: SpecOrdering): Seq[EntryEmitter] = {
    f.array.values
      .map(e => operationEmitter(e.asInstanceOf[Operation], ordering, references))
  }

  override def position(): Position = pos(endpoint.annotations)
}
