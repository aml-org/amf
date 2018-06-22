package amf.internal.environment

import amf.core.unsafe.PlatformSecrets
import amf.internal.resource.ResourceLoader

case class Environment(loaders: Seq[ResourceLoader]) {
  def add(loader: ResourceLoader): Environment = Environment(loader +: loaders)

  def withLoaders(loaders: Seq[ResourceLoader]) = Environment(loaders)
}

object Environment extends PlatformSecrets {
  def apply(): Environment                       = new Environment(platform.loaders())
  def apply(loader: ResourceLoader): Environment = new Environment(Seq(loader))
  def empty(): Environment                       = new Environment(Nil)
}
