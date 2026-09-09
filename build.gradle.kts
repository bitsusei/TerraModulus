import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.internal.os.OperatingSystem

plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.1.20"
    id("org.jetbrains.kotlinx.atomicfu") version "0.27.0"
    id("net.terramodulus.plugins.cargo") apply false
//    id("fr.stardustenterprises.rust.wrapper") version "3.2.4" apply false
    application
}

version = "0.0.1"

repositories {
    mavenCentral()
}

project(":ferricia") {
    // Candidates: fr.stardustenterprises.rust.wrapper
    apply(plugin = "net.terramodulus.plugins.cargo")

    if (providers.gradleProperty("release").isPresent) configure<CargoExtension> {
        release = true // use `-Prelease=true`
    }
    configure<CargoExtension> {
        outputFile = release.map {
            projectDir.resolve("target/${if (it) "release" else "debug"}/${System.mapLibraryName("ferricia")}")
        }
    }
    // somehow, .cargo extension is unusable
    tasks.register<CargoTask>("buildClient") {
        args = listOf("-F", "client")
        println(outputFile.get())
    }
    tasks.register<CargoTask>("buildServer") {
        args = listOf("-F", "server")
    }
    configurations {
        create("client") {
            isCanBeConsumed = true
            isCanBeResolved = false
        }
        create("server") {
            isCanBeConsumed = true
            isCanBeResolved = false
        }
    }
    artifacts {
        add("client", tasks.named("buildClient"))
        add("server", tasks.named("buildServer"))
    }
}

configure(listOf(project(":kernel"), project(":internal"))) {
    configure(listOf(project("common"), project("client"), project("server"))) {
        apply(plugin = "org.jetbrains.kotlin.jvm")

        version = rootProject.version

        repositories {
            mavenCentral()
        }

        sourceSets.main {
            kotlin.srcDir("kotlin")
            resources.srcDir("resources")
        }

        tasks.compileKotlin {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }

        kotlin {
            jvmToolchain(17)
        }
    }

    configure(listOf(project("client"), project("server"))) {
        dependencies {
            implementation(project("${parent!!.path}:common"))
        }
    }
}

project(":kernel") {
    arrayOf("common", "client", "server").forEach {
        project(it) {
            dependencies {
                implementation(project(":internal:$it"))
            }
        }
    }

    arrayOf("client", "server").forEach {
        project(it) {
            dependencies {
                implementation(project(":internal:common"))
            }
        }
    }
}

project(":kernel:client") {
    dependencies {
        implementation(project(":ferricia", "client"))
    }
}
project(":kernel:server") {
    dependencies {
        implementation(project(":ferricia", "server"))
    }
}

configure(listOf(project(":internal:common"), project(":kernel:common"))) {
    dependencies {
        api("com.cout970:kotlin-vector-math:0.1.0")
    }
}

project(":kernel:common") {
    dependencies {
        api("org.jetbrains:annotations:26.1.0")
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
        api("org.jetbrains.kotlinx:kotlinx-io-core:0.7.0")
        api("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
        api("org.jetbrains.kotlinx:multik-core:0.2.3")
        api("org.jetbrains.kotlinx:multik-default:0.2.3")
        api(kotlin("reflect"))
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        implementation("com.github.oshi:oshi-core:6.8.0")
        api("com.google.errorprone:error_prone_annotations:2.38.0")
        implementation("org.apache.logging.log4j:log4j-core:2.24.3")
        implementation("org.apache.logging.log4j:log4j-api:2.24.3")
        implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3")
        implementation(platform("org.apache.logging.log4j:log4j-bom:2.24.3"))
        annotationProcessor("org.apache.logging.log4j:log4j-core:2.24.3")
        runtimeOnly("com.lmax:disruptor:4.0.0")
        api("io.github.oshai:kotlin-logging-jvm:7.0.3")
        implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    }
}

project(":kernel:client").dependencies {
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
}

project(":kernel:server").dependencies {
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
}

project(":internal:common").dependencies {
    implementation("net.java.dev.jna:jna:5.17.0")
    implementation("net.java.dev.jna:jna-platform:5.17.0")
}

project(":kernel:client").tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    listOf(project(":internal:common"), project(":internal:client"), project(":kernel:common")).forEach {
        from(it.sourceSets.main.get().output)
    }
}

configure(listOf(project(":kernel:server"), project(":kernel:client"))) {
    apply(plugin = "application")

    application {
        mainClass = "net.terramodulus.core.MainKt"
    }
}

tasks.register("buildClient") {
    group = "build"
    description = "Build client"
    dependsOn(":kernel:client:build")
}
tasks.register("buildServer") {
    group = "build"
    description = "Build server"
    dependsOn(":kernel:server:build")
}

tasks.named("run") {
    enabled = false
}
tasks.register("runClient") {
    group = "application"
    description = "Run client"
    dependsOn(":kernel:client:run")
}
tasks.register("runServer") {
    group = "application"
    description = "Run server"
    dependsOn(":kernel:server:run")
}

configure(listOf(project(":kernel:server"), project(":kernel:client"))) {
    distributions {
        main {
            contents {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                into("lib") {
                    val dir = if (project.hasProperty("release")) "release" else "debug"
                    from("$rootDir/ferricia/target/$dir/${System.mapLibraryName("ferricia")}")
                    if (OperatingSystem.current().isWindows) from(
                        "$rootDir/ferricia/target/$dir/oded.dll",
                        "$rootDir/ferricia/target/$dir/OpenAL32.dll",
                        "$rootDir/ferricia/target/$dir/SDL3.dll",
                    ) // for UNIX, other libs should have been installed on user's end directly
                }
            }
        }
    }

    tasks.named<CreateStartScripts>("startScripts") {
        defaultJvmOpts = listOf("-Djava.library.path=../lib")
    }

    tasks.withType<Tar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    tasks.withType<Zip> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    tasks.named<JavaExec>("run") {
        jvmArgs("-Djava.library.path=${rootProject.file("ferricia/target/${
            if (project.hasProperty("release")) "release" else "debug"
        }").path}")
        args("--screen-size", "800x500")
    }
}
