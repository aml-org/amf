package amf.emit

import amf.parser.{ASTNode, Document}
import amf.remote.{Oas, Raml}
import org.scalatest.Matchers._
import org.scalatest.{Assertion, FunSuite}

class SpecEmitTest extends FunSuite with AMFUnitFixtureTest {

  test("test simple raml generation") {
    val unit = buildSimpleUnit(Raml)
    unit.`type` should be(Document)
    assertRamlTree(unit.root, List(("title", "test"), ("description", "test description")))

  }

  test("test simple oas generation") {
    val unit = buildSimpleUnit(Oas)

    unit.`type` should be(Document)
    assertNode(unit.root.children.head, ("info", List(("title", "test"), ("description", "test description"))))

  }

//list of triple key -> value
  def assertRamlTree(root: ASTNode[_], expected: (List[(String, String)])): Assertion = {
    expected
      .map(e => {
        val value = root.children.head.children
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
