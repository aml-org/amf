package amf.emit

import amf.Core
import amf.client.environment.{AsyncAPIConfiguration, WebAPIConfiguration}
import amf.core.AMFSerializer
import amf.core.model.document.Document
import amf.core.parser._
import amf.core.remote._
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.document.webapi._
import amf.plugins.domain.VocabulariesRegister
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.APIDomainPlugin
import amf.plugins.syntax.SYamlSyntaxPlugin
import org.mulesoft.common.test.ListAssertions
import org.scalatest.Matchers._
import org.scalatest.{Assertion, FunSuite}
import org.yaml.model.YMap

class AMFMakerTest extends FunSuite with AMFUnitFixtureTest with ListAssertions {

  test("Test simple Raml generation") {
    val root = ast(`document/api/bare`, Raml10)
    assertNode(root, ("title", "test"))
    assertNode(root, ("description", "test description"))
  }

  test("Test simple Oas generation") {
    val root = ast(`document/api/bare`, Oas20)
    assertNode(root, ("info", List(("title", "test"), ("description", "test description"))))
  }

  test("Test complete Oas generation") {
    val root = ast(`document/api/basic`, Oas20)

    assertNode(
      root,
      ("info",
       List(
         ("title", "test"),
         ("description", "test description"),
         ("version", "1.1"),
         ("termsOfService", "termsOfService"),
         ("license", List(("url", "licenseUrl"), ("name", "licenseName"))),
         ("contact", List(("url", "organizationUrl"), ("name", "organizationName"), ("email", "test@test")))
       ))
    )
    assertNode(root, ("schemes", Array("http", "https")))
    assertNode(root, ("basePath", "/api"))
    assertNode(root, ("host", "localhost.com"))
    assertNode(root, ("consumes", Array("application/json")))
    assertNode(root, ("produces", Array("application/json")))

    assertNode(root,
               ("externalDocs",
                List(
                  ("url", "creativoWorkUrl"),
                  ("description", "creativeWorkDescription")
                )))

  }

  test("Test complete Raml generation") {
    val root = ast(`document/api/basic`, Raml10)
    assertNode(root, ("title", "test"))
    assertNode(root, ("description", "test description"))

    assertNode(root, ("version", "1.1"))
    assertNode(root, ("(amf-termsOfService)", "termsOfService"))
    assertNode(root, ("(amf-license)", List(("url", "licenseUrl"), ("name", "licenseName"))))

    assertNode(root, ("protocols", Array("http", "https")))
    assertNode(root, ("baseUri", "localhost.com/api"))

    assertNode(root, ("mediaType", Array("application/json")))

    assertNode(
      root,
      ("(amf-contact)", List(("url", "organizationUrl"), ("name", "organizationName"), ("email", "test@test"))))

//    assertNode(root,
//               ("(amf-externalDocs)",
//                List(
//                  ("url", "creativoWorkUrl"),
//                  ("description", "creativeWorkDescription")
//                )))
    // todo: assert node of Array[List[tuple]] ??
  }

  test("Test Raml generation with operations") {
    val root = ast(`document/api/advanced`, Raml10)
    assertNode(root,
               ("/endpoint", List(("get", List(("description", "test operation get"), ("displayName", "test get"))))))
  }

  test("Test Oas generation with operations") {
    val root = ast(`document/api/advanced`, Oas20)
    assertNode(
      root,
      ("paths",
       List(("/endpoint", List(("get", List(("description", "test operation get"), ("operationId", "test get"))))))))
  }

  private def assertNode(container: YMap, expected: (String, Any)): Assertion = {
    expected match {
      case (k, v) =>
        container.key(k) match {
          case Some(entry) =>
            val value = entry.value
            v match {
              case x: String =>
                entry.value.as[String] should be(x)
              case l: Array[String] =>
                assert(l.toList, entry.value.as[Seq[String]].toList)
              case l: List[Any] =>
                val obj = value.as[YMap]
                l.map(e => assertNode(obj, e.asInstanceOf[(String, Any)]))
            }
          case None => notFound(k)
        }
    }
    succeed
  }

  private def notFound(field: String): Assertion = {
    fail(s"Field $field not found in tree where was expected to be")
  }

  private def ast(document: Document, vendor: Vendor): YMap = {

    Core.init()
    // Remod registering
    VocabulariesRegister.register(platform)
    amf.core.registries.AMFPluginsRegistry.registerSyntaxPlugin(SYamlSyntaxPlugin)
    amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Raml10Plugin)
    amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Raml08Plugin)
    amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Oas20Plugin)
    amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Oas30Plugin)
    amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
    amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
    amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(APIDomainPlugin)
    amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(DataShapesDomainPlugin)
    amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(JsonSchemaPlugin)

    val mediaType = vendor match {
      case Aml     => "application/yaml"
      case Amf     => "application/ld+json"
      case Payload => "application/amf+json"
      case r: Raml => "application/yaml"
      case Oas20   => "application/json"
      case _       => ""
    }

    val config = WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20())

    val serializer = new AMFSerializer(document, vendor.mediaType, config.renderConfiguration)

    serializer
      .renderAsYDocument(serializer.getRenderPlugin)
      .document
      .node
      .as[YMap]
  }
}
