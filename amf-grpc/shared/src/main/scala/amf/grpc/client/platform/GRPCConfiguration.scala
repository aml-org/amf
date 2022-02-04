package amf.grpc.client.platform

import amf.apicontract.client.platform.AMFConfiguration
import amf.grpc.client.scala.{GRPCConfiguration => InternalGRPCConfiguration}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("GRPCConfiguration")
object GRPCConfiguration {

  def GRPC(): AMFConfiguration = new AMFConfiguration(InternalGRPCConfiguration.GRPC())
}
