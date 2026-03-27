$ErrorActionPreference = "Stop"

$env:SPRING_PROFILES_ACTIVE = "local"
$env:DB_URL = "jdbc:postgresql://localhost:5432/fatihozkurtcom"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
$env:REDIS_HOST = "localhost"
$env:REDIS_PORT = "6379"

& "$PSScriptRoot\..\mvnw.cmd" spring-boot:run
