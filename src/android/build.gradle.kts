buildscript {
    repositories {
        google()
        jcenter()
        maven (url="https://developer.huawei.com/repo/" ) // HUAWEI Maven repository

    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.3.70"))
        classpath("com.android.tools.build:gradle:4.2.2")
        classpath("com.beust:klaxon:5.0.1")
        classpath("com.huawei.agconnect:agcp:1.4.2.300")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven (url="https://developer.huawei.com/repo/" ) // HUAWEI Maven repository

        // maven(url = "https:// some custom repo")
        val nativeDir = if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            System.getenv("CORONA_ROOT")
        } else {
            "${System.getenv("HOME")}/Library/Application Support/Corona/Native/"
        }
        flatDir {
            dirs("$nativeDir/Corona/android/lib/gradle", "$nativeDir/Corona/android/lib/Corona/libs")
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
