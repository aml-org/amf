package amf.cli.internal.commands

import amf.apicontract.client.scala.{
  AMFConfiguration,
  AsyncAPIConfiguration,
  BaseApiConfiguration,
  OASConfiguration,
  RAMLConfiguration
}
import amf.core.internal.remote.{AsyncApi20, Oas20, Oas30, Raml08, Raml10, SpecId}

object ConfigProvider {

  def configFor(vendor: SpecId): AMFConfiguration = vendor match {
    case Raml08     => RAMLConfiguration.RAML08()
    case Raml10     => RAMLConfiguration.RAML10()
    case Oas20      => OASConfiguration.OAS20()
    case Oas30      => OASConfiguration.OAS30()
    case AsyncApi20 => AsyncAPIConfiguration.Async20()
    case _          => BaseApiConfiguration.BASE()
  }
}
