package amf.core.parser

import amf.core.model.domain.Linkable

case class DeclarationPromise(private val success: (Linkable) => Any,
                              private val failure: () => Any,
                              var resolved: Boolean = false) {

  def resolve(element: Linkable): Unit = {
    resolved = true
    success(element)
  }

  def fail(): Unit = {
    resolved = true
    failure()
  }
}

trait FutureDeclarations {

  var promises: Map[String, Seq[DeclarationPromise]] = Map()

  def futureRef(name: String, promise: DeclarationPromise): Unit = {
    val otherPromises = promises.getOrElse(name, Seq())
    promises = promises.updated(name, otherPromises ++ Seq(promise))
  }

  def resolveRef(name: String, value: Linkable): Unit = {
    promises.getOrElse(name, Seq()).foreach { _.resolve(value) }
    promises = promises.updated(name, Seq())
  }

}
