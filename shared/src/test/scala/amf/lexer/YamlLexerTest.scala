package amf.lexer

import amf.common.AMFToken._
import amf.common.ListAssertions
import amf.yaml.YamlLexer
import org.scalatest.FunSuite

/**
  * Created by pedro.colunga on 5/12/17.
  */
class YamlLexerTest extends FunSuite with ListAssertions {

  test("Simple mapping parse test") {
    val input                         = "a: 1"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected =
      List((StartMap, ": "), (StringToken, "a"), (IntToken, "1"), (EndMap, ""), (Eof, ""))

    assert(actual, expected)
  }

  test("Simple key value with white space parse test") {
    val input                         = "a : 1"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected =
      List((StartMap, ": "), (StringToken, "a "), (IntToken, "1"), (EndMap, ""), (Eof, ""))
    assert(actual, expected)
  }
  test("Simple single quoted mapping parse test") {
    val input                         = "'a': 1"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected =
      List((StartMap, ": "), (StringToken, "'a'"), (IntToken, "1"), (EndMap, ""), (Eof, ""))

    assert(actual, expected)
  }

  test("Simple double quoted mapping parse test") {
    val input                         = "\"a\": 1"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected =
      List((StartMap, ": "), (StringToken, "\"a\""), (IntToken, "1"), (EndMap, ""), (Eof, ""))

    assert(actual, expected)
  }

  test("Simple mapping with white space parses test") {
    val input                         = "a : 1"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected =
      List((StartMap, ": "), (StringToken, "a "), (IntToken, "1"), (EndMap, ""), (Eof, ""))
    assert(actual, expected)
  }

  test("Simple double quoted mapping with white space parses test") {
    val input                         = "\"a\" : 1"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected =
      List((StartMap, ": "), (StringToken, "\"a\" "), (IntToken, "1"), (EndMap, ""), (Eof, ""))
    assert(actual, expected)
  }

  test("Simple flow mapping parses test") {
    val input                         = "{b: 1,c: 2}"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartMap, "{"),
      (StringToken, "b:"),
      (WhiteSpace, " "),
      (IntToken, "1"),
      (Comma, ","),
      (StringToken, "c:"),
      (WhiteSpace, " "),
      (IntToken, "2"),
      (EndMap, "}"),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("Map with diff types of numbers") {
    val input                         = "b: 1\nc: \"2\"\nd: 2.4"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartMap, ": "),
      (StringToken, "b"),
      (IntToken, "1"),
      (WhiteSpace, "\\n"),
      (Comma, ": "),
      (StringToken, "c"),
      (StringToken, "\"2\""),
      (WhiteSpace, "\\n"),
      (Comma, ": "),
      (StringToken, "d"),
      (FloatToken, "2.4"),
      (EndMap, ""),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("Sequence with diff types of numbers") {
    val input                         = "- 1\n- \"2\"\n- 2.4"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartSequence, "- "),
      (IntToken, "1"),
      (WhiteSpace, "\\n"),
      (Comma, "- "),
      (StringToken, "\"2\""),
      (WhiteSpace, "\\n"),
      (Comma, "- "),
      (FloatToken, "2.4"),
      (EndSequence, ""),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("flow mapping of flow mapping parses test") {
    val input                         = "{{a: 1,b: 2},c: 3}"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartMap, "{"),
      (StartMap, "{"),
      (StringToken, "a:"),
      (WhiteSpace, " "),
      (IntToken, "1"),
      (Comma, ","),
      (StringToken, "b:"),
      (WhiteSpace, " "),
      (IntToken, "2"),
      (EndMap, "}"),
      (Comma, ","),
      (StringToken, "c:"),
      (WhiteSpace, " "),
      (IntToken, "3"),
      (EndMap, "}"),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("map of flow mapping parses test") {
    val input                         = "a : {b: 1,c: 2}"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartMap, ": "),
      (StringToken, "a "),
      (StartMap, "{"),
      (StringToken, "b:"),
      (WhiteSpace, " "),
      (IntToken, "1"),
      (Comma, ","),
      (StringToken, "c:"),
      (WhiteSpace, " "),
      (IntToken, "2"),
      (EndMap, "}"),
      (EndMap, ""),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("Simple flow sequence parse test") {
    val input                         = "[1,2,3]"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List((StartSequence, "["),
                        (IntToken, "1"),
                        (Comma, ","),
                        (IntToken, "2"),
                        (Comma, ","),
                        (IntToken, "3"),
                        (EndSequence, "]"),
                        (Eof, ""))

    assert(actual, expected)
  }

