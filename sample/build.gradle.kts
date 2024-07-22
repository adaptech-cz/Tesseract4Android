plugins {
	id("com.android.application")
	alias(libs.plugins.jetbrains.kotlin.android)
}

android {
	namespace = "cz.adaptech.tesseract4android.sample"
	compileSdk = 34

	defaultConfig {
		applicationId = "cz.adaptech.tesseract4android.sample"
		minSdk = 21
		targetSdk = 34
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	buildFeatures {
		viewBinding = true
	}
	kotlinOptions {
		jvmTarget = "17"
	}
}

// In case you are using dependency on local library (the project(":tesseract4android") below),
// uncomment this to specify which flavor you want to build.
// Or you can specify *same* flavors also for the app - then they will be matched automatically.
// See more: https://developer.android.com/studio/build/build-variants#variant_aware
/*android {
    defaultConfig {
        // Choose "standard" or "openmp" flavor of the library
        missingDimensionStrategy "parallelization", "standard"
    }
    flavorDimensions = ["parallelization"]
}*/

dependencies {
	// To use library from JitPack
	implementation(libs.tesseract4android.jitpack) // standard flavor
	//implementation(libs.tesseract4android.jitpack.openmp) // openmp flavor

	// To use library from local maven repository
	// Don't forget to specify mavenLocal() in repositories block in project's build.gradle file
	//implementation(libs.tesseract4android.local) // standard flavor
	//implementation(libs.tesseract4android.local.openmp) // openmp flavor

	// To use library compiled locally
	// Which flavor to use is determined by missingDimensionStrategy parameter above.
	//implementation(project(":tesseract4android"))

	implementation(libs.androidx.appcompat)
	implementation(libs.material)
	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.lifecycle.livedata)
	implementation(libs.androidx.lifecycle.viewmodel)
	implementation(libs.androidx.core.ktx)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}