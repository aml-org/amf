package amf.cycle
import amf.client.parse.DefaultErrorHandler
import amf.client.render.WebApiDomainElementEmitter
import amf.client.resolve.ClientErrorHandlerConverter.ErrorHandlerConverter
import amf.core.model.domain.DomainElement
import amf.core.remote.{OasJsonHint, RamlYamlHint, Vendor}
import org.yaml.builder.YamlOutputBuilder
import amf.client.convert.CoreClientConverters._

class ClientOas30ElementCycleTest extends ClientDomainElementCycleTest {

  override def basePath: String = "amf-client/shared/src/test/resources/cycle/oas30/"
  val upanddownPath: String     = "amf-client/shared/src/test/resources/upanddown/oas3/"
  val vendor: Vendor            = Vendor.OAS30

  test("type - composition with refs and inlined") {
    renderElement(
      "type/composition-with-refs.json",
      CommonExtractors.declaresIndex(0),
      "type/login-response-emission.yaml",
      OasJsonHint
    )
  }

  test("parameter - cookie parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(2),
      "parameter/cookie-param.yaml",
      OasJsonHint
    )
  }

  test("parameter - explicit header") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(5),
      "parameter/explicit-header.yaml",
      OasJsonHint
    )
  }
}

class ClientRaml10ElementCycleTest extends ClientDomainElementCycleTest {

  val basePath: String       = "amf-client/shared/src/test/resources/cycle/raml10/"
  val jsonSchemaPath: String = "amf-client/shared/src/test/resources/org/raml/json_schema/"
  val vendor: Vendor         = Vendor.RAML10

  test("type - multiple inheritance with union and properties") {
    renderElement(
      "type/complex-inheritance-unions.raml",
      CommonExtractors.declaredWithName("root"),
      "type/complex-inheritance-unions.yaml",
      RamlYamlHint
    )
  }

}

trait ClientDomainElementCycleTest extends DomainElementCycleTest {
  override def renderDomainElement(element: Option[DomainElement]): String =
    element
      .map { interalElement =>
        val stringBuilder = YamlOutputBuilder()
        val eh            = ErrorHandlerConverter.asClient(DefaultErrorHandler())
        WebApiDomainElementEmitter.emitToBuilder(interalElement, vendor, eh, stringBuilder)
        stringBuilder.result.toString
      }
      .getOrElse("")
}
