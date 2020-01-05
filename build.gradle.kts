import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType;

val nettyVersion = "4.1.35.Final"
val junitVersion = "5.3.2"
val sfl4jVersion = "1.7.26"
val logbackVersion = "1.2.3"
val lombokVersion = "1.18.6"
val scaleCubeConfigVersion = "0.3.9"

val dockerBaseImage: String by project
val dockerImageName: String by project
val dockerMaintainer: String by project

val jvmDefaultOpts: String by project
val jvmDebuggerPort: String by project

plugins {
    java
    application
    idea
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("net.nemerosa.versioning") version "2.8.2"
    id("com.diffplug.gradle.spotless") version "3.24.2"
    id("com.bmuschko.docker-remote-api") version "5.0.0"
    id("com.palantir.graal") version "0.4.0"
    id("net.ltgt.apt-idea") version "0.21"
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
    implementation("io.netty:netty-transport-native-epoll:$nettyVersion")

    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.slf4j:slf4j-api:$sfl4jVersion")

    implementation("io.atomix:atomix:3.1.5")
    implementation("io.atomix:atomix-raft:3.1.5")
    implementation("io.atomix:atomix-primary-backup:3.1.5")
    implementation("io.atomix:atomix-gossip:3.1.5")
    
    implementation("org.cfg4j:cfg4j-core:4.4.1")

    implementation("com.hivemq:hivemq-mqtt-client:1.0.0")
    implementation("com.google.guava:guava:27.1-jre")
    implementation("commons-codec:commons-codec:1.8")

    implementation("com.dorkbox:MessageBus:2.1")

    implementation("com.google.dagger:dagger:2.24")
    annotationProcessor("com.google.dagger:dagger-compiler:2.24")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testImplementation("org.assertj:assertj-core:3.12.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.11.1")
}

docker {
    registryCredentials {
        url.set(getConfigurationProperty("DOCKER_REGISTRY_URL", "dockerRegistryUrl"))
        username.set(getConfigurationProperty("DOCKER_REGISTRY_USERNAME", "dockerRegistryUsername"))
        password.set(getConfigurationProperty("DOCKER_REGISTRY_PASSWORD", "dockerRegistryPassword"))
    }
}

graal {
    mainClass("io.jqtt.Launcher")
    outputName("jqtt")
    option("--enable-http")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClassName = "io.jqtt.Launcher"
    applicationDefaultJvmArgs = jvmDefaultOpts
            .replace("\\", "")
            .split(" ")
            //.plus("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${jvmDebuggerPort}")
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<ShadowJar> {
        archiveBaseName.set("jqtt")
    }
}

val shadowJar = tasks["shadowJar"] as ShadowJar

tasks.register<Copy>("dockerSyncJar") {
    dependsOn(shadowJar)
    from(shadowJar.archivePath)
    into("$buildDir/docker")
    setGroup("docker")
    description = "Copy fat jar to docker directory."
}

val dockerSyncJar = tasks["dockerSyncJar"]

val dockerCreateDockerfile by tasks.creating(Dockerfile::class) {
    destFile.set(file("build/docker/Dockerfile"))
    from(dockerBaseImage)
    copyFile("${rootProject.name}-all.jar", "/app/app.jar")
    environmentVariable("DEFAULT_JAVA_OPTS", jvmDefaultOpts)
    environmentVariable("JAVA_OPTS", "\"\"")
    entryPoint("sh", "-c")
    defaultCommand("java \$DEFAULT_JAVA_OPTS \$JAVA_OPTS -jar /app/app.jar")
    label(mapOf("maintainer" to dockerMaintainer))
    dependsOn(dockerSyncJar)
    group = "docker"
    description = "Create Dockerfile."
}

val dockerBuildImage by tasks.creating(DockerBuildImage::class) {
    tags.add("$dockerImageName:${versioning.info.full}")
    noCache.set(true)
    dependsOn(dockerCreateDockerfile)
    group = "docker"
    description = "Build docker image."
}

tasks.create("dockerPushImage", DockerPushImage::class) {
    dependsOn(dockerBuildImage)
    tag.set(versioning.info.full)
    imageName.set(dockerImageName)
    group = "docker"
    description = "Push image to registry (tag is resolved base on versioning plugin)."
}

tasks {
    "wrapper"(Wrapper::class) {
        gradleVersion = "5.6.2"
        distributionType = DistributionType.ALL
    }
}

fun getConfigurationProperty(envVar: String, sysProp: String): String {
    return System.getenv(envVar) ?: project.findProperty(sysProp).toString()
}