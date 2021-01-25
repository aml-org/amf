package amf.client.`new`.builder

import java.util.EventListener

import amf.client.`new`.{AmfEnvironment, AmfIdGenerator, PathAmfIdGenerator}
import amf.client.`new`.amfcore.AmfLogger
import amf.core.client.ParsingOptions
import amf.core.remote.Platform

import scala.concurrent.ExecutionContext

class AmfEnvironmentBuilder{

  private val resolverBuilder:AmfResolversBuilder = new AmfResolversBuilder
  private var logger: AmfLogger,
  private var listeners: List[EventListener],
  private var platform: Platform,
  private var executionContext: ExecutionContext
  private var idGenerator:AmfIdGenerator = PathAmfIdGenerator


  def resolverBuilder() :AmfResolversBuilder = resolverBuilder
  def  withlogger: AmfLogger,
  def  withlisteners: List[EventListener],
  def  withplatform: Platform,
  def  withexecutionContext: ExecutionContext

  def build() = {
    new AmfEnvironment()
  }
}

