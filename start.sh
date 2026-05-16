#!/bin/bash
set -e

echo "▶ Start MySQL + RabbitMQ..."
cd Db && docker compose up -d && cd ..

echo "▶ Start Spring Boot..."

# Liberar puerto 3000 si quedó una instancia anterior (Ctrl+C no siempre mata el JVM)
free_port() {
  local port=$1 pid
  if command -v lsof >/dev/null 2>&1; then
    pid=$(lsof -ti:"$port" 2>/dev/null | head -1)
    [ -n "$pid" ] && kill -9 "$pid" 2>/dev/null && echo "   Puerto $port liberado (PID $pid)"
  else
    pid=$(netstat -ano 2>/dev/null | grep ":$port " | grep LISTENING | awk '{print $NF}' | head -1)
    if [ -n "$pid" ] && [ "$pid" != "0" ]; then
      taskkill //F //PID "$pid" 2>/dev/null && echo "   Puerto $port liberado (PID $pid)"
    fi
  fi
}
free_port 3000

./mvnw spring-boot:run
