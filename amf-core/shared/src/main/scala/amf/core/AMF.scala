package amf.core

import amf.plugins.features.validation.ParserSideValidationPlugin

object AMF {
  def init(): Unit = {
    AMFCompiler.init()
    AMFSerializer.init()
    ParserSideValidationPlugin.init()
  }
}