  test("flow sequence with flow sequence parse test") {
    val input                         = "[[1,2],3]"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartSequence, "["),
      (StartSequence, "["),
      (IntToken, "1"),
      (Comma, ","),
      (IntToken, "2"),
      (EndSequence, "]"),
      (Comma, ","),
      (IntToken, "3"),
      (EndSequence, "]"),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("flow sequence parse test") {
    val input                         = "a: [1,2,3]"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartMap, ": "),
      (StringToken, "a"),
      (StartSequence, "["),
      (IntToken, "1"),
      (Comma, ","),
      (IntToken, "2"),
      (Comma, ","),
      (IntToken, "3"),
      (EndSequence, "]"),
      (EndMap, ""),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("Two items mapping") {
    val input                         = "a : 1\nb: 2"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List((StartMap, ": "),
                        (StringToken, "a "),
                        (IntToken, "1"),
                        (WhiteSpace, "\\n"),
                        (Comma, ": "),
                        (StringToken, "b"),
                        (IntToken, "2"),
                        (EndMap, ""),
                        (Eof, ""))

    assert(actual, expected)
  }

  test("Two items sequence") {
    val input                         = "- a\n- b"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List((StartSequence, "- "),
                        (StringToken, "a"),
                        (WhiteSpace, "\\n"),
                        (Comma, "- "),
                        (StringToken, "b"),
                        (EndSequence, ""),
                        (Eof, ""))

    assert(actual, expected)
  }

