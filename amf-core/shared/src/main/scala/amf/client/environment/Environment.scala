package amf.client.environment

import amf.client.convert.CoreClientConverters._
import amf.client.resource._
import amf.internal.environment.{Environment => InternalEnvironment}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Environment(private[amf] val _internal: InternalEnvironment) {

  @JSExportTopLevel("client.environment.Environment")
  def this() = this(InternalEnvironment.empty())

  def loaders: ClientList[ResourceLoader] = _internal.loaders.asClient

  def add(loader: ResourceLoader): Environment = Environment(_internal.add(loader))

  def withLoaders(loaders: ClientList[ResourceLoader]): Environment =
    Environment(_internal.withLoaders(loaders.asInternal))
}

object Environment {
  def empty(): Environment                       = new Environment()
  def apply(loader: ResourceLoader): Environment = empty().add(loader)
}
