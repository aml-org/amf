package amf.client

import java.util.concurrent.CompletableFuture

import amf.model.BaseUnit
import amf.remote.Vendor

import scala.compat.java8.FutureConverters

/**
  *
  */
class JvmGenerator extends BaseGenerator with Generator[BaseUnit] {

  override type FH = FileHandler
  override type SH = StringHandler

  /**
    * Generates the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  override def generateToFile(unit: BaseUnit, url: String, syntax: Vendor, handler: FileHandler): Unit =
    super.generateAndHanldeFile(unit.unit, url, syntax, handler)

  /** Generates the syntax text and returns it to the provided callback. */
  override def generateToString(unit: BaseUnit, syntax: Vendor, handler: StringHandler): Unit =
    super.generateAndHandleString(unit.unit, syntax, handler)

  /**
    * Generates asynchronously the syntax text and stores it in the file pointed by the provided URL.
    * It must throw a UnsupportedOperation exception in platforms without support to write to the file system
    * (like the browser) or if a remote URL is provided.
    */
  def generateToFileAsync(unit: BaseUnit, url: String, syntax: Vendor): CompletableFuture[String] =
    FutureConverters.toJava(super.generateFile(unit.unit, url, syntax)).toCompletableFuture

  /** Generates the syntax text and returns it  asynchronously. */
  def generateToStringAsync(unit: BaseUnit, syntax: Vendor): CompletableFuture[String] =
    FutureConverters.toJava(super.generateString(unit.unit, syntax)).toCompletableFuture

}
