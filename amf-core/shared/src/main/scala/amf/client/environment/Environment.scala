package amf.client.environment

import amf.client.convert.CoreClientConverters
import amf.client.convert.CoreClientConverters._
import amf.client.resource.ResourceLoader
import amf.internal.resource.{ResourceLoader => InternalResourceLoader}
import amf.internal.environment.{Environment => InternalEnvironment}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Environment(private[amf] val _internal: InternalEnvironment) {

  @JSExportTopLevel("client.environment.Environment")
  def this() = this(InternalEnvironment.empty())

  def loaders: ClientList[ResourceLoader] = _internal.loaders.asClient

  def add(loader: ClientLoader): Environment = {
    val internal: ResourceLoader = loader
    Environment(_internal.add(internal))
  }

  def withLoaders(loaders: ClientList[ClientLoader]): Environment = {
    val l: ClientList[ResourceLoader] = loaders.asInstanceOf[ClientList[ResourceLoader]]
    Environment(_internal.withLoaders(l.asInternal))
  }
}

object Environment {
  def empty(): Environment                     = new Environment()
  def apply(loader: ClientLoader): Environment = empty().add(loader)
}
