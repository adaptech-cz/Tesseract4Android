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
		vectorDrawables {
			useSupportLibrary = true
		}
	}

	buildTypes {
		release {
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
		compose = true
	}
	kotlinOptions {
		jvmTarget = "17"
	}
	composeOptions {
		kotlinCompilerExtensionVersion = "1.5.1"
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
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

	implementation(libs.material)
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.ui)
	implementation(libs.androidx.ui.graphics)
	implementation(libs.androidx.ui.tooling.preview)
	implementation(libs.androidx.material3)
	implementation(libs.androidx.window)

	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.lifecycle.viewmodel)
	implementation(libs.androidx.lifecycle.viewmodel.compose)
	implementation(libs.androidx.adaptive.android)

	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.ui.test.junit4)
	debugImplementation(libs.androidx.ui.tooling)
	debugImplementation(libs.androidx.ui.test.manifest)
}