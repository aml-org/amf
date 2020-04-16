package amf.plugins.document.webapi.parser.spec.oas

import amf.core.model.domain.AmfScalar
import amf.core.parser.Annotations
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorCallback
import amf.plugins.domain.webapi.metamodel.CallbackModel
import amf.plugins.domain.webapi.models.{Callback, EndPoint}
import amf.plugins.features.validation.CoreValidations
import org.yaml.model.{YMap, YMapEntry, YScalar}

import scala.collection.mutable

/**
  * A single named callback may be parsed into multiple Callback when multiple expressions are defined.
  * This is due to inconsistency in the model, pending refactor in APIMF-1771
  */
case class Oas30CallbackParser(map: YMap, adopt: Callback => Unit, name: String, rootEntry: YMapEntry)(
    implicit val ctx: OasWebApiContext) {
  def parse(): List[Callback] = {
    ctx.link(map) match {
      case Left(fullRef) =>
        val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "callbacks")
        ctx.declarations
          .findCallbackInDeclarations(label)
          .map { callbacks =>
            callbacks.map { callback =>
              val linkCallback: Callback = callback.link(label, Annotations(map))
              adopt(linkCallback)
              linkCallback
            }
          }
          .getOrElse {
            ctx.navigateToRemoteYNode(fullRef) match {
              case Some(navigation) =>
                Oas30CallbackParser(navigation.remoteNode.as[YMap], adopt, name, rootEntry)(navigation.context).parse()
              case None =>
                ctx.eh.violation(CoreValidations.UnresolvedReference,
                                 "",
                                 s"Cannot find callback reference $fullRef",
                                 map)
                val callback: Callback = new ErrorCallback(label, map).link(name, Annotations(rootEntry))

                adopt(callback)
                List(callback)
            }
          }
      case Right(_) =>
        val callbackEntries = map.entries
        callbackEntries.map { entry =>
          val expression = entry.key.as[YScalar].text
          val callback   = Callback().add(Annotations(entry))
          callback.fields.setWithoutId(CallbackModel.Expression, AmfScalar(expression, Annotations(entry.key)))
          adopt(callback)
          val collected = ctx.factory.endPointParser(entry, callback.withEndpoint, List()).parse()
          collected.foreach(_.withPath(s"/$expression")) // rename path to avoid endpoint validations
          callback
        }.toList

    }

  }
}
