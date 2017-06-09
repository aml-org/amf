package amf.parser

/**
  * Created by pedro.colunga on 5/18/17.
  */
trait Annotation {
    def name(): String
}

case class IncludeAnnotation(url: String) extends Annotation {
    override def name(): String = "include"
}
