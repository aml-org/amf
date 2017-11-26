package amf.core.interop

/**
  *
  */
import scala.scalajs.js

@js.native
trait ParsedPath extends js.Object {

  /**
    * The root of the path such as '/' or 'c:\'
    */
  var root: String = js.native

  /**
    * The full directory path such as '/home/user/dir' or 'c:\path\dir'
    */
  var dir: String = js.native

  /**
    * The file name including extension (if any) such as 'index.html'
    */
  var base: String = js.native

  /**
    * The file extension (if any) such as '.html'
    */
  var ext: String = js.native

  /**
    * The file name without extension (if any) such as 'index'
    */
  var name: String = js.native
}
