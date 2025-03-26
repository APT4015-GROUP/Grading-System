  plugins {
	java
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "com.gradingsystem"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
        //  Parsing Jackson
        implementation ("com.fasterxml.jackson.core:jackson-databind:2.15.1")

        implementation ("com.theokanning.openai-gpt3-java:client:0.17.0")     // OpenAI API for grading
        implementation ("com.theokanning.openai-gpt3-java:service:0.17.0")     // OpenAI API for grading
        implementation ("com.google.cloud:google-cloud-document-ai:2.57.0")   // Google Document AI
        implementation ("org.apache.pdfbox:pdfbox:2.0.29")                   // PDF Processing
        implementation ("org.apache.poi:poi-ooxml:5.2.3")                    // Word Document Processing

        // Cohere API 
        implementation ("com.cohere:cohere-java:+")

        // Plagiarism
        implementation ("org.apache.lucene:lucene-core:8.11.2")
        implementation ("org.apache.lucene:lucene-analyzers-common:8.11.2")

        // Natural Language Processing
        implementation ("org.apache.opennlp:opennlp-tools:2.5.0")

        //  Lombok Dependency
        implementation ("org.projectlombok:lombok:1.18.36")
        compileOnly ("org.projectlombok:lombok:1.18.36")
        annotationProcessor ("org.projectlombok:lombok:1.18.36")

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-tomcat")
	runtimeOnly("com.mysql:mysql-connector-j")

        //  Testing
        testImplementation("org.testcontainers:testcontainers:1.20.6") 
        testImplementation("org.testcontainers:junit-jupiter:1.20.6")
        testImplementation("org.testcontainers:mysql:1.20.6")
        testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform(){
        includeTags("unit")  // Run only unit tests
    }
}

// Define configurations for API and Integration test source sets
configurations {
    create("apiTestImplementation") {
        extendsFrom(configurations["testImplementation"])
    }
    create("apiTestCompileClasspath") {
        extendsFrom(configurations["apiTestImplementation"])
        isCanBeResolved = true  // Allows resolving dependencies
    }
    create("apiTestRuntimeClasspath") {
        extendsFrom(configurations["testRuntimeClasspath"])
        isCanBeResolved = true  // Allows resolving dependencies
    }

    create("integrationTestImplementation") {
        extendsFrom(configurations["testImplementation"])
    }
    create("integrationTestCompileClasspath") {
        extendsFrom(configurations["integrationTestImplementation"])
        isCanBeResolved = true  // Allows resolving dependencies
    }
    create("integrationTestRuntimeClasspath") {
        extendsFrom(configurations["testRuntimeClasspath"])
        isCanBeResolved = true  // Allows resolving dependencies
    }
}

sourceSets {
    test {
        java.srcDirs("src/test/java/com/gradingsystem/tesla/service")
    }

    create("apiTest") {
        java.srcDirs("src/test/java/com/gradingsystem/tesla/controller")
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output + configurations["apiTestCompileClasspath"]
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output + configurations["apiTestRuntimeClasspath"]
    }

    create("integrationTest") {
        java.srcDirs("src/test/java/com/gradingsystem/tesla/integration")
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output + configurations["integrationTestCompileClasspath"]
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output + configurations["integrationTestRuntimeClasspath"]
    }
}

// Integration Test Task (runs with `./gradlew integrationTest`)
tasks.register<Test>("integrationTest") {
    description = "Runs integration tests"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform {
        includeTags("integration")  // Run only integration tests
    }
    shouldRunAfter(tasks.test)
}

// API Test Task (runs with `./gradlew apiTest`)
tasks.register<Test>("apiTest") {
    description = "Runs API tests"
    group = "verification"
    testClassesDirs = sourceSets["apiTest"].output.classesDirs
    classpath = sourceSets["apiTest"].runtimeClasspath
    useJUnitPlatform {
        includeTags("api")  // Run only API tests
    }
    shouldRunAfter(tasks.test, tasks.named("integrationTest"))
}

tasks.named("check") {
    dependsOn("apiTest", "integrationTest")  // Ensure they run during `./gradlew build`
}