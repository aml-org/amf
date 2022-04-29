package amf.apicontract.internal.spec.common.emitter

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation, Parameter, Server}
import amf.apicontract.internal.metamodel.domain.ServerModel
import amf.apicontract.internal.metamodel.domain.api.BaseApiModel
import amf.apicontract.internal.spec.oas.emitter.context.{Oas3SpecEmitterFactory, OasSpecEmitterContext}
import amf.apicontract.internal.spec.oas.parser.domain.BaseUriSplitter
import amf.apicontract.internal.spec.raml.emitter.context.RamlSpecEmitterContext
import amf.apicontract.internal.spec.spec.toRaml
import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, DomainElement}
import amf.core.internal.annotations.{BasePathLexicalInformation, HostLexicalInformation, VirtualElement}
import amf.core.internal.datanode.DataNodeEmitter
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.domain.{FieldEntry, Value}
import amf.core.internal.render.BaseEmitters._
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.shapes.internal.domain.metamodel.ScalarShapeModel
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import amf.shapes.internal.spec.common.emitter.{EnumValuesEmitter, ShapeEmitterContext}
import amf.shapes.internal.spec.contexts.emitter.raml.RamlScalarEmitter
import org.yaml.model.YDocument
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable.ListBuffer

case class RamlServersEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlSpecEmitterContext
) {

  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)

  def emitters(): Seq[EntryEmitter] = {
    val servers = Servers(f)

    val result = ListBuffer[EntryEmitter]()

    servers.default match {
      case Some(server) =>
        defaultServer(server, result)
        asExtension(servers.servers, result)
      case None if servers.servers.nonEmpty =>
        defaultServer(servers.servers.head, result)
        asExtension(servers.servers.tail, result)
      case None => // ignore
    }

    result
  }

  private def defaultServer(default: Server, result: ListBuffer[EntryEmitter]) = {
    val fs = default.fields

    fs.entry(ServerModel.Url).map(f => result += RamlScalarEmitter("baseUri", f))
    fs.entry(ServerModel.Description).map(f => result += RamlScalarEmitter("serverDescription".asRamlAnnotation, f))
    fs.entry(ServerModel.Variables)
      .map(f => result += RamlParametersEmitter("baseUriParameters", f, ordering, references))
  }

  private def asExtension(servers: Seq[Server], result: ListBuffer[EntryEmitter]) =
    if (servers.nonEmpty) result += ServersEmitters("servers".asRamlAnnotation, servers, ordering)
}

abstract class OasServersEmitter(elem: DomainElement, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext
) {
  def emitters(): Seq[EntryEmitter]
  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)

  protected def asExtension(key: String, servers: Seq[Server], result: ListBuffer[EntryEmitter]): Unit =
    if (servers.nonEmpty) result += ServersEmitters(key, servers, ordering)
}

abstract class Oas3ServersEmitter(
    elem: DomainElement,
    f: FieldEntry,
    ordering: SpecOrdering,
    references: Seq[BaseUnit]
)(implicit spec: OasSpecEmitterContext)
    extends OasServersEmitter(elem, f, ordering, references) {

  override protected def asExtension(key: String, servers: Seq[Server], result: ListBuffer[EntryEmitter]): Unit = {
    spec.factory match {
      case _: Oas3SpecEmitterFactory =>
        super.asExtension(key, servers, result)
      case _ => // Nothing
    }
  }
}

case class Oas3WebApiServersEmitter(api: Api, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: OasSpecEmitterContext
) extends OasServersEmitter(api, f, ordering, references) {

  def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()

    asExtension("servers", f.arrayValues(classOf[Server]), result)

    result
  }
}

case class Oas3EndPointServersEmitter(
    endpoint: EndPoint,
    f: FieldEntry,
    ordering: SpecOrdering,
    references: Seq[BaseUnit]
)(implicit spec: OasSpecEmitterContext)
    extends OasServersEmitter(endpoint, f, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()
    asExtension("servers", endpoint.servers, result)
    result
  }
}

case class Oas3OperationServersEmitter(
    operation: Operation,
    f: FieldEntry,
    ordering: SpecOrdering,
    references: Seq[BaseUnit]
)(implicit spec: OasSpecEmitterContext)
    extends Oas3ServersEmitter(operation, f, ordering, references) {

  def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()
    asExtension("servers", operation.servers, result)
    result
  }
}

