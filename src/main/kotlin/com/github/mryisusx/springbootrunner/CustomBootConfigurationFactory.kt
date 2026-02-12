package com.github.mryisusx.springbootrunner

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.project.Project

class CustomBootConfigurationFactory(type: ConfigurationType) :
    ConfigurationFactory(type) {

    override fun createTemplateConfiguration(project: Project): CustomBootRunConfiguration =
        CustomBootRunConfiguration(project, this, "Custom Boot")

    override fun getId() = "CustomBootRun"
}