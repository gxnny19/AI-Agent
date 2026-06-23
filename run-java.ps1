# PowerShell helper to run the Spring Boot app
# Usage: .\run-java.ps1

$env:JAVA_HOME = "C:\Users\USER\AppData\Local\jdks\jdk-21.0.10"
$env:PATH = "$env:JAVA_HOME\bin;C:\Users\USER\.maven\maven-3.9.16\bin;$env:PATH"

# Build and run via Maven
mvn clean package
mvn spring-boot:run
