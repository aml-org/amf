package org.yaml

import com.intellij.application.options.{
  CodeStyleAbstractConfigurable,
  CodeStyleAbstractPanel,
  TabbedLanguageCodeStylePanel
}
import com.intellij.psi.codeStyle.{CodeStyleSettings, CodeStyleSettingsProvider}

class YamlCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
  override def createSettingsPage(settings: CodeStyleSettings, originalSettings: CodeStyleSettings) =
    new CodeStyleAbstractConfigurable(settings, originalSettings, YamlLanguage.getDisplayName) {

      override def getHelpTopic: String = "reference.settingsdialog.codestyle.yaml"

      override protected def createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel =
        new TabbedLanguageCodeStylePanel(YamlLanguage, getCurrentSettings, settings) {
          override protected def initTabs(settings: CodeStyleSettings): Unit = addIndentOptionsTab(settings)
        }
    }

  override def getConfigurableDisplayName: String = YamlLanguage.getDisplayName
}
