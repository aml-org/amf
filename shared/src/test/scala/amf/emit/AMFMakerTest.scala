package amf.emit

import amf.common.AMFToken.{Entry, MapToken, SequenceToken, StringToken}
import amf.parser.ASTNode
import amf.remote.{Amf, Oas, Raml}
import org.scalatest.Matchers._
import org.scalatest.{Assertion, FunSuite}

class AMFMakerTest extends FunSuite with AMFUnitFixtureTest {

  test("Test simple raml generation") {
    val root = buildSimpleUnit(Raml)
    assertRamlTree(root, List(("title", "test"), ("description", "test description")))

  }

  test("test simple oas generation") {
    val root = buildSimpleUnit(Oas)
    assertNode(root.children.head, ("info", List(("title", "test"), ("description", "test description"))))

  }

  ignore("test simple JSONLD generation") {
    val root = buildSimpleUnit(Amf)

    //root-map-entry
    root.children.head.children.size should be(1)
    val encodes = root.children.head.children.head
    encodes.`type` should be(Entry)
    encodes.children.size should be(2)
    encodes.children.head.content should be("http://raml.org/vocabularies/document#encodes")
    encodes.children(1).`type` should be(SequenceToken)
    encodes.children(1).children.size should be(1)
    val encodesMap = encodes.children(1).children.head
    encodesMap.`type` should be(MapToken)
    encodesMap.children.size should be(3)

    val nameEntry = encodesMap.children.head
    assertJsonLdEntry("http://schema.org/name", "test", nameEntry)

    val hostEntry = encodesMap.children(1)
    assertJsonLdEntry("http://raml.org/vocabularies/http#host", "http://localhost.com/api", hostEntry)

    val schemesEntry = encodesMap.children(2)
    assertJsonLdSequence("http://raml.org/vocabularies/http#scheme", List("http", "https"), schemesEntry)
  }

  def assertJsonLdSequence(key: String, values: List[String], entryNode: ASTNode[_]): Assertion = {
    assertEntryKey(key, entryNode)
    val content = entryNode.children(1)
    content.`type` should be(SequenceToken)
    content.children.size should be(values.size)
    val mapValues = content.children
    mapValues.head.`type` should be(MapToken)
    values
      .map(v => {
        val value1 = mapValues.find(c => c.children.head.children(1).content == v).get
        value1.children.size should be(1)
        val container = value1.children.head
        container.`type` should be(Entry)
        container.children.size should be(2)
        container.head.content should be("@value")
        container.children(1).content should be(v)
      })
      .count(s => s != succeed) should be(0)

  }

  def assertEntryKey(key: String, entryNode: ASTNode[_]): Assertion = {
    entryNode.`type` should be(Entry)
    entryNode.children.size should be(2)
    val head = entryNode.children.head
    head.content should be(key)
    head.`type` should be(StringToken)
  }

  def assertJsonLdEntry(key: String, value: String, entryNode: ASTNode[_]): Assertion = {
    assertEntryKey(key, entryNode)
    val content = entryNode.children(1)
    content.`type` should be(SequenceToken)
    content.children.size should be(1)
    val mapContent = content.children.head
    mapContent.children.size should be(1)
    mapContent.children.head.`type` should be(Entry)
    val entry = mapContent.children.head
    entry.children.head.content should be("@value")
    val entryValue = entry.children(1)
    entryValue.content should be(value)

  }

  test("test complete oas generation") {
    val root = buildCompleteUnit(Oas)

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
    assertNode(root.children.head, ("consumes", "application/json"))
    assertNode(root.children.head, ("produces", "application/json"))
    assertNode(root.children.head,
               ("contact", List(("url", "organizationUrl"), ("name", "organizationName"), ("email", "test@test"))))

    assertNode(root.children.head,
               ("externalDocs",
                List(
                  ("url", "creativoWorkUrl"),
                  ("description", "creativeWorkDescription")
                )))

  }

  test("test complete raml generation") {
    val root = buildCompleteUnit(Raml)
    assertNode(root.last, ("title", "test"))
    assertNode(root.last, ("description", "test description"))

    assertNode(root.last, ("version", "1.1"))
    assertNode(root.last, ("termsOfService", "termsOfService"))
    assertNode(root.last, ("license", List(("url", "licenseUrl"), ("name", "licenseName"))))

    assertNode(root.last, ("protocols", List("http", "http")))
    assertNode(root.last, ("baseUri", "http://localhost.com/api"))

    assertNode(root.last, ("mediaType", "application/json"))

    assertNode(root.last,
               ("contact", List(("url", "organizationUrl"), ("name", "organizationName"), ("email", "test@test"))))

    assertNode(root.last,
               ("externalDocs",
                List(
                  ("url", "creativoWorkUrl"),
                  ("description", "creativeWorkDescription")
                )))
  }

  test("test raml generation with operations") {
    val root = buildExtendedUnit(Raml)
    assertNode(root.last,
               ("/endpoint", List(("get", List(("description", "test operation get"), ("title", "test get"))))))
  }

  test("test oas generation with operations") {
    val root = buildExtendedUnit(Oas)
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
