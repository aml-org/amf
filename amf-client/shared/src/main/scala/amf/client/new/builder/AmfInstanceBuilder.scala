package amf.client.`new`.builder

import java.util.EventListener

import amf.ProfileName
import amf.client.`new`.amfcore.plugins.GuessingParsePlugin
import amf.client.`new`.amfcore.{AmfLogger, AmfParsePlugin, AmfResolutionPipeline, AmfResolutionStep, AmfResolvePlugin, AmfValidatePlugin}
import amf.client.`new`.{AmfEnvironment, AmfIdGenerator, AmfInstance, AmfRegistry, AmfResolvers, EntitiesRegistry, ErrorHandlerProvider, PathAmfIdGenerator, PluginsRegistry}
import amf.core.client.ParsingOptions
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.model.domain.DomainElement
import amf.core.remote.{Platform, Vendor}
import amf.core.resolution.stages.selectors.Selector
import amf.internal.reference.ReferenceResolver
import amf.internal.resource.ResourceLoader

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

class AmfInstanceBuilder {

  protected val envBuilder: AmfEnvironmentBuilder = new AmfEnvironmentBuilder()
  protected var errorHandlerProvider: ErrorHandlerProvider =
  protected val registry: AmfRegistryBuilder = new AmfRegistryBuilder()
  protected val parsingOptions:ParsingOptions = new ParsingOptions

  def registryBuilder: AmfRegistryBuilder = registryBuilder

  def environmentBuilder = envBuilder

  def withErrorHandlerProvider(ehProvider: ErrorHandlerProvider) = {
    errorHandlerProvider = ehProvider
    this
  }

  def parsingOptions:ParsingOptions = parsingOptions

  def build() = {
    //init() // amf plugins initialization
    registry.build()

    new AmfInstance()
  }


}
