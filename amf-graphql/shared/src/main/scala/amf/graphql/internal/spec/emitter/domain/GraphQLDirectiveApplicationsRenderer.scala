package amf.graphql.internal.spec.emitter.domain
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.client.scala.model.domain.{DataNode, NamedDomainElement, ObjectNode, ScalarNode}

object GraphQLDirectiveApplicationsRenderer {
  def apply(elem: NamedDomainElement): String = {
    val applications = elem.customDomainProperties
    applications.map(renderDirectiveApplication(_)).mkString(" ")
  }

  private def renderDirectiveApplication(directive: DomainExtension) = {
    val name = directive.name.value()
    renderDirectiveArguments(directive) match {
      case Some(arguments) => s"@$name($arguments)"
      case _               => s"@$name"
    }
  }

  private def renderDirectiveArguments(directive: DomainExtension): Option[String] = {
    val arguments    = directive.extension.asInstanceOf[ObjectNode].allProperties()
    val hasArguments = arguments.nonEmpty

    if (hasArguments) {
      val args = arguments.foldLeft("") { (acc, argument) =>
        val argString = buildArgumentString(argument)
        if (renderingFirstArgument(acc)) argString else s"$acc, $argString"
      }
      Some(args)
    } else {
      None
    }
  }

  private def buildArgumentString(arg: DataNode) = {
    val argName   = arg.asInstanceOf[ScalarNode].name.value()
    val value     = arg.asInstanceOf[ScalarNode].value.value()
    s"$argName: $value"
  }

  private def renderingFirstArgument(acc: String) = acc.isEmpty

}
