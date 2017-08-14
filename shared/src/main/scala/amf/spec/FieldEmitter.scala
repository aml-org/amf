package amf.spec

import amf.common.AMFToken._
import amf.common.{AMFAST, AMFASTNode, AMFToken}
import amf.domain.Annotation.{ExplicitField, MediaType}
import amf.domain.{EndPoint, Fields, Operation, _}
import amf.metadata.Field
import amf.metadata.domain.{ParameterModel, ResponseModel}
import amf.model.AmfElement
import amf.parser.Range.NONE
import amf.remote.{Oas, Raml, Vendor}
import amf.spec.FieldEmitter.StringValueEmitter.key
import amf.spec.Matcher.KeyMatcher
import amf.spec.Spec.RequestSpec

import scala.collection.immutable.ListMap
import scala.collection.mutable.ListBuffer

/**
  * Node emitters.
  */
object FieldEmitter {

  case class SpecEmitter(map: Map[Field, (SpecField, Option[SpecFieldEmitter])]) {

    def emit(fields: Fields): NodeBuilder = {
      val principal = new LazyBuilder(MapToken) {
        override def build: AMFAST = {
          new AMFASTNode(MapToken, "", NONE, nodes.map(_.build))
        }
      }
      addEmitToPrincipal(fields, principal)
    }

    def addEmitToPrincipal(fields: Fields, principal: LazyBuilder): LazyBuilder = {
      fields
        .filter({ t =>
          t._2 != null && (t._2.value != null && t._2.value != Nil)
        })
        .foreach {
          case (key, value) =>
            map.get(key).foreach {
              case (specField, emitter) =>
                val builders =
                  emitter
                    .fold(specField.emitter.emit(specField, key, value.value))(_.emit(specField, key, value.value))

                builders.filter(!principal.nodes.contains(_)).foreach(principal.add)
            }
        }
      principal
    }
  }
  object SpecEmitter {
    def apply(specFields: List[SpecField]): SpecEmitter = emitters(specFields)
  }

  trait SpecFieldEmitter {
    def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder]

