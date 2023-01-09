plugins {
	id("com.gradle.plugin-publish") version "0.19.0"
	id("com.github.ben-manes.versions") version "0.41.0"
	`java-gradle-plugin`
	`maven-publish`
	`embedded-kotlin`
	`kotlin-dsl`
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
}


group = "com.github.ramonwirsch"
version = "0.4.0"

repositories {
	gradlePluginPortal()
	mavenCentral()
}

tasks.withType<Javadoc>().configureEach {
	isEnabled = false
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	kotlinOptions {
		jvmTarget = "1.8"
	}
}

gradlePlugin {
//	testSourceSets(java.sourceSets["functionalTest"])
}

dependencies {
	implementation("org.apache.avalon.framework:avalon-framework-impl:4.3.1")
	implementation("org.apache.xmlgraphics:fop:2.8")
	implementation("net.sf.offo:fop-hyph:2.0")
	implementation("xalan:xalan:2.7.2")
}

pluginBundle {
	website = "https://github.com/ramonwirsch/fopRenderer"
	vcsUrl = "https://github.com/ramonwirsch/fopRenderer"
	tags = listOf("Apache FOP", "FOP", "XML", "XSD", "Validation", "XML Schema Validation")
}
gradlePlugin {
	plugins {
		create("fopRendererPlugin") {
			id = "com.github.ramonwirsch.FopRenderer"
			displayName = "FOP Renderer plugin"
			description = "Plugin that can render documents using Apache FOP. It can also validate XMLs against XSD schemas."
			implementationClass = "com.github.ramonwirsch.fopRenderer.FopRendererPlugin"
		}
	}
}
