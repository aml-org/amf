package amf.parser

import amf.lexer.Token

/**
  * Created by pedro.colunga on 5/19/17.
  */
case class Document(root: ASTNode[_ <: Token], url: String)
