package amf.lexer

import amf.common.ListAssertions
import amf.yaml.YamlToken._
import amf.yaml.{YamlLexer, YamlToken}
import org.scalatest.FunSuite

/**
  * Created by pedro.colunga on 5/12/17.
  */

class YamlLexerTest extends FunSuite with ListAssertions{

  test("Simple key value parse test"){
    val input= "a: 1"
    val actual: List[(Token,String)] = YamlLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap,": "), (StringToken,"a"),
    (StringToken,"1"),(EndMap,""),(Eof,""))

    assert(actual,expected)
  }

  test("Simple key value with white space parse test"){
    val input= "a : 1"
    val actual: List[(Token,String)] = YamlLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap,": "), (StringToken,"a "),
      (StringToken,"1"),(EndMap,""),(Eof,""))
    assert(actual,expected)
  }

  test("Test complete yaml example"){
      val input =
          """
            | # Yaml Example
            | a: 1
            | b: !include file://include1.yaml
            | c:
            |  - 2
            | --- # Document Start
            | ... # Document End
          """.stripMargin
      val actual: List[(Token,String)]= YamlLexer(input).lex()

    val expected = List((WhiteSpace,"\\n") , (WhiteSpace," "), (Comment,"# Yaml Example\\n"),
      (WhiteSpace," ") ,(StartMap,": "),(StringToken,"a") ,(StringToken,"1"),
      (WhiteSpace,"\\n"),(WhiteSpace," "), (Comma,": " ), (StringToken,"b"),
      (Link,"!include " ), (StringToken,"file://include1.yaml"), (WhiteSpace, "\\n"), (WhiteSpace," "),
      (Comma,":\\n"), (StringToken,"c"), (WhiteSpace,"  "), (StartSequence,"- " ), (StringToken,"2"),
      (WhiteSpace,"\\n") ,(WhiteSpace," "), (EndSequence,"---"), (WhiteSpace," "),
      (Comment,"# Document Start\\n"), (WhiteSpace," "), (EndMap,"..."), (WhiteSpace," "),
      (Comment,"# Document End\\n"), (WhiteSpace,"          "), (Eof,""))

    assert(actual,expected)
  }
}