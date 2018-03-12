package amf.plugins.document.vocabularies.emitters.common

class IdCounter {
  private var c = 0

  def genId(id: String): String = {
    c += 1
    s"${id}_$c"
  }

  def reset(): Unit = c = 0
}