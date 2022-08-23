package amf.cycle

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.graphql.client.scala.GraphQLConfiguration
import amf.io.FunSuiteCycleTests

trait GraphQLFunSuiteCycleTests extends FunSuiteCycleTests {
  override def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    val amfConfig: AMFConfiguration = GraphQLConfiguration.GraphQL()
    val renderedConfig: AMFConfiguration = options.fold(amfConfig.withRenderOptions(renderOptions()))(r => {
      amfConfig.withRenderOptions(r)
    })
    eh.fold(renderedConfig.withErrorHandlerProvider(() => IgnoringErrorHandler))(e =>
      renderedConfig.withErrorHandlerProvider(() => e)
    )
  }

  override def renderOptions(): RenderOptions = super.renderOptions().withPrettyPrint
}
