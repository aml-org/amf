package amf.shapes.internal.domain.resolution.shape_normalization

case class ShapeNormalizationLogger() {
  private val enabled = false
  private var padding = ""
  private var step    = 0
  def log(msg: String): Unit = {
    if (enabled) {
      println(s"[step $step] $padding$msg")
      step += 1
    }
  }
  def addPadding(): Unit    = padding = s"$padding\t"
  def removePadding(): Unit = padding = padding.substring(0, Math.max(0, padding.length - 1))

}
