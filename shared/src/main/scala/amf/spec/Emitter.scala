package amf.spec

import amf.parser.Position

/**
  * Created by pedro.colunga on 8/22/17.
  */
trait Emitter {
  def emit(): Unit

  def position(): Position
}
