package amf.parser

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.graphqlfederation.client.scala.GraphQLFederationConfiguration
import amf.io.FunSuiteCycleTests

trait GraphQLFederationFunSuiteCycleTests extends FunSuiteCycleTests {

  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    GraphQLFederationConfiguration
      .GraphQLFederation()
      .withRenderOptions(options.getOrElse(renderOptions()))
      .withErrorHandlerProvider(() => eh.getOrElse(IgnoringErrorHandler))
  }
}
