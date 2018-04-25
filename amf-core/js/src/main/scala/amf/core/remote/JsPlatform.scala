package amf.core.remote

trait JsPlatform extends Platform {
  override def findCharInCharSequence(stream: CharSequence)(p: Char => Boolean): Option[Char] = stream.toString.find(p)
}