case class Oas2ServersEmitter(api: Api, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: OasSpecEmitterContext
) extends OasServersEmitter(api, f, ordering, references) {

  def emitters(): Seq[EntryEmitter] = {
    val servers = Servers(f)

    val result = ListBuffer[EntryEmitter]()

    servers.default.foreach(server => defaultServer(server, result))

    asExtension("servers".asOasExtension, servers.servers, result)

    result
  }

  private def defaultServer(default: Server, result: ListBuffer[EntryEmitter]) = {
    val fs = default.fields

    fs.entry(ServerModel.Url).map { f =>
      val uri = BaseUriSplitter(f.scalar.toString)

      if (uri.domain.nonEmpty)
        result += MapEntryEmitter(
          "host",
          uri.domain,
          position = pos(f.value.annotations, classOf[HostLexicalInformation])
        )
      if (uri.path.nonEmpty)
        result += MapEntryEmitter(
          "basePath",
          uri.path,
          position = pos(f.value.annotations, classOf[BasePathLexicalInformation])
        )
      if (uri.protocol.nonEmpty && !api.fields.exists(BaseApiModel.Schemes))
        result += spec.arrayEmitter(
          "schemes",
          FieldEntry(BaseApiModel.Schemes, Value(AmfArray(Seq(AmfScalar(uri.protocol))), f.value.annotations)),
          ordering
        )
    }

    fs.entry(ServerModel.Description).map(f => result += RamlScalarEmitter("serverDescription".asOasExtension, f))

    fs.entry(ServerModel.Variables)
      .map(f =>
        result += RamlParametersEmitter("baseUriParameters".asOasExtension, f, ordering, references)(toRaml(spec))
      )
  }
}

private case class ServersEmitters(key: String, servers: Seq[Server], ordering: SpecOrdering)(implicit
    spec: SpecEmitterContext
) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      key,
      _.list(traverse(orderedServers(servers), _))
    )
  }

  private def orderedServers(servers: Seq[Server]): Seq[PartEmitter] =
    ordering.sorted(servers.map(e => OasServerEmitter(e, ordering)))

  override def position(): Position = servers.headOption.map(_.annotations).map(pos).getOrElse(Position.ZERO)
}

case class OasServerEmitter(server: Server, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends PartEmitter {

  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)

  override def emit(b: YDocument.PartBuilder): Unit = {
    val result = ListBuffer[EntryEmitter]()
    val fs     = server.fields

    fs.entry(ServerModel.Url).map(f => result += ValueEmitter("url", f))

    fs.entry(ServerModel.Description).map(f => result += ValueEmitter("description", f))

    fs.entry(ServerModel.Variables).map(f => result += OasServerVariablesEmitter(f, ordering))

    result ++= AnnotationsEmitter(server, ordering).emitters

    b.obj(traverse(ordering.sorted(result), _))
  }

  override def position(): Position = pos(server.annotations)
}

case class OasServerVariablesEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val all                 = f.arrayValues(classOf[Parameter])
    val hasNonOas3Variables = all.exists(!Servers.isVariable(_))

    b.entry(
      "variables",
      _.obj(traverse(variables(all), _))
    )

    if (hasNonOas3Variables) {
      RamlParametersEmitter("parameters".asOasExtension, f, ordering, Seq())(toRaml(spec)).emit(b)
    }
  }

  private def variables(vars: Seq[Parameter]): Seq[EntryEmitter] =
    ordering.sorted(vars.map(v => OasServerVariableEmitter(v, ordering)))

  override def position(): Position = pos(f.element.annotations)
}

/** This emitter reduces the parameter to the fields that the oas3 variables support. */
private case class OasServerVariableEmitter(variable: Parameter, ordering: SpecOrdering)(implicit
    spec: SpecEmitterContext
) extends EntryEmitter {

  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      variable.name.value(),
      _.obj(traverse(entries(variable), _))
    )
  }

  private def entries(variable: Parameter): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()
    val shape  = variable.schema
    val fs     = shape.fields

    fs.entry(ShapeModel.Description).map(f => result += ValueEmitter("description", f))

    fs.entry(ShapeModel.Values).map(f => result += EnumValuesEmitter("enum", f.value, ordering))

    fs.entry(ShapeModel.Default) match {
      case Some(f) =>
        result += EntryPartEmitter(
          "default",
          DataNodeEmitter(shape.default, ordering)(spec.eh),
          position = pos(f.value.annotations)
        )
      case None => result += MapEntryEmitter("default", "")
    }

    result
  }

  override def position(): Position = pos(variable.annotations)
}

private case class Servers(default: Option[Server], servers: Seq[Server])

private object Servers {
  def apply(f: FieldEntry): Servers = {
    f.arrayValues(classOf[Server]).toList match {
      case Nil         => Servers(None, Nil)
      case head :: Nil => Servers(Some(head), Nil)
      case _ =>
        val (default, servers) =
          f.arrayValues(classOf[Server]).partition(_.annotations.find(classOf[VirtualElement]).isDefined)
        new Servers(default.headOption, servers)
    }
  }

  def isVariable(parameter: Parameter): Boolean = parameter.schema match {
    case s: ScalarShape =>
      val variableFields = Seq(
        ShapeModel.Name,
        ShapeModel.Default,
        ShapeModel.DefaultValueString,
        ShapeModel.Values,
        ScalarShapeModel.DataType,
        ShapeModel.Description
      )
      s.fields.foreach { case (field, _) =>
        if (!variableFields.contains(field)) return false
      }
      true
    case _ => false
  }
}
