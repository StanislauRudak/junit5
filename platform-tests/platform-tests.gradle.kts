plugins {
	id("me.champeau.gradle.jmh")
}

apply(from = "$rootDir/gradle/testing.gradle.kts")

tasks.named<Test>("test") {
	useJUnitPlatform {
		excludeTags("exclude")
	}
}

dependencies {
	// --- Things we are testing --------------------------------------------------
	testImplementation(project(":junit-platform-commons"))
	testImplementation(project(":junit-platform-console"))
	testImplementation(project(":junit-platform-engine"))
	testImplementation(project(":junit-platform-launcher"))

	// --- Things we are testing with ---------------------------------------------
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(":junit-platform-testkit"))
	testImplementation(project(path = ":junit-jupiter-engine", configuration = "testArtifacts"))
	testImplementation("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	// --- Test run-time dependencies ---------------------------------------------
	testRuntimeOnly(project(":junit-vintage-engine"))
	testRuntimeOnly("org.codehaus.groovy:groovy-jsr223:${Versions.groovy}") {
		because("Tests annotated with @EnabledIf(engine = \"groovy\", ...) need it.")
	}

	// --- http://openjdk.java.net/projects/code-tools/jmh/ -----------------------
	jmh("org.openjdk.jmh:jmh-core:${Versions.jmh}") {
		exclude(module = "jopt-simple")
	}
	jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:${Versions.jmh}")
	jmh(project(":junit-jupiter-api"))
	jmh("junit:junit:${Versions.junit4}")
}

jmh {
	jmhVersion = Versions.jmh

	duplicateClassesStrategy = DuplicatesStrategy.WARN
	fork = 0 // Too long command line on Windows...
	warmupIterations = 1
	iterations = 5
}

tasks.named<Checkstyle>("checkstyleJmh") { // use same style rules as defined for tests
	configFile = rootProject.file("src/checkstyle/checkstyleTest.xml")
}

eclipse {
	classpath {
		plusConfigurations.add(project(":junit-platform-console").configurations["shadowed"])
	}
}

idea {
	module {
		scopes["PROVIDED"]!!["plus"]!!.add(project(":junit-platform-console").configurations["shadowed"])
	}
}
