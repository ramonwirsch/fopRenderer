# fopRenderer
Gradle Plugin for Validating XMLs and rendering them with Apache FOP.

*Note: The plugin is no longer in active development by me. I will try to maintain existing functionality whenever needed. Also see 'Contributing'.*

## Usage

### `plugins` block:

### groovy
```groovy
plugins {
  id "com.github.ramonwirsch.FopRenderer" version "0.4.0"
}
```
### kotlin
```kotlin
plugins {
    id("com.github.ramonwirsch.FopRenderer") version "0.4.0"
}
```
or via the

### `buildscript` block (groovy):
```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.github.ramonwirsch:fopRenderer:0.4.0"
  }
}

apply plugin: "com.github.ramonwirsch.FopRenderer"
```

## `fopRenderer` configuration:
### groovy
```groovy
fopRenderer {
  // groups of files to validated each against 1 schema (URL or local file)
	schemas {
		schemaGroupName { // used for task names
			files = file('some.xml') // or fileTree(dir: 'xmlDir', include: '*.xml')
			schemaUri = 'http://www.url.com/to/some/schema.xsd'
			offlineSchema = file('offlineSchema.xsd') // [optional]
			useInherentSchemas = false // [default: false] uses xsi:schemaLocation tags or Doctype statements in the XML files for validation instead of forced schema
		}
		// ... as many different groups as you like
	}
	
	// files that should be rendered with FOP
	render {
		renderGroupName { // used for task names
			stylesheet = file('stylesheet.xsl')
			rootSrc = file('main/file/to/render.xml')
			dependencies = fileTree('dep-files/*.xml') // files that are monitored by transformTask. Defaults to siblings of rootSrc
			resourcesBaseDir = file('resources/dir') // pictures and other resources. Links will be interpreted relative to this
			resourceCollectionParams = [exclude: '**/*.xml'] // default: params for resource fileTree.
			// resources + resourceCollectionParams will be combined into a FileCollection that is monitored for changes by the renderTask
			requiresValidation = true // [default: true] whether to require passing of schema validation before attempting to transform/render
		}
	}
}
```
### kotlin
```kotlin
fopRenderer {
  // groups of files to validated each against 1 schema (URL or local file)
	schemas {
		"schemaGroupName" { // used for task names
			files = file("some.xml") // or fileTree(dir: "xmlDir", include: "*.xml")
			setSchemaUri("http://www.url.com/to/some/schema.xsd")
			offlineSchema = file("offlineSchema.xsd") // [optional]
			isUseInherentSchemas = false // [default: false] uses xsi:schemaLocation tags or Doctype statements in the XML files for validation instead of forced schema
		}
		// ... as many different groups as you like
	}
	
	// files that should be rendered with FOP
	render {
		"renderGroupName" { // used for task names
			stylesheet = file("stylesheet.xsl")
			rootSrc = file("main/file/to/render.xml")
			dependencies = fileTree("dep-files/*.xml") // files that are monitored by transformTask. Defaults to siblings of rootSrc
			resourcesBaseDir = file("resources/dir") // pictures and other resources. Links will be interpreted relative to this
			resourceCollectionParams = mapOf("exclude" to "**/*.xml") // default: params for resource fileTree.
			// resources + resourceCollectionParams will be combined into a FileCollection that is monitored for changes by the renderTask
			isRequiresValidation = true // [default: true] whether to require passing of schema validation before attempting to transform/render
		}
	}
}
```

The plugin will prioritize the online schemas if they are set, except if the gradle property `offlineSchemas` is true.
This can be done via command line with `-PofflineSchemas=true`.

The `render` block is optional, as is any actual schema group, so both functions can be used pretty much separately.

## tasks
* `validate<SchemaGroupName>` tasks for every schema group
* `validate` task that depends on all other validations
* `transform<RenderGroupName>` tasks for every render group. Apply the respective XSLT stylesheets.
* `render<RenderGroupName>` tasks for every render group. This will use FOP to generate a PDF.

the `render<>` tasks depend each on their `transform<>` task, which in turn depends on `validate`.
the base `build` task is configured to depend on all `render<>` tasks, the base `check` task is configured to depend on `validate`


# Contributing
I originally wrote this plugin to replace really old Ant and Make code in a preexisting project. Therefore the plugin did, what I needed it to do and I just used said project to test the plugin, instead of writing automated tests for it.
Given, that I no longer have any active need for the plugin, I am very unlikely to add any new features to it. I am however happy to update the plugin to keep working on new Gradle versions. If the plugin starts failing, create an issue.
I am also happy to accept merge requests with additional functionality and will test the plugin against my old use case and publish it.
But if new features are added, we really also need to add (automated) tests, as I otherwise cannot verify the plugin keeps working, before publishing updates. I will try to find time and aid in adding CI support and tests for my existing functionality, should there be any interest in doing so.
