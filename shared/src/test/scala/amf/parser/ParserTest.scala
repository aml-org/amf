package amf.parser

import amf.common.AMFToken._
import amf.common.core.Strings
import amf.common.{AMFAST, AMFToken}
import amf.json.JsonLexer
import amf.lexer.CharSequenceStream
import amf.oas.OasParser
import amf.raml.RamlParser
import amf.remote.{Content, Platform}
import amf.yaml.YamlLexer
import org.scalatest.FunSuite
import org.scalatest.Matchers._

import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ParserTest extends FunSuite {
  private val `RAML/yaml`: String =
    """a: 1
          |b: !include include1.yaml
          |c:
          |  - 2
          |  - 3
          |d: !include include2.yaml""".stripMargin

  private val `RAML/json`: String =
    """{
          |  "a": 1,
          |  "b": "!include include1.yaml",
          |  "c": [
          |    2,
          |    3
          |  ],
          |  "d": "!include include2.yaml"
          |}""".stripMargin

  private val `OAS/json`: String =
    """{
          |  "a": 1,
          |  "b": {
          |    "$ref": "include1.json"
          |  },
          |  "c": [
          |    2,
          |    3
          |  ],
          |  "d": {
          |    "$ref": "include2.json"
          |  }
          |}""".stripMargin

  private val `OAS/yaml`: String =
    """a: 1
          |b:
          |  $ref: include1.yaml
          |c:
          |  - 2
          |  - 3
          |d:
          |  $ref: include2.yaml""".stripMargin

  test("Test RamlParser with RAML/yaml") {
    val builder = YeastASTBuilder(YamlLexer(`RAML/yaml`))
    val parser  = new RamlParser(builder)

    val root = builder.root() { () =>
      parser.parse()
    }

    assertDocumentRoot(root)
  }

  ignore("Test RamlParser with RAML/json") {
    val builder = YeastASTBuilder(JsonLexer(`RAML/json`))
    val parser  = new RamlParser(builder)

    val root = builder.root() { () =>
      parser.parse()
    }

    assertDocumentRoot(root)
  }

  test("Test OASParser with OAS/json") {
    val builder = YeastASTBuilder(JsonLexer(`OAS/json`))
    val parser  = new OasParser(builder)

    val root = builder.root() { () =>
      parser.parse()
    }

    assertDocumentRoot(root)
  }

  test("Test OASParser with OAS/yaml") {
    val builder = YeastASTBuilder(YamlLexer(`OAS/yaml`))
    val parser  = new OasParser(builder)

    val root = builder.root() { () =>
      parser.parse()
    }

    assertDocumentRoot(root)
  }

  def typed(head: AMFAST): Any = head.`type` match {
    case IntToken    => head.content.toInt
    case StringToken => head.content.unquote
    case _           => head.content
  }

  private def assertDocumentRoot(root: AMFAST) = {
    // Assert AST tree
    assert(root.children.length == 1)
    assert(root.`type` equals Root)
    assert(root.content == null)

    val content = root.head
    assert(content.children.length == 4)
    assert(content.`type` equals MapToken)
    assert(content.content == null)

    val first = content.child(0)
    typed(first.head) shouldBe "a"
    typed(first.last) shouldBe 1

    val second = content.child(1)
    typed(second.head) shouldBe "b"
    typed(second.last).toString should startWith("include1.")

    val third = content.child(2)
    assert(third.content == null)
    typed(third.head) shouldBe "c"
    assertYamlSequence(third.last)

    val fourth = content.child(3)
    typed(fourth.head) shouldBe "d"
    typed(fourth.last).toString should startWith("include2.")

    assert(content.child(4) eq root.empty)
  }

  private def assertYamlSequence(sequence: AMFAST) = {
    assert(sequence.content == null)
    sequence.`type` should be(SequenceToken)

    val v0 = sequence.child(0)
    typed(v0) shouldBe 2
    assert(v0.children isEmpty)
    val v1 = sequence.child(1)
    typed(v1) shouldBe 3
    assert(v1.children isEmpty)
  }

  private def assertYamlEntry(node: AMFAST, expected: Range) = {
    assert(node.children.length == 2)
    node.`type` should be(Entry)
    assert(node.content == null)
    node.range should be(expected)
  }

  private def assertYamlLink(link: AMFAST, expected: Range) = {
    link.`type` should be(Link)
    assert(link.children.isEmpty)
    link.range should be(expected)
    /*val include = link.asInstanceOf[YamlASTLink]
        val root = include.target.root.asInstanceOf[YamlAST]
        assert(root.children.length == 1)

        val entry = root.head.head
        assertYamlEntry(entry, Range((1, 0), (1, 5)))
        assert(entry.last.content equals "0")
        assert(entry.last.range equals Range((1, 4), (1, 5)))*/
  }

  private def assertOASRoot(root: AMFAST) = {
    // Assert AST tree ...
    assert(root.children.length == 1)
  }

  private def assertJsonEntry(node: AMFAST, expected: Range) = {
    assert(node.children.length == 2)
    assert(node.`type` equals AMFToken.Entry)
    assert(node.content == null)
    assert(node.range equals expected)
  }

  private class TestMemoryPlatform extends Platform {

    /** Resolve specified file. */
    override protected def fetchFile(path: String): Future[Content] = {
      path match {
        case "input.yaml" =>
          Future {
            Content(new CharSequenceStream(`RAML/yaml`), path)
          }
        case "include1.yaml" =>
          Future {
            Content(new CharSequenceStream("aa: 0"), path)
          }
        case "include2.yaml" =>
          Future {
            Content(new CharSequenceStream("dd: 0"), path)
          }
        case "input.json" =>
          Future {
            Content(new CharSequenceStream(`OAS/json`), path)
          }
        case "include1.json" =>
          Future {
            Content(new CharSequenceStream("{\"aa\": 0}"), path)
          }
        case "include2.json" =>
          Future {
            Content(new CharSequenceStream("{\"dd\": 0}"), path)
          }
        case _ => Future.failed(new Exception(s"[TEST] Unable to load $path"))
      }
    }

    override protected def fetchHttp(url: String): Future[Content] =
      Future.failed(new Exception(s"[TEST] Unable to fetch url $url"))

    override protected def writeFile(path: String, content: String): Future[String] =
      Future.failed(new Exception(s"[TEST] Unsupported write operation $path"))

    override def tmpdir(): String = throw new Exception(s"[TEST] Unsupported tmpdir operation")

    override def resolvePath(path: String): String = path
  }

}
