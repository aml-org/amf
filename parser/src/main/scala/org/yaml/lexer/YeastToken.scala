package org.yaml.lexer

import org.mulesoft.lexer.TokenData

/**
  * Created by emilio.gabeiras on 8/16/17.
  */
case class YeastToken(tokenType: YamlToken, start: Int, end:Int) {
    override def toString: String = s"$tokenType($start, $end)"
}

