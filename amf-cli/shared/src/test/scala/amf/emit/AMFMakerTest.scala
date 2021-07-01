package amf.emit

import amf.apicontract.client.scala.APIConfiguration
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.remote._
import amf.core.internal.render.AMFSerializer
import amf.core.internal.unsafe.PlatformSecrets
import org.mulesoft.common.test.ListAssertions
import org.scalatest.Matchers._
import org.scalatest.{Assertion, FunSuite}
import org.yaml.model.YMap
import amf.core.internal.parser._

class AMFMakerTest extends FunSuite with AMFUnitFixtureTest with ListAssertions with PlatformSecrets {

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
    val config = APIConfiguration.API()
    config.baseUnitClient().renderAST(document, vendor.mediaType) match {
      case doc: SyamlParsedDocument => doc.document.node.as[YMap]
      case _                        => YMap.empty
    }
  }
}
