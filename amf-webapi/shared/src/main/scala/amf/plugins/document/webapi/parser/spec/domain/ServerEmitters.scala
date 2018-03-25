package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{BasePathLexicalInformation, HostLexicalInformation, SynthesizedField}
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{FieldEntry, Position, Value}
import amf.plugins.document.webapi.contexts.{
  OasSpecEmitterContext,
  RamlScalarEmitter,
  RamlSpecEmitterContext,
  SpecEmitterContext
}
import amf.plugins.document.webapi.parser.spec.declaration.{AnnotationsEmitter, DataNodeEmitter}
import amf.plugins.document.webapi.parser.spec.{BaseUriSplitter, toRaml}
import amf.plugins.domain.webapi.metamodel.{ServerModel, WebApiModel}
import amf.plugins.domain.webapi.models.{Parameter, Server, WebApi}
import org.yaml.model.YDocument
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable.ListBuffer

case class RamlServersEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext) {

  def emitters(): Seq[EntryEmitter] = {
    val servers = Servers(f)

    val result = ListBuffer[EntryEmitter]()

    servers.default.foreach(server => defaultServer(server, result))

    asExtension(servers.servers, result)

    result
  }

  private def defaultServer(default: Server, result: ListBuffer[EntryEmitter]) = {
    val fs = default.fields

    fs.entry(ServerModel.Url).map(f => result += RamlScalarEmitter("baseUri", f))
    fs.entry(ServerModel.Description).map(f => result += RamlScalarEmitter("(serverDescription)", f))
    fs.entry(ServerModel.Variables)
      .map(f => result += RamlParametersEmitter("baseUriParameters", f, ordering, references))
  }

  private def asExtension(servers: Seq[Server], result: ListBuffer[EntryEmitter]) =
    if (servers.nonEmpty) result += ServersEmitters("(servers)", servers, ordering)
}

case class OasServersEmitter(api: WebApi, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext) {

  def emitters(): Seq[EntryEmitter] = {
    val servers = Servers(f)

    val result = ListBuffer[EntryEmitter]()

    servers.default.foreach(server => defaultServer(server, result))

    asExtension(servers.servers, result)

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
      if (uri.protocol.nonEmpty && !api.fields.exists(WebApiModel.Schemes))
        result += ArrayEmitter("schemes",
                               FieldEntry(WebApiModel.Schemes,
                                          Value(AmfArray(Seq(AmfScalar(uri.protocol))), f.value.annotations)),
                               ordering)
    }

    fs.entry(ServerModel.Description).map(f => result += RamlScalarEmitter("x-serverDescription", f))

    fs.entry(ServerModel.Variables)
      .map(f => result += RamlParametersEmitter("x-base-uri-parameters", f, ordering, references)(toRaml(spec)))
  }

  private def asExtension(servers: Seq[Server], result: ListBuffer[EntryEmitter]) =
    if (servers.nonEmpty) result += ServersEmitters("(servers)", servers, ordering)
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

  override def position(): Position = pos(servers.head.annotations)
}

private case class OasServerEmitter(server: Server, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
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

private case class OasServerVariablesEmitter(f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val vars: Seq[Parameter] = f.arrayValues(classOf[Parameter])
    b.entry(
      "variables",
      _.obj(traverse(variables(vars), _))
    )
  }

  private def variables(vars: Seq[Parameter]): Seq[EntryEmitter] =
    ordering.sorted(vars.map(v => OasServerVariableEmitter(v, ordering)))

  override def position(): Position = pos(f.element.annotations)
}

/** This emitter reduces the parameter to the fields that the oas3 variables support. */
private case class OasServerVariableEmitter(variable: Parameter, ordering: SpecOrdering) extends EntryEmitter {
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

    fs.entry(ShapeModel.Values).map(f => result += ArrayEmitter("enum", f, ordering))

    fs.entry(ShapeModel.Default)
      .map(f => {
        result += EntryPartEmitter("default",
                                   DataNodeEmitter(shape.default, ordering),
                                   position = pos(f.value.annotations))
      })

    result
  }

  override def position(): Position = pos(variable.annotations)
}

private case class Servers(default: Option[Server], servers: Seq[Server])

private object Servers {
  def apply(f: FieldEntry): Servers = {
    val (default, servers) =
      f.arrayValues(classOf[Server]).partition(_.annotations.find(classOf[SynthesizedField]).isDefined)
    new Servers(default.headOption, servers)
  }
}
