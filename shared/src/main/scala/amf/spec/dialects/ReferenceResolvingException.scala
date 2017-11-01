package amf.spec.dialects

import org.mulesoft.lexer.InputRange

/**
  * Created by kor on 01/11/17.
  */
class ReferenceResolvingException(message:String,range:InputRange) extends RuntimeException(message){

  def getRange():InputRange = range
}