  test("sequence of mappings") {
    val input                         = "- a: 1\n- b: 2"
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartSequence, "- "),
      (StartMap, ": "),
      (StringToken, "a"),
      (IntToken, "1"),
      (WhiteSpace, "\\n"),
      (EndMap, ""),
      (Comma, "- "),
      (StartMap, ": "),
      (StringToken, "b"),
      (IntToken, "2"),
      (EndMap, ""),
      (EndSequence, ""),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("mapping of sequences") {
    val input =
      """a:
        | - 1
        | - 2
        |b:
        | - 3""".stripMargin
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartMap, ":\\n"),
      (StringToken, "a"),
      (WhiteSpace, " "),
      (StartSequence, "- "),
      (IntToken, "1"),
      (WhiteSpace, "\\n"),
      (WhiteSpace, " "),
      (Comma, "- "),
      (IntToken, "2"),
      (WhiteSpace, "\\n"),
      (EndSequence, ":\\n"),
      (Comma, ":\\n"),
      (StringToken, "b"),
      (WhiteSpace, " "),
      (StartSequence, "- "),
      (IntToken, "3"),
      (EndSequence, ""),
      (EndMap, ""),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("sequence of  sequences") {
    val input =
      """-
        | - 1
        | - 2
        |-
        | - 3""".stripMargin
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartSequence, "-\\n"),
      (WhiteSpace, " "),
      (StartSequence, "- "),
      (IntToken, "1"),
      (WhiteSpace, "\\n"),
      (WhiteSpace, " "),
      (Comma, "- "),
      (IntToken, "2"),
      (WhiteSpace, "\\n"),
      (EndSequence, ""),
      (Comma, "-\\n"),
      (WhiteSpace, " "),
      (StartSequence, "- "),
      (IntToken, "3"),
      (EndSequence, ""),
      (EndSequence, ""),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("mapping of mappings") {
    val input =
      """q:
        | z: 1
        | x: 2
        |w:
        | c: 3""".stripMargin
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartMap, ":\\n"),
      (StringToken, "q"),
      (WhiteSpace, " "),
      (StartMap, ": "),
      (StringToken, "z"),
      (IntToken, "1"),
      (WhiteSpace, "\\n"),
      (WhiteSpace, " "),
      (Comma, ": "),
      (StringToken, "x"),
      (IntToken, "2"),
      (WhiteSpace, "\\n"),
      (EndMap, ":\\n"),
      (Comma, ":\\n"),
      (StringToken, "w"),
      (WhiteSpace, " "),
      (StartMap, ": "),
      (StringToken, "c"),
      (IntToken, "3"),
      (EndMap, ""),
      (EndMap, ""),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("mapping of mappings 3 lvl") {
    val input =
      """q:
        | z:
        |  x:
        |   y: 4
        |w:
        | c: 3""".stripMargin
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartMap, ":\\n"),
      (StringToken, "q"),
      (WhiteSpace, " "),
      (StartMap, ":\\n"),
      (StringToken, "z"),
      (WhiteSpace, "  "),
      (StartMap, ":\\n"),
      (StringToken, "x"),
      (WhiteSpace, "   "),
      (StartMap, ": "),
      (StringToken, "y"),
      (IntToken, "4"),
      (WhiteSpace, "\\n"),
      (EndMap, ":\\n"),
      (EndMap, ":\\n"),
      (EndMap, ":\\n"),
      (Comma, ":\\n"),
      (StringToken, "w"),
      (WhiteSpace, " "),
      (StartMap, ": "),
      (StringToken, "c"),
      (IntToken, "3"),
      (EndMap, ""),
      (EndMap, ""),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("test mixed asymmetric tokens types levels") {
    val input =
      """-
        | -
        |  - y:
        |      a: 1
        |      b: 2
        |-
        | - 3""".stripMargin
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartSequence, "-\\n"),
      (WhiteSpace, " "),
      (StartSequence, "-\\n"),
      (WhiteSpace, "  "),
      (StartSequence, "- "),
      (StartMap, ":\\n"),
      (StringToken, "y"),
      (WhiteSpace, "      "),
      (StartMap, ": "),
      (StringToken, "a"),
      (IntToken, "1"),
      (WhiteSpace, "\\n"),
      (WhiteSpace, "      "),
      (Comma, ": "),
      (StringToken, "b"),
      (IntToken, "2"),
      (WhiteSpace, "\\n"),
      (EndMap, ""),
      (EndMap, ""),
      (EndSequence, ""),
      (EndSequence, ""),
      (Comma, "-\\n"),
      (WhiteSpace, " "),
      (StartSequence, "- "),
      (IntToken, "3"),
      (EndSequence, ""),
      (EndSequence, ""),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("sequence of sequences 3 lvl") {
    val input =
      """-
        | -
        |  -
        |   - y: 4
        |-
        | - 3""".stripMargin
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartSequence, "-\\n"),
      (WhiteSpace, " "),
      (StartSequence, "-\\n"),
      (WhiteSpace, "  "),
      (StartSequence, "-\\n"),
      (WhiteSpace, "   "),
      (StartSequence, "- "),
      (StartMap, ": "),
      (StringToken, "y"),
      (IntToken, "4"),
      (WhiteSpace, "\\n"),
      (EndMap, ""),
      (EndSequence, ""),
      (EndSequence, ""),
      (EndSequence, ""),
      (Comma, "-\\n"),
      (WhiteSpace, " "),
      (StartSequence, "- "),
      (IntToken, "3"),
      (EndSequence, ""),
      (EndSequence, ""),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("sequence of map of sequences of map") {
    val input =
      """- q:
        |    - z: 1
        |      h: 9
        |    - x: 2
        |- w:
        |    - c: 3
        |    - v: 4""".stripMargin
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartSequence, "- "),
      (StartMap, ":\\n"),
      (StringToken, "q"),
      (WhiteSpace, "    "),
      (StartSequence, "- "),
      (StartMap, ": "),
      (StringToken, "z"),
      (IntToken, "1"),
      (WhiteSpace, "\\n"),
      (WhiteSpace, "      "),
      (Comma, ": "),
      (StringToken, "h"),
      (IntToken, "9"),
      (WhiteSpace, "\\n"),
      (WhiteSpace, "    "),
      (EndMap, ""),
      (Comma, "- "),
      (StartMap, ": "),
      (StringToken, "x"),
      (IntToken, "2"),
      (WhiteSpace, "\\n"),
      (EndMap, ""),
      (EndSequence, ""),
      (EndMap, ""),
      (Comma, "- "),
      (StartMap, ":\\n"),
      (StringToken, "w"),
      (WhiteSpace, "    "),
      (StartSequence, "- "),
      (StartMap, ": "),
      (StringToken, "c"),
      (IntToken, "3"),
      (WhiteSpace, "\\n"),
      (WhiteSpace, "    "),
      (EndMap, ""),
      (Comma, "- "),
      (StartMap, ": "),
      (StringToken, "v"),
      (IntToken, "4"),
      (EndMap, ""),
      (EndSequence, ""),
      (EndMap, ""),
      (EndSequence, ""),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("mixed types sequence") {
    val input =
      """- a: 1
        |  b: 2
        |- z""".stripMargin
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartSequence, "- "),
      (StartMap, ": "),
      (StringToken, "a"),
      (IntToken, "1"),
      (WhiteSpace, "\\n"),
      (WhiteSpace, "  "),
      (Comma, ": "),
      (StringToken, "b"),
      (IntToken, "2"),
      (WhiteSpace, "\\n"),
      (EndMap, ""),
      (Comma, "- "),
      (StringToken, "z"),
      (EndSequence, ""),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("mixed types mapping") {
    val input =
      """a: 1
        |b:
        | - 2
        | - 3""".stripMargin
    val actual: List[(Token, String)] = YamlLexer(input).lex()
    val expected = List(
      (StartMap, ": "),
      (StringToken, "a"),
      (IntToken, "1"),
      (WhiteSpace, "\\n"),
      (Comma, ":\\n"),
      (StringToken, "b"),
      (WhiteSpace, " "),
      (StartSequence, "- "),
      (IntToken, "2"),
      (WhiteSpace, "\\n"),
      (WhiteSpace, " "),
      (Comma, "- "),
      (IntToken, "3"),
      (EndSequence, ""),
      (EndMap, ""),
      (Eof, "")
    )

    assert(actual, expected)
  }