    def key(spec: SpecField): String = spec.matcher match {
      case KeyMatcher(key) => key
      case _               => ""
    }
  }

  abstract class PairValueEmmiter extends SpecFieldEmitter {

    def token: AMFToken

    override def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder] = {
      List(Resolved(entry(spec, valueNode(value, token))))
    }
  }

  object StringValueEmitter extends PairValueEmmiter {

    override def token: AMFToken = StringToken
  }

  object BooleanValueEmitter extends PairValueEmmiter {

    override def token: AMFToken = BooleanToken
  }

  object StringListEmitter extends SpecFieldEmitter {

    override def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder] = {
      List(
        Resolved(
          entry(spec,
                new AMFASTNode(SequenceToken,
                               "",
                               NONE,
                               value
                                 .asInstanceOf[Seq[String]]
                                 .map(sc => {
                                   valueNode(sc)
                                 })))))
    }

  }

  object ObjectEmitter extends SpecFieldEmitter {

    override def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder] = {
      val fields = value.asInstanceOf[AmfElement].fields

      val parent = new LazyBuilder(Entry) {

        override def build: AMFAST = {
          entry(spec, nodes.map(_.build).head)
        }
      }
      parent.add(SpecEmitter(spec.children).emit(fields))
      List(parent)
    }
  }

  object EndPointEmitter extends SpecFieldEmitter {
    override def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder] = {
      var eps: Map[EndPoint, LazyBuilder] = ListMap()
      val endPoints                       = value.asInstanceOf[List[EndPoint]]
      val vendor                          = spec.vendor

      endPoints.foreach(endPoint => {
        val builder: LazyBuilder = endPointBuilder(endPoint, spec)

        if (vendor == Raml) endPoint.parent.foreach(eps(_).add(builder))

        eps = eps + (endPoint -> builder)
      })

      if (vendor == Raml) eps.filterKeys(_.parent.isEmpty).values.toList
      else eps.values.toList
    }

    private def endPointBuilder(endPoint: EndPoint, spec: SpecField): LazyBuilder = new LazyBuilder(Entry) {
      override def build: AMFAST =
        entry(
          if (spec.vendor == Raml) endPoint.relativePath else endPoint.path,
          map(
            List
              .concat(SpecEmitter(spec.children.map(_.copy(vendor = spec.vendor)))
                        .emit(endPoint.fields)
                        .asInstanceOf[LazyBuilder]
                        .nodes,
                      nodes)
              .map(_.build))
        )
    }
  }

  object OperationEmitter extends SpecFieldEmitter {
    override def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder] = {
      val operations = value.asInstanceOf[List[Operation]]

      operations.map(operation => {
        val oBuilder: LazyBuilder = operationBuilder(operation, spec)
        if (operation.request != null) emittRequest(operation.request, spec.vendor, oBuilder)
        else oBuilder
      })
    }

    private def emittRequest(request: Request, vendor: Vendor, principal: LazyBuilder): LazyBuilder = {
      val emitter = SpecEmitter(RequestSpec(vendor).fields.map(_.copy(vendor = vendor)).toList)
      emitter.addEmitToPrincipal(request.fields, principal)
    }

    private def operationBuilder(operation: Operation, spec: SpecField): LazyBuilder = new LazyBuilder(Entry) {
      override def build: AMFAST =
        entry(operation.method,
              map(
                buildChildrens(spec, operation.fields)
              ))
    }
  }

  private def map(entries: Seq[AMFAST]): AMFAST = new AMFASTNode(MapToken, "", NONE, entries)

  private def entry(spec: SpecField, value: AMFAST): AMFAST = entry(key(spec), value)
  private def entry(key: String, value: AMFAST): AMFAST = {
    new AMFASTNode(Entry,
                   "",
                   NONE,
                   List(
                     valueNode(key),
                     value
                   ))
  }

  object ParametersEmitter extends SpecFieldEmitter {
    override def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder] = {
      val parameters = value.asInstanceOf[List[Parameter]]

      spec.vendor match {
        case Oas if spec.fields.head.equals(ResponseModel.Headers) => getBuilderForPlain(spec, parameters)
        case Raml                                                  => getBuilderForPlain(spec, parameters)
        case Oas =>
          List(new LazyBuilder(Entry) {
            override def build: AMFAST =
              entry(spec, new AMFASTNode(SequenceToken, "", null, parameters.map(parameter => {
                oasParameterBuilder(parameter, spec).build
              })))
          })
        case _ => ???
      }
    }

    private def getBuilderForPlain(spec: SpecField, parameters: List[Parameter]): List[NodeBuilder] = {
      List(new LazyBuilder(Entry) {
        override def build: AMFAST =
          entry(spec, map(parameters.map(parameter => { ramlParameterBuilder(parameter, spec).build })))
      })
    }

    private def ramlParameterBuilder(parameter: Parameter, spec: SpecField): LazyBuilder = new LazyBuilder(Entry) {
      def isExplicit(field: Field, value: Value): Boolean =
        field == ParameterModel.Required && value.annotations.exists(classOf[ExplicitField].isInstance(_))

      val requiredFilter: (((Field, Value)) => Boolean) = { t =>
        { !isExplicit(t._1, t._2) }
      }
      private val required = parameter.fields.getValue(ParameterModel.Required)
      if (!isExplicit(ParameterModel.Required, required)) {

        val value  = parameter.fields.getValue(ParameterModel.Name)
        val sValue = value.value.toString + (if (!required.value.asInstanceOf[Boolean]) "?" else "")

        parameter.fields.set(ParameterModel.Name, sValue, value.annotations)
        parameter.fields.filter(f => f._1 != ParameterModel.Required)
      }

      override def build: AMFAST =
        entry(parameter.name, map(buildChildrens(spec, parameter.fields)))
    }

    private def oasParameterBuilder(parameter: Parameter, spec: SpecField): LazyBuilder = {
      val principal = new LazyBuilder(MapToken) {
        override def build: AMFAST = map(nodes.map(_.build))
      }
      val emitter = SpecEmitter(spec.children.map(_.copy(vendor = spec.vendor)))
      emitter.addEmitToPrincipal(parameter.fields, principal)

      val mediaTypeOption = parameter.annotations.find(p => p.isInstanceOf[MediaType]).map(_.asInstanceOf[MediaType])
      if (mediaTypeOption.isDefined && mediaTypeOption.get.mediaType != null && !mediaTypeOption.get.mediaType.isEmpty)
        principal.add(new LazyBuilder(Entry) {
          override def build: AMFAST = entry(mediaTypeOption.get.key, valueNode(mediaTypeOption.get.mediaType))
        })

      principal
    }
  }

  object ResponseEmitter extends SpecFieldEmitter {
    override def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder] = {
      val responses: List[Response] = value.asInstanceOf[List[Response]]

      responses.map(r => {
        val emitter = SpecEmitter(Spec.OasPayload.fields.toList)

        val builder = new LazyBuilder(Entry) {
          override def build: AMFAST = {
            entry(if (spec.vendor == Raml) r.statusCode else r.name, map(buildChildrens(spec, r.fields)))
          }
        }
        if (r.payloads.nonEmpty && spec.vendor == Oas) {
          val default = defaultPayload(r.payloads.toList)
          emitter.addEmitToPrincipal(default.get.fields, builder)
        }
        builder
      })
    }
  }

  object PayloadEmitter extends SpecFieldEmitter {
    override def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder] = {

      val payloads = value.asInstanceOf[List[Payload]]
      val default  = defaultPayload(payloads)
      spec.vendor match {
        case Raml =>
          List(new LazyBuilder(Entry) {
            override def build: AMFAST = {
              entry(
                key(spec),
                map(payloads.map(p => {
                  if (default.isDefined && default.get == p && (p.mediaType == null || p.mediaType.isEmpty))
                    entry("type", valueNode(p.schema))
                  else
                    entry(p.mediaType, valueNode(p.schema))
                }))
              )
            }
          })
        case Oas =>
          val default            = defaultPayload(payloads)
          val nonDefaultPayloads = payloads.filter(p => p != default.get)
          if (nonDefaultPayloads.isEmpty) Nil
          else
            List(new LazyBuilder(Entry) {
              override def build: AMFAST = {
                entry(
                  key(spec),
                  new AMFASTNode(
                    SequenceToken,
                    "",
                    null,
                    nonDefaultPayloads.map(p => {
                      val emitter = SpecEmitter(Spec.OasExtensionPayload.fields.toList)
                      emitter
                        .addEmitToPrincipal(p.fields, new LazyBuilder(MapToken) {
                          override def build: AMFAST = map(nodes.map(_.build))
                        })
                        .build
                    })
                  )
                )
              }
            })
        case _ => ???

      }
    }
  }

  private def valueNode(value: Any, token: AMFToken = StringToken) =
    new AMFASTNode(token, if (value == null) "" else value.toString, NONE)

  def nested(sf: SpecField): Seq[(Field, (SpecField, Option[SpecFieldEmitter]))] = {

    lazy val virtualNodeEmitter = new SpecFieldEmitter {
      var parent: Option[LazyBuilder] = None
      override def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder] = {
        if (parent.isEmpty)
          parent = Some(new LazyBuilder(Entry) {

            override def build: AMFAST = {
              entry(spec, map(nodes.map(_.build)))
            }
          })

        val sonSpec = spec.children.find(sp => sp.fields.head == field).get.copy(vendor = spec.vendor)
        parent.get.add(sonSpec.emitter.emit(sonSpec, field, value))

        List(parent.get)
      }
    }

    for {
      spec <- sf.children
    } yield {
      (spec.fields.head, (sf.copy(children = sf.children.map(_.copy(vendor = sf.vendor))), Some(virtualNodeEmitter)))
    }
  }

  def emitters(specFields: List[SpecField]): SpecEmitter = {

    var map: Map[Field, (SpecField, Option[SpecFieldEmitter])] = Map()

    specFields.foreach(sf =>
      sf.fields match {
        case Nil => map = map ++ nested(sf)
        case _   => map = map + (sf.fields.head -> (sf, None))
    })

    SpecEmitter(map)
  }

  trait NodeBuilder {
    def build: AMFAST
  }

  abstract class LazyBuilder(val token: AMFToken, val nodes: ListBuffer[NodeBuilder] = ListBuffer())
      extends NodeBuilder {
    def add(n: NodeBuilder): Unit       = nodes += n
    def add(n: List[NodeBuilder]): Unit = nodes ++= n

    override def build: AMFAST

    protected def buildChildrens(spec: SpecField, fields: Fields): List[AMFAST] =
      List
        .concat(
          SpecEmitter(spec.children.map(_.copy(vendor = spec.vendor))).emit(fields).asInstanceOf[LazyBuilder].nodes,
          nodes)
        .map(_.build)
  }

  case class Resolved(node: AMFAST) extends NodeBuilder {
    override def build: AMFAST = node
  }

  //TODO where goes this aux method?
  def defaultPayload(payloads: Seq[Payload]): Option[Payload] = {
    //TODO only for raml?
    if (payloads.isEmpty) None
    else {
      val emptyMTOption = payloads.find(p => p.schema == null || p.schema.isEmpty)
      if (emptyMTOption.isDefined) emptyMTOption
      else {
        val jsonSchemaOption = payloads.find(p => p.mediaType == "application/json")
        if (jsonSchemaOption.isDefined) jsonSchemaOption
        else
          Some(payloads.head)
      }
    }
  }
}
