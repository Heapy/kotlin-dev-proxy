plugins {
    kotlin("jvm").version(kotlinVersion)
    application
}

repositories {
    jcenter()
}

configure<ApplicationPluginConvention> {
    mainClassName = "server.Server"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("io.undertow:undertow-core:$undertowVersion")
    implementation("org.yaml:snakeyaml:$snakeYmlVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
}
