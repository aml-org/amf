package org.mulesoft.yaml

import org.yaml.lexer.{YamlLexer, YamlToken}

/**
  * Created by emilio.gabeiras on 6/9/17.
  */
object BasicTest  {

  val yaml = "    #aaaaa\n # hhh\n"

  def main(args: Array[String]): Unit = {
      val lexer = YamlLexer(yaml)
      while (lexer.token != YamlToken.EndStream) {
          println(YeastData(lexer.tokenData, lexer.tokenString))
          lexer.advance()
      }

  }

}




