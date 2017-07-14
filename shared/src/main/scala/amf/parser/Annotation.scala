package amf.parser

/**
  *
  */
trait Annotation {
  def name(): String
}

case class IncludeAnnotation(url: String) extends Annotation {
  override def name(): String = "include"
}
