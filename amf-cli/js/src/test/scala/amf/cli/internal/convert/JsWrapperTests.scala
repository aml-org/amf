package amf.cli.internal.convert

import amf.core.internal.convert.NativeOpsFromJs

import scala.language.implicitConversions

class JsWrapperTests extends WrapperTests with NativeOpsFromJs {
  override def getAbsolutePath(path: String): String = {
    // temp: absolute path should be implemented at common.
    // We need to figured out how to get absolute path from js server platform.
    if (path.startsWith("/")) "file:/" + path
    else "file://" + path
  }

}
