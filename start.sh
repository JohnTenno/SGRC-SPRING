#!/bin/bash
set -e

echo "▶ Start MySQL + RabbitMQ..."
cd Db && docker compose up -d && cd ..

echo "▶ Start Spring Boot..."
./mvnw spring-boot:run
