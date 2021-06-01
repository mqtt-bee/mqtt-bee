plugins {
    id("java-platform")
}


/* ******************** metadata ******************** */

description = "Adds dependencies for the HiveMQ MQTT Client websocket module"

metadata {
    moduleName.set("com.hivemq.client2.mqtt.websocket")
    readableName.set("HiveMQ MQTT Client websocket module")
}


/* ******************** dependencies ******************** */

javaPlatform {
    allowDependencies()
}

dependencies {
    api(rootProject)
}

configurations.runtime {
    extendsFrom(rootProject.configurations["websocketImplementation"])
}
