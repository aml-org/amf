package amf.emit

import amf.parser.ASTNode
import amf.remote.{Oas, Raml}
import org.scalatest.Matchers._
import org.scalatest.{Assertion, FunSuite}

class AMFMakerTest extends FunSuite with AMFUnitFixtureTest {

  test("Test simple Raml generation") {
    val root = ast(`document/api/bare`, Raml)
    assertRamlTree(root, List(("title", "test"), ("description", "test description")))

  }

  test("Test simple Oas generation") {
    val root = ast(`document/api/bare`, Oas)
    assertNode(root.children.head, ("info", List(("title", "test"), ("description", "test description"))))

  }

  test("Test complete Oas generation") {
    val root = ast(`document/api/basic`, Oas)

    assertNode(
      root.children.head,
      ("info",
       List(
         ("title", "test"),
         ("description", "test description"),
         ("version", "1.1"),
         ("termsOfService", "termsOfService"),
         ("license", List(("url", "licenseUrl"), ("name", "licenseName")))
       ))
    )
    assertNode(root.children.head, ("schemes", List("http", "http")))
    assertNode(root.children.head, ("basePath", "http://localhost.com/api"))
    assertNode(root.children.head, ("host", "http://localhost.com/api"))
    assertNode(root.children.head, ("consumes", List("application/json")))
    assertNode(root.children.head, ("produces", List("application/json")))
    assertNode(root.children.head,
               ("contact", List(("url", "organizationUrl"), ("name", "organizationName"), ("email", "test@test"))))

    assertNode(root.children.head,
               ("externalDocs",
                List(
                  ("url", "creativoWorkUrl"),
                  ("description", "creativeWorkDescription")
                )))

  }

  test("Test complete Raml generation") {
    val root = ast(`document/api/basic`, Raml)
    assertNode(root.last, ("title", "test"))
    assertNode(root.last, ("description", "test description"))

    assertNode(root.last, ("version", "1.1"))
    assertNode(root.last, ("(termsOfService)", "termsOfService"))
    assertNode(root.last, ("(license)", List(("url", "licenseUrl"), ("name", "licenseName"))))

    assertNode(root.last, ("protocols", List("http", "http")))
    assertNode(root.last, ("baseUri", "http://localhost.com/api"))

    assertNode(root.last, ("mediaType", List("application/json")))

    assertNode(root.last,
               ("(contact)", List(("url", "organizationUrl"), ("name", "organizationName"), ("email", "test@test"))))

    assertNode(root.last,
               ("(externalDocs)",
                List(
                  ("url", "creativoWorkUrl"),
                  ("description", "creativeWorkDescription")
                )))
  }

  test("Test Raml generation with operations") {
    val root = ast(`document/api/advanced`, Raml)
    assertNode(root.last,
               ("/endpoint", List(("get", List(("description", "test operation get"), ("displayName", "test get"))))))
  }

  test("Test Oas generation with operations") {
    val root = ast(`document/api/advanced`, Oas)
    assertNode(
      root.last,
      ("paths",
       List(("/endpoint", List(("get", List(("description", "test operation get"), ("operationId", "test get"))))))))
  }

//list of triple key -> value
  def assertRamlTree(root: ASTNode[_], expected: (List[(String, String)])): Assertion = {
    expected
      .map(e => {
        val value = root.last.children
          .find(c => c.children.head.content.startsWith(e._1))
        if (value.isEmpty) throwNotFound(e._1)
        value.get.children(1).content should be(e._2)
      })
      .count(a => a != succeed) should be(0)
  }

  def assertNode(conainter: ASTNode[_], expected: (String, Any)): Assertion = {
    val infoNode = conainter.children
      .find(c => c.children.head.content.startsWith(expected._1))
    if (infoNode.isEmpty) throwNotFound(expected._1)
    expected._2 match {
      case x: String => infoNode.get.children(1).content should be(x)
      case l: List[Any] if l.head.isInstanceOf[String] =>
        l.map(i => {
            val maybeN = infoNode.get.children(1).children.find(c => c.content == i)
            maybeN.isDefined should be(true)
          })
          .count(p => p != succeed) should be(0)
      case l: List[Any] if l.head.isInstanceOf[(String, Any)] =>
        l.map(e => { assertNode(infoNode.get.children(1), e.asInstanceOf[(String, Any)]) })
          .count(e => e != succeed) should be(0)
      case _ => ???
      //TODO hanlde secuences
    }
  }

  def throwFail(field: String, expected: String, actual: String): Assertion = {
    fail(s"Field $field expected: $expected but actual: $actual")
  }

  def throwNotFound(field: String): Assertion = {
    fail(s"Field $field not found in tree where was expected to be")
  }

}
