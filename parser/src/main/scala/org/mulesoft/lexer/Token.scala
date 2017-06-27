package org.mulesoft.lexer

/**
  * The Token Trait
  */
abstract class Token(val name:String)

/**
  * The Token data
  */
case class TokenData[T <: Token](token:T, range:InputRange, start:Int, end:Int)
