package amf.emit

import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.templates.{ParametrizedDeclaration, VariableValue}
import amf.core.remote._
import amf.io.{BuildCycleTests, FunSuiteCycleTests}
import amf.plugins.domain.webapi.models.api.WebApi
import org.scalatest.Assertion

import scala.concurrent.Future

class MutateModelCycleTest extends FunSuiteCycleTests {

  test("Test add empty variable to trait") {

    val trasform = (bu: BaseUnit) => {
      val traitNode = bu.asInstanceOf[Document].encodes.asInstanceOf[WebApi].endPoints.head.operations.head.extend.head
      val newParam  = VariableValue().withName("param1")
      traitNode.asInstanceOf[ParametrizedDeclaration].withVariables(Seq(newParam))
      bu
    }
    transformCycle("add-empty-variable.raml", "add-empty-variable-mutated.raml", RamlYamlHint, Raml10, trasform)
  }

  final def transformCycle(source: String,
                           golden: String,
                           hint: Hint,
                           target: Vendor,
                           transform: BaseUnit => BaseUnit,
                           directory: String = basePath): Future[Assertion] = {
    val config = CycleConfig(source, golden, hint, target, directory, None, None)

    build(config, None, useAmfJsonldSerialisation = true)
      .map(transform(_))
      .flatMap(render(_, config, useAmfJsonldSerialization = true))
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
  }

  override val basePath = "amf-client/shared/src/test/resources/upanddown/mutate-tests/"
}
