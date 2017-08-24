package amf.io

import amf.unsafe.PlatformSecrets

/**
  * Temporary file and directory creator
  */
trait TmpTests extends PlatformSecrets {

  /** Return random temporary file name for testing. */
  def tmp(name: String = ""): String = platform.tmpdir() + System.nanoTime() + "-" + name

}
