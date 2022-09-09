package amf.cli.internal

import org.yaml.parser.YamlParser

object App extends App {

  val payload =
    """{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{
      |[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{
      |{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{
      |{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{
      |{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{
      |{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[{{{{{{{[
      |{{{{{[{{{{{{""".stripMargin

  val bigPayload = (1 to 2).map(_ => payload).mkString("")
  val yaml       = YamlParser(bigPayload)
  val doc        = yaml.document()
  println("finished")
}
