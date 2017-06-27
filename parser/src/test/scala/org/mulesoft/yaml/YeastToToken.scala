package org.mulesoft.yaml

import java.io.PrintWriter

import org.mulesoft.common.core.Strings
import org.mulesoft.lexer.TokenData
import org.yaml.lexer.YamlToken

import scala.io.Source

/**
  * Created by emilio.gabeiras on 6/9/17.
  */
object YeastToYToken {

  def main(args: Array[String]): Unit = {
    for (file <- args) {
      val output = new PrintWriter(file.replace(".yeast", ".yt"))
      for (Seq(a, b) <- Source.fromFile(file).getLines().grouped(2))
        output.println(YeastData(a + ", " + b))
      output.close()
    }

  }

}

case class YeastData(t: YamlToken, start: Int, line: Int, col: Int, text: String) {
  override def toString: String = "%-15s %5d,%3d,%3d '%s'".format(t, start, line, col, text.encode)
}

object YeastData {
  def apply(tokenData: TokenData[YamlToken], txt: String): YeastData =
    new YeastData(tokenData.token,
                  tokenData.start,
                  tokenData.range.lineFrom,
                  tokenData.range.columnFrom,
                  txt)
  def apply(str: String): YeastData = {
    str match {
      case Regex(_, offset, line, column, abbreviation, text) =>
        new YeastData(YamlToken(abbreviation), offset.toInt, line.toInt, column.toInt, text.decode)
    }
  }
  private val Regex = "# B: (\\d+), C: (\\d+), L: (\\d+), c: (\\d+), ([a-zA-Z]) ?(.*)".r
}
