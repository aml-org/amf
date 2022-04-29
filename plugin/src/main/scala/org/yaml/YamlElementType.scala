package org.yaml

import com.intellij.psi.tree.IElementType

class YamlElementType(val debugName: String) extends IElementType(debugName, YamlFileType.getLanguage)
