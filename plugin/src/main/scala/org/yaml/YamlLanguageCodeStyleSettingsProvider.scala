package org.yaml

import javax.swing._

import com.intellij.application.options.IndentOptionsEditor
import com.intellij.openapi.application.ApplicationBundle
import com.intellij.psi.codeStyle.{CommonCodeStyleSettings, LanguageCodeStyleSettingsProvider}

class YamlLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
  override def getDefaultCommonSettings: CommonCodeStyleSettings = {
    val defaultSettings = new CommonCodeStyleSettings(YamlLanguage)
    val indentOptions   = defaultSettings.initIndentOptions()
    indentOptions.INDENT_SIZE = 4
    indentOptions.USE_TAB_CHARACTER = false
    defaultSettings
  }

  override def getIndentOptionsEditor = new IndentOptionsEditor {
    override protected def addComponents(): Unit = {
      addTabOptions()
      // Tabs in YAML are not allowed
      myCbUseTab.setEnabled(false)
      myTabSizeField = createIndentTextField
      myTabSizeLabel = new JLabel(ApplicationBundle.message("editbox.indent.tab.size"))
      myIndentField = createIndentTextField
      myIndentLabel = new JLabel(ApplicationBundle.message("editbox.indent.indent"))
      add(myIndentLabel, myIndentField)
    }

    override def setEnabled(enabled: Boolean): Unit = {
      // Do nothing
    }
  }

  override def getLanguage = YamlLanguage

  override def getCodeSample(settingsType: LanguageCodeStyleSettingsProvider.SettingsType): String =
    """product:
          |  name: RubyMine
          |  version: 8
          |  vendor: JetBrains
          |  url: "https://www.jetbrains.com/ruby"""".stripMargin

}
