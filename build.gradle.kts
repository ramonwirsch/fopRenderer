apply {
	plugin("groovy")
	plugin("maven")
}

group = "de.tu_darmstadt.rs"
version = 0.1



repositories {
	jcenter()
}


dependencies {
	compile(gradleApi())
	compile(localGroovy())

	compile("org.apache.avalon.framework:avalon-framework-impl:4.3.1")
	compile("org.apache.xmlgraphics:fop:1.1")
	runtime("net.sf.offo:fop-hyph:2.0")
	compile("xalan:xalan:2.7.2")
}

