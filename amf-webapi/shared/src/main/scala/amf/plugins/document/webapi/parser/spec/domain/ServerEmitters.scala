package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{BasePathLexicalInformation, HostLexicalInformation, SynthesizedField, VirtualElement}
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{AmfArray, AmfScalar, DomainElement}
import amf.core.parser.{FieldEntry, Position, Value}
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.contexts._
import amf.plugins.document.webapi.contexts.emitter.oas.{Oas3SpecEmitterFactory, OasSpecEmitterContext}
import amf.plugins.document.webapi.contexts.emitter.raml.{RamlScalarEmitter, RamlSpecEmitterContext}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.EnumValuesEmitter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.{AnnotationsEmitter, DataNodeEmitter}
import amf.plugins.document.webapi.parser.spec.{BaseUriSplitter, toRaml}
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel
import amf.plugins.domain.shapes.models.ScalarShape
import amf.plugins.domain.webapi.metamodel.ServerModel
import amf.plugins.domain.webapi.metamodel.api.{BaseApiModel, WebApiModel}
import amf.plugins.domain.webapi.models._
import amf.plugins.domain.webapi.models.api.Api
import org.yaml.model.YDocument
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable.ListBuffer

case class RamlServersEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext) {

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

abstract class OasServersEmitter(elem: DomainElement,
                                 f: FieldEntry,
                                 ordering: SpecOrdering,
                                 references: Seq[BaseUnit])(implicit spec: OasSpecEmitterContext) {
  def emitters(): Seq[EntryEmitter]

  protected def asExtension(key: String, servers: Seq[Server], result: ListBuffer[EntryEmitter]): Unit =
    if (servers.nonEmpty) result += ServersEmitters(key, servers, ordering)
}

abstract class Oas3ServersEmitter(elem: DomainElement,
                                  f: FieldEntry,
                                  ordering: SpecOrdering,
                                  references: Seq[BaseUnit])(implicit spec: OasSpecEmitterContext)
    extends OasServersEmitter(elem, f, ordering, references) {

  override protected def asExtension(key: String, servers: Seq[Server], result: ListBuffer[EntryEmitter]): Unit = {
    spec.factory match {
      case _: Oas3SpecEmitterFactory =>
        super.asExtension(key, servers, result)
      case _ => // Nothing
    }
  }
}

case class Oas3WebApiServersEmitter(api: Api, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends OasServersEmitter(api, f, ordering, references) {

  def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()

    asExtension("servers", f.arrayValues(classOf[Server]), result)

    result
  }
}

case class Oas3EndPointServersEmitter(endpoint: EndPoint,
                                      f: FieldEntry,
                                      ordering: SpecOrdering,
                                      references: Seq[BaseUnit])(implicit spec: OasSpecEmitterContext)
    extends OasServersEmitter(endpoint, f, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()
    asExtension("servers", endpoint.servers, result)
    result
  }
}

case class Oas3OperationServersEmitter(operation: Operation,
                                       f: FieldEntry,
                                       ordering: SpecOrdering,
                                       references: Seq[BaseUnit])(implicit spec: OasSpecEmitterContext)
    extends Oas3ServersEmitter(operation, f, ordering, references) {

  def emitters(): Seq[EntryEmitter] = {
    val result = ListBuffer[EntryEmitter]()
    asExtension("servers", operation.servers, result)
    result
  }
}

case class Oas2ServersEmitter(api: Api, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends OasServersEmitter(api, f, ordering, references) {

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
        result += MapEntryEmitter("host",
                                  uri.domain,
                                  position = pos(f.value.annotations, classOf[HostLexicalInformation]))
      if (uri.path.nonEmpty)
        result += MapEntryEmitter("basePath",
                                  uri.path,
                                  position = pos(f.value.annotations, classOf[BasePathLexicalInformation]))
      if (uri.protocol.nonEmpty && !api.fields.exists(BaseApiModel.Schemes))
        result += spec.arrayEmitter("schemes",
                                    FieldEntry(BaseApiModel.Schemes,
                                               Value(AmfArray(Seq(AmfScalar(uri.protocol))), f.value.annotations)),
                                    ordering)
    }

    fs.entry(ServerModel.Description).map(f => result += RamlScalarEmitter("serverDescription".asOasExtension, f))

    fs.entry(ServerModel.Variables)
      .map(f =>
        result += RamlParametersEmitter("baseUriParameters".asOasExtension, f, ordering, references)(toRaml(spec)))
  }
}

private case class ServersEmitters(key: String, servers: Seq[Server], ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
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
private case class OasServerVariableEmitter(variable: Parameter, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
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

    fs.entry(ShapeModel.Values).map(f => result += EnumValuesEmitter("enum", f.value, ordering)(spec))

    fs.entry(ShapeModel.Default) match {
      case Some(f) =>
        result += EntryPartEmitter("default",
                                   DataNodeEmitter(shape.default, ordering)(spec.eh),
                                   position = pos(f.value.annotations))
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
      val variableFields = Seq(ShapeModel.Name,
                               ShapeModel.Default,
                               ShapeModel.DefaultValueString,
                               ShapeModel.Values,
                               ScalarShapeModel.DataType,
                               ShapeModel.Description)
      s.fields.foreach {
        case (field, _) =>
          if (!variableFields.contains(field)) return false
      }
      true
    case _ => false
  }
}
