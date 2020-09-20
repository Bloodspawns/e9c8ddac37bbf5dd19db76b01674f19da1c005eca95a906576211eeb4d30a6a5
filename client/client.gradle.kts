plugins {
    java
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

val client = configurations.create("client")
val plugins = configurations.create("plugins")
val patches = configurations.create("patches")
val runclient = configurations.create("runclient")

dependencies {
    client(Libraries.runelite_client)
    runclient(Libraries.jcodec_javase)
    runclient(Libraries.jcodec)
    runclient(files(client.files.filter { !it.name.startsWith("client") }))
    runclient(files("build/libs/bclient-1.0.jar"))
    plugins(project(":plugins")) { isTransitive = false }
    plugins(fileTree(project.gradle.gradleUserHomeDir.parent + "/.runelite/bluerepo/"){ include("*.jar") })
    runclient(fileTree(project.gradle.gradleUserHomeDir.parent + "/.runelite/bexternalplugins/"){ include("*.jar") })
    patches(files(project.gradle.gradleUserHomeDir.parent + "/.runelite/bluerepo/blue-patch.jar"))
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

//runclient.files.filter { it.name.startsWith("client") }.forEach{println(it.name)}

tasks {

    register<Jar>("repack") {
        from(zipTree(client.first())) {
            exclude("META-INF/")
        }
        baseName = "bclient"
    }

    register<JavaExec>("Client.main()") {
//        dependsOn(":client:repack")
        group = "run"

        classpath = plugins.plus(patches).plus(runclient)
        main = "net.runelite.client.RuneLite"
        jvmArgs = listOf("-ea")
        args = listOf("--developer-mode", "--debug")
    }
}