package amf.`export`

class MarkdownBuilder(val current: String = "") {

  def addHeader(size: Int, text: String): MarkdownBuilder = {
    val header = "#" * size + " " + text
    new MarkdownBuilder(current + "\n" + header)
  }

  def addText(text: String): MarkdownBuilder = {
    new MarkdownBuilder(current + "\n" + text)
  }

  def startTable(cols: List[String]): MarkdownBuilder = {
    val separator = cols.map(_ => "------")
    addText("").addRow(cols).addRow(separator)
  }

  def addLine(): MarkdownBuilder = {
    new MarkdownBuilder(current + "\n" + "---")
  }

  def addRow(cols: List[String]): MarkdownBuilder = {
    val text = cols.fold("") { (acc, curr) =>
      acc + " | " + curr.replace("\n", " ")
    } + " |"
    new MarkdownBuilder(current + "\n" + text)
  }

  def build: String = current
}
