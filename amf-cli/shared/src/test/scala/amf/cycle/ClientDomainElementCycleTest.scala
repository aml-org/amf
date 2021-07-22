package amf.cycle
import amf.apicontract.client.platform.AMFConfiguration
import amf.apicontract.client.platform.render.ApiDomainElementEmitter
import amf.apicontract.client.scala.{AMFConfiguration => InternalAMFConfiguration}
import amf.core.client.scala.errorhandling.DefaultErrorHandler
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.convert.ClientErrorHandlerConverter.ErrorHandlerConverter
import amf.core.internal.remote.{Raml10YamlHint, Vendor}
import org.yaml.builder.YamlOutputBuilder
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.testing.{Oas30Json, Raml10Yaml}

class ClientOas30ElementCycleTest extends ClientDomainElementCycleTest {

  override def basePath: String = "amf-cli/shared/src/test/resources/cycle/oas30/"
  val upanddownPath: String     = "amf-cli/shared/src/test/resources/upanddown/oas3/"
  val vendor: Vendor            = Vendor.OAS30

  test("type - composition with refs and inlined") {
    renderElement(
      "type/composition-with-refs.json",
      CommonExtractors.declaresIndex(0),
      "type/login-response-emission.yaml",
      Oas30Json
    )
  }

  test("parameter - cookie parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(2),
      "parameter/cookie-param.yaml",
      Oas30Json
    )
  }

  test("parameter - explicit header") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(5),
      "parameter/explicit-header.yaml",
      Oas30Json
    )
  }
}

class ClientRaml10ElementCycleTest extends ClientDomainElementCycleTest {

  val basePath: String       = "amf-cli/shared/src/test/resources/cycle/raml10/"
  val jsonSchemaPath: String = "amf-cli/shared/src/test/resources/org/raml/json_schema/"
  val vendor: Vendor         = Vendor.RAML10

  test("type - multiple inheritance with union and properties") {
    renderElement(
      "type/complex-inheritance-unions.raml",
      CommonExtractors.declaredWithName("root"),
      "type/complex-inheritance-unions.yaml",
      Raml10Yaml
    )
  }

}

trait ClientDomainElementCycleTest extends DomainElementCycleTest {
  override def renderDomainElement(element: Option[DomainElement], amfConfig: InternalAMFConfiguration): String = {
    val platformConfig: AMFConfiguration = amfConfig
    element
      .map { interalElement =>
        val stringBuilder = YamlOutputBuilder()
        platformConfig.elementClient().renderToBuilder(interalElement, vendor.mediaType, stringBuilder)
        stringBuilder.result.toString
      }
      .getOrElse("")
  }
}
