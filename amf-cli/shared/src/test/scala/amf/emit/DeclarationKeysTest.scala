package amf.emit

import amf.aml.internal.parse.common.DeclarationKeys
import amf.apicontract.client.scala.AsyncAPIConfiguration
import amf.core.internal.metamodel.document.ModuleModel
import amf.io.FunSuiteCycleTests
import org.scalatest.matchers.should.Matchers

class DeclarationKeysTest extends FunSuiteCycleTests with Matchers {

  override val basePath: String = "amf-cli/shared/src/test/resources/validations/async20/"

  test("Async declarations should have declaration keys") {
    val apiPath = s"file://$basePath" + "components/async-components.yaml"
    for {
      parseResult <- AsyncAPIConfiguration.Async20().baseUnitClient().parseDocument(apiPath)
    } yield {
      val api                  = parseResult.document
      val declaresAnnotations  = api.fields.getValue(ModuleModel.Declares).annotations
      val maybeDeclarationKeys = declaresAnnotations.find(classOf[DeclarationKeys])
      maybeDeclarationKeys shouldBe a[Some[_]]
      val keys = maybeDeclarationKeys.get.keys
      keys should have size 9 // one for each type of component, not for each component
    }
  }
}
