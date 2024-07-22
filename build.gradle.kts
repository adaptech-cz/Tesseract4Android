// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
	repositories {
		google()
		mavenCentral()

	}
	dependencies {
		classpath(libs.gradle)

		// NOTE: Do not place your application dependencies here; they belong
		// in the individual module build.gradle files
	}
}

allprojects {
	repositories {
		mavenLocal()
		google()
		mavenCentral()
		maven("https://jitpack.io")
	}
}

tasks.register<Delete>("clean") {
	delete(rootProject.buildDir)
}
