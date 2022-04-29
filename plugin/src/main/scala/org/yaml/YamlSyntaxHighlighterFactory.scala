package org.yaml

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class YamlSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
  override def getSyntaxHighlighter(project: Project, virtualFile: VirtualFile) = new YamlSyntaxHighlighter
}
