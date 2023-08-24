import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.2"
	id("org.asciidoctor.jvm.convert") version "3.3.2"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
	kotlin("plugin.allopen") version "1.8.22"
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.Embeddable")
	annotation("jakarta.persistence.MappedSuperclass")
}

group = "Soko"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

val snippetsDir by extra { file("build/generated-snippets") }

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("com.mysql:mysql-connector-j")
	runtimeOnly("com.h2database:h2")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
	testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
	testImplementation("io.kotest:kotest-assertions-core:5.6.2")
	testImplementation("io.rest-assured:rest-assured:5.3.1")
	testImplementation("io.rest-assured:kotlin-extensions:5.3.1")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.test {
	outputs.dir(snippetsDir)
}

tasks.asciidoctor {
	inputs.dir(snippetsDir)
	dependsOn(tasks.test)

	doFirst {
		delete {
			file("src/main/resources/static/docs")
		}
	}
}

tasks.register("copyHTML", Copy::class) {
	dependsOn(tasks.asciidoctor)
	from(file("build/asciidoc/html5"))
	into(file("src/main/resources/static/docs"))
}

tasks.build {
	dependsOn(tasks.getByName("copyHTML"))
}

tasks.bootJar {
	dependsOn(tasks.asciidoctor)
	dependsOn(tasks.getByName("copyHTML"))
}
