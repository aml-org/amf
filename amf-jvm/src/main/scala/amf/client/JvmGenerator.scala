package amf.client

import amf.model.BaseUnit
import amf.remote.Vendor

/**
  *
  */
class JvmGenerator extends BaseGenerator with Generator[BaseUnit] {

  /**
    * Generates the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  override def generateToFile(unit: BaseUnit, url: String, syntax: Vendor, handler: FileHandler): Unit =
    super.generateFile(unit.unit, url, syntax, handler)

  /** Generates the syntax text and returns it to the provided callback. */
  override def generateToString(unit: BaseUnit, syntax: Vendor, handler: StringHandler): Unit =
    super.generateString(unit.unit, syntax, handler)
}
