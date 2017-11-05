plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
}

dependencies {
  compile(gradleApi())
  compile("org.jetbrains.kotlin:kotlin-stdlib:1.1.4")
  compile("org.jetbrains.kotlin:kotlin-reflect:1.1.4")
  compile("com.squareup:kotlinpoet:0.6.0")
}
