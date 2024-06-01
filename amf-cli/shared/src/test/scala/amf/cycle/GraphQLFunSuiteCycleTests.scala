package amf.cycle

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.graphql.client.scala.GraphQLConfiguration
import amf.io.FunSuiteCycleTests

trait GraphQLFunSuiteCycleTests extends FunSuiteCycleTests {
  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    GraphQLConfiguration
      .GraphQL()
      .withRenderOptions(options.getOrElse(renderOptions()))
      .withErrorHandlerProvider(() => eh.getOrElse(IgnoringErrorHandler))
  }

  override def renderOptions(): RenderOptions = super.renderOptions().withPrettyPrint
}
