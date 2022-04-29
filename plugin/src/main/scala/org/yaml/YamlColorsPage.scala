/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ /*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yaml

import java.util

import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.{AttributesDescriptor, ColorDescriptor, ColorSettingsPage}

class YamlColorsPage extends ColorSettingsPage {
  private val map = new util.HashMap[String, TextAttributesKey]
  private val attributes: Array[AttributesDescriptor] = Array(
    new AttributesDescriptor(YamlBundle.message("color.settings.yaml.key"), YamlHighlighter.SCALAR_KEY),
    new AttributesDescriptor(YamlBundle.message("color.settings.yaml.string"), YamlHighlighter.SCALAR_STRING),
    new AttributesDescriptor(YamlBundle.message("color.settings.yaml.dstring"), YamlHighlighter.SCALAR_DSTRING),
    new AttributesDescriptor(YamlBundle.message("color.settings.yaml.scalar.list"), YamlHighlighter.SCALAR_LIST),
    new AttributesDescriptor(YamlBundle.message("color.settings.yaml.scalar.text"), YamlHighlighter.SCALAR_TEXT),
    new AttributesDescriptor(YamlBundle.message("color.settings.yaml.text"), YamlHighlighter.TEXT),
    new AttributesDescriptor(YamlBundle.message("color.settings.yaml.sign"), YamlHighlighter.SIGN),
    new AttributesDescriptor(YamlBundle.message("color.settings.yaml.comment"), YamlHighlighter.COMMENT)
  )

  override def getAdditionalHighlightingTagToDescriptorMap: util.HashMap[String, TextAttributesKey] = map

  override def getDisplayName: String = YamlBundle.message("color.settings.yaml.name")

  override def getIcon = AllIcons.Nodes.DataTables

  override def getAttributeDescriptors: Array[AttributesDescriptor] = attributes

  override def getColorDescriptors: Array[ColorDescriptor] = ColorDescriptor.EMPTY_ARRAY

  override def getHighlighter: YamlSyntaxHighlighter = new YamlSyntaxHighlighter

  override def getDemoText: String =
    """---
          |# An Example yaml
          |static_sidebar:
          |  id: "foo"
          |  name: 'side_bar'
          |  staged_position: 1
          |  blog_id: 1
          |  config: |+
          |    --- !map:HashWithIndifferentAccess
          |      title: Static Sidebar
          |      body: The body of a static sidebar
          |  type: StaticSidebar
          |  type: > some_type_here""".stripMargin
}
