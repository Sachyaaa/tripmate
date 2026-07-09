# Starts local Kafka 4.3.1 in KRaft mode (no ZooKeeper, no Docker, no admin).
# Leave this window open while you work; press Ctrl+C to stop Kafka.
# Broker listens on localhost:9092 (that's what the app connects to).

# The machine's JAVA_HOME is broken/stale, and PATH java is Java 8.
# Kafka 4.x needs Java 17+, so point it at IntelliJ's bundled JDK 17 explicitly.
$env:JAVA_HOME = "C:\Users\sdivase\.jdks\ms-17.0.15"

$KAFKA = "C:\Users\sdivase\kafka"
Write-Host "Starting Kafka (KRaft) on localhost:9092 ..." -ForegroundColor Green
Write-Host "Using JAVA_HOME=$env:JAVA_HOME" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop." -ForegroundColor Yellow
& "$KAFKA\bin\windows\kafka-server-start.bat" "$KAFKA\config\server.properties"
