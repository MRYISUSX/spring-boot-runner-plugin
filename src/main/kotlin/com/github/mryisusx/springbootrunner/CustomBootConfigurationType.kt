package com.github.mryisusx.springbootrunner

import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.icons.AllIcons
import javax.swing.Icon

class CustomBootConfigurationType : ConfigurationType {

    override fun getDisplayName() = "Custom Spring Boot Runner"

    override fun getConfigurationTypeDescription() =
        "Run Spring Boot with custom JAVA_HOME"

    override fun getIcon(): Icon = AllIcons.RunConfigurations.Application

    override fun getId() = "CUSTOM_BOOT_RUN"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
        arrayOf(CustomBootConfigurationFactory(this))
}