import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val jvmDefaultOpts: String by project
val jvmDebuggerPort: String by project

val nettyVersion = "4.1.34.Final"
val junitVersion = "5.3.2"
val sfl4jVersion = "1.7.26"
val logbackVersion = "1.2.3"
val lombokVersion = "1.18.6"
val scaleCubeConfigVersion = "0.3.9"

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "5.0.0"
    id("net.nemerosa.versioning") version "2.8.2"
    id("com.diffplug.gradle.spotless") version "3.20.0"
}

repositories {
    mavenCentral()
    jcenter()
}

spotless {
    java {
        licenseHeaderFile("spotless.license.java")
        googleJavaFormat()
        encoding("UTF-8")
    }
}

dependencies {
    implementation("io.netty:netty-all:$nettyVersion")

    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.slf4j:slf4j-api:$sfl4jVersion")

    implementation("io.scalecube:config:$scaleCubeConfigVersion")
    implementation("org.cfg4j:cfg4j-core:4.4.1")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
    
    testImplementation("junit:junit:4.12")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClassName = "io.jqtt.Launcher"
    applicationDefaultJvmArgs = jvmDefaultOpts
            .replace("\\", "")
            .split(" ")
            .plus("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${jvmDebuggerPort}")
}

val shadowJar = tasks["shadowJar"] as ShadowJar
tasks.getByName("shadowJar", ShadowJar::class).apply {
    isZip64 = true
}
