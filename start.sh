#!/bin/bash
set -e

echo "▶ Start MySQL + RabbitMQ..."
cd Db && docker compose up -d --wait && cd ..

wait_for_container() {
  local name=$1 check_cmd=$2
  echo "   Esperando $name..."
  local attempts=0
  while [ "$attempts" -lt 60 ]; do
    if docker exec "$name" sh -c "$check_cmd" >/dev/null 2>&1; then
      echo "   $name listo."
      return 0
    fi
    attempts=$((attempts + 1))
    sleep 2
  done
  echo "   ERROR: $name no respondió a tiempo."
  exit 1
}

wait_for_container sgrc_db "mysqladmin ping -h localhost -u sgrc_user -psgrc_password --silent"
wait_for_container sgrc_rabbitmq "rabbitmq-diagnostics -q ping"

echo "▶ Start Spring Boot..."

# Liberar puerto 3000 si quedó una instancia anterior (Ctrl+C no siempre mata el JVM)
free_port() {
  local port=$1 pid
  if command -v lsof >/dev/null 2>&1; then
    pid=$(lsof -ti:"$port" 2>/dev/null | head -1)
    [ -n "$pid" ] && kill -9 "$pid" 2>/dev/null && echo "   Puerto $port liberado (PID $pid)" || true
  else
    pid=$(netstat -ano 2>/dev/null | grep ":$port " | grep LISTENING | awk '{print $NF}' | head -1)
    if [ -n "$pid" ] && [ "$pid" != "0" ]; then
      taskkill //F //PID "$pid" 2>/dev/null && echo "   Puerto $port liberado (PID $pid)"
    fi
  fi
}
free_port 3000

./mvnw spring-boot:run
