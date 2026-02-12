package com.github.mryisusx.springbootrunner

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jdom.Element
import java.io.File
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.util.Key

class CustomBootRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<CustomBootRunConfiguration>(project, factory, name) {

    var javaHome: String = ""
    var goal: String = "spring-boot:run"
    var port: String = "8080"

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return CustomBootSettingsEditor(project)
    }

    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment
    ): RunProfileState {

        return object : CommandLineState(environment) {

            override fun startProcess(): OSProcessHandler {

                val selectedPort = port.toIntOrNull() ?: 8080
                var finalPort = selectedPort
                var portArg = ""

                if (!isPortAvailable(selectedPort)) {
                    val freePort = findFreePort()
                    finalPort = freePort

                    Notifications.Bus.notify(
                        Notification(
                            "SpringBootRunner",
                            "Puerto ocupado",
                            "Puerto $selectedPort estaba ocupado. Usando $finalPort",
                            NotificationType.WARNING
                        ),
                        project
                    )
                    portArg = "-Dspring-boot.run.arguments=--server.port=$finalPort"
                }

                val workDir = File(project.basePath!!)
                val isWindows = System.getProperty("os.name").lowercase().contains("win")

                val mvnw = File(workDir, if (isWindows) "mvnw.cmd" else "mvnw")

                portArg = "-Dspring-boot.run.arguments=--server.port=${port}"

                val commandLine = when {
                    mvnw.exists() && isWindows ->
                        GeneralCommandLine("cmd", "/c", "mvnw.cmd", goal, portArg)

                    mvnw.exists() ->
                        GeneralCommandLine("./mvnw", goal, portArg)

                    isWindows ->
                        GeneralCommandLine("cmd", "/c", "mvn", goal, portArg)

                    else ->
                        GeneralCommandLine("mvn", goal, portArg)
                }

                commandLine.withWorkDirectory(workDir)
                commandLine.environment["JAVA_HOME"] = javaHome

                val handler = OSProcessHandler(commandLine)

                handler.addProcessListener(object : ProcessAdapter() {
                    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {


                        val text = event.text

                        if (text.contains("Tomcat started on port")) {

                            val contextPath = getContextPath().trim()
                            val url = "http://localhost:$finalPort$contextPath/"

                            if(contextPath.isNotBlank()) {
                                BrowserUtil.browse(url)
                            }
                        }
                    }
                })

                return handler
            }
        }
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        element.setAttribute("javaHome", javaHome)
        element.setAttribute("goal", goal)
        element.setAttribute("port", port)
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        javaHome = element.getAttributeValue("javaHome") ?: ""
        goal = element.getAttributeValue("goal") ?: "spring-boot:run"
        port = element.getAttributeValue("port") ?: "8080"
    }

    private fun isPortAvailable(port: Int): Boolean {
        return try {
            java.net.ServerSocket(port).use { true }
        } catch (e: Exception) {
            false
        }
    }

    private fun findFreePort(): Int {
        java.net.ServerSocket(0).use { socket ->
            return socket.localPort
        }
    }

    private fun getContextPath(): String {
        val file = java.io.File(project.basePath, "src/main/resources/application.properties")
        if (!file.exists()) return ""

        val line = file.readLines()
            .find { it.trim().startsWith("server.servlet.context-path") }

        return line?.split("=")?.getOrNull(1)?.trim() ?: ""
    }

}