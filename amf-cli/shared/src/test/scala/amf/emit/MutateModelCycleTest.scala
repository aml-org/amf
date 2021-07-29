package amf.emit

import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.templates.{ParametrizedDeclaration, VariableValue}
import amf.core.internal.remote.{Hint, Raml10, Raml10YamlHint, Spec}
import amf.io.FunSuiteCycleTests
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.testing.ConfigProvider.configFor
import org.scalatest.Assertion

import scala.concurrent.Future

class MutateModelCycleTest extends FunSuiteCycleTests {

  test("Test add empty variable to trait") {

    val transform = (bu: BaseUnit) => {
      val traitNode = bu.asInstanceOf[Document].encodes.asInstanceOf[WebApi].endPoints.head.operations.head.extend.head
      val newParam  = VariableValue().withName("param1")
      traitNode.asInstanceOf[ParametrizedDeclaration].withVariables(Seq(newParam))
      bu
    }
    transformCycle("add-empty-variable.raml",
                   "add-empty-variable-mutated.raml",
                   Raml10YamlHint,
                   Raml10YamlHint,
                   transform)
  }

  final def transformCycle(source: String,
                           golden: String,
                           hint: Hint,
                           target: Hint,
                           transform: BaseUnit => BaseUnit,
                           directory: String = basePath): Future[Assertion] = {
    val config    = CycleConfig(source, golden, hint, target, directory, None, None)
    val amfConfig = buildConfig(configFor(target.vendor), None, None)
    build(config, amfConfig)
      .map(transform(_))
      .map(render(_, config, amfConfig))
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
  }

  override val basePath = "amf-cli/shared/src/test/resources/upanddown/mutate-tests/"
}
