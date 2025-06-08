dependencies {
    implementation(project(":bank-core"))
    implementation(project(":bank-domain"))
    implementation(project(":bank-event"))
    implementation(project(":bank-monitoring"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.mysql:mysql-connector-j")
    implementation("com.h2database:h2")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    
    // 테스트 의존성
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}