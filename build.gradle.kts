plugins {
	id("com.gradle.plugin-publish") version "0.9.10"
	id("com.github.ben-manes.versions") version "0.20.0"
	`java-gradle-plugin`
	maven
	`embedded-kotlin`
	`kotlin-dsl`
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
}


group = "com.github.ramonwirsch"
version = "0.2.0"

repositories {
	jcenter()
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
	compile("org.apache.avalon.framework:avalon-framework-impl:4.3.1")
	compile("org.apache.xmlgraphics:fop:1.1")
	runtime("net.sf.offo:fop-hyph:2.0")
	compile("xalan:xalan:2.7.2")
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