  test("Test complete yaml example") {
    val input =
      """
            | # Yaml Example
            | a: 1
            | b: !include file://include1.yaml
            | b1: true
            | c:
            |  - 2
            | --- # Document Start
            | ... # Document End
          """.stripMargin
    val actual: List[(Token, String)] = YamlLexer(input).lex()

    val expected = List(
      (WhiteSpace, "\\n"),
      (WhiteSpace, " "),
      (Comment, "# Yaml Example\\n"),
      (WhiteSpace, " "),
      (StartMap, ": "),
      (StringToken, "a"),
      (IntToken, "1"),
      (WhiteSpace, "\\n"),
      (WhiteSpace, " "),
      (Comma, ": "),
      (StringToken, "b"),
      (StringToken, "!include file://include1.yaml"),
      (WhiteSpace, "\\n"),
      (WhiteSpace, " "),
      (Comma, ": "),
      (StringToken, "b1"),
      (BooleanToken, "true"),
      (WhiteSpace, "\\n"),
      (WhiteSpace, " "),
      (Comma, ":\\n"),
      (StringToken, "c"),
      (WhiteSpace, "  "),
      (StartSequence, "- "),
      (IntToken, "2"),
      (WhiteSpace, "\\n"),
      (WhiteSpace, " "),
      (EndSequence, "---"),
      (WhiteSpace, " "),
      (Comment, "# Document Start\\n"),
      (WhiteSpace, " "),
      (EndMap, "..."),
      (WhiteSpace, " "),
      (Comment, "# Document End\\n"),
      (WhiteSpace, "          "),
      (Eof, "")
    )

    assert(actual, expected)
  }

}
