package amf.shapes.test

import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.remote.Mimes._
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import org.scalatest.matchers.should.Matchers

abstract class JsonSchemaBasedSpecSourceSpecTestBase
    extends AsyncFunSuiteWithPlatformGlobalExecutionContext
    with Matchers {

  def testConfig: JsonSchemaBasedSpecTestConfig

  /** Override to provide a specific instance for SourceSpec tests. Defaults to first valid instance. */
  def sourceSpecInstance: String = testConfig.validInstances.head

  private lazy val basePath = "file://" + testConfig.basePath
  private lazy val renderOptions = RenderOptions().withPrettyPrint.withCompactUris.withEntityEmission
  private lazy val client =
    testConfig.configuration.withRenderOptions(renderOptions).baseUnitClient()

  test(s"Parsed JSON-LD from ${testConfig.specName} should have ${testConfig.specName} source spec") {
    for {
      result <- client.parse(basePath + sourceSpecInstance)
      jsonld = client.render(result.baseUnit, `application/ld+json`)
      cycledResult <- client.parseContent(jsonld)
    } yield {
      result.conforms shouldBe true
      cycledResult.conforms shouldBe true
      result.baseUnit.isInstanceOf[JsonLDInstanceDocument] shouldBe true
      cycledResult.baseUnit.isInstanceOf[JsonLDInstanceDocument] shouldBe true
      cycledResult.sourceSpec shouldBe testConfig.spec
      val encodes       = result.baseUnit.asInstanceOf[JsonLDInstanceDocument].encodes
      val cycledEncodes = cycledResult.baseUnit.asInstanceOf[JsonLDInstanceDocument].encodes
      encodes.headOption shouldBe defined
      cycledEncodes.headOption shouldBe defined
      encodes.head.isInstanceOf[JsonLDObject] shouldBe true
      cycledEncodes.head.isInstanceOf[JsonLDObject] shouldBe true
      val rootPropertiesSize       = encodes.head.asInstanceOf[JsonLDObject].fields.fields().size
      val cycledRootPropertiesSize = cycledEncodes.head.asInstanceOf[JsonLDObject].fields.fields().size
      rootPropertiesSize shouldBe cycledRootPropertiesSize
    }
  }

  test(s"${testConfig.specName} transformation (empty) should conform") {
    for {
      parseResult <- client.parse(basePath + sourceSpecInstance)
      transformationDefault = client.transform(parseResult.baseUnit.cloneUnit(), PipelineId.Default)
      transformationEditing = client.transform(parseResult.baseUnit.cloneUnit(), PipelineId.Editing)
      transformationCache   = client.transform(parseResult.baseUnit.cloneUnit(), PipelineId.Cache)
    } yield {
      parseResult.conforms shouldBe true
      transformationDefault.conforms shouldBe true
      transformationEditing.conforms shouldBe true
      transformationCache.conforms shouldBe true
      transformationDefault.baseUnit.isInstanceOf[JsonLDInstanceDocument] shouldBe true
      transformationEditing.baseUnit.isInstanceOf[JsonLDInstanceDocument] shouldBe true
      transformationCache.baseUnit.isInstanceOf[JsonLDInstanceDocument] shouldBe true
    }
  }
}
