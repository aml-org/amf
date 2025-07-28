package amf.core.common

import amf.core.internal.unsafe.PlatformSecrets
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.ExecutionContext

trait AsyncFunSuiteWithPlatformGlobalExecutionContext extends AsyncFunSuite with PlatformSecrets {
  override implicit def executionContext: ExecutionContext = platform.globalExecutionContext
}
