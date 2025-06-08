dependencies {
    implementation(project(":bank-domain"))
    implementation(project(":bank-core"))
    implementation(project(":bank-monitoring"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework:spring-aspects")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}