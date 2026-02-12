package com.github.mryisusx.springbootrunner

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.*
import java.awt.BorderLayout
import java.awt.Dimension

class CustomBootSettingsEditor(
    private val project: Project
) : SettingsEditor<CustomBootRunConfiguration>() {

    private val panel = JPanel(BorderLayout())

    private val jdkComboBox = JComboBox<Sdk>()
    private val goalField = JBTextField("spring-boot:run", 20)
    private val portField = JBTextField("8080")


    init {

        // Puerto pequeño
        portField.preferredSize = Dimension(80, portField.preferredSize.height)
        portField.maximumSize = Dimension(100, portField.preferredSize.height)

        val formPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("JDK:", jdkComboBox)
            .addLabeledComponent("Maven Goal:", goalField)
            .addLabeledComponent("Port:", portField)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        panel.layout = BorderLayout()
        panel.add(formPanel, BorderLayout.NORTH)

        cargarJdks()
    }

    private fun cargarJdks() {
        val jdks = ProjectJdkTable.getInstance().allJdks
        jdks.forEach { jdkComboBox.addItem(it) }
    }

    override fun resetEditorFrom(s: CustomBootRunConfiguration) {
        goalField.text = s.goal
        portField.text = s.port

        val selected = ProjectJdkTable.getInstance().allJdks
            .find { it.homePath == s.javaHome }

        if (selected != null) {
            jdkComboBox.selectedItem = selected
        }
    }

    override fun applyEditorTo(s: CustomBootRunConfiguration) {
        val selectedJdk = jdkComboBox.selectedItem as? Sdk
        s.javaHome = selectedJdk?.homePath ?: ""
        s.goal = goalField.text
        s.port = portField.text
    }

    override fun createEditor(): JComponent = panel
}