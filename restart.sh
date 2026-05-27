#!/bin/bash
set -e

echo "▶ Delete..."
cd Db && docker compose down -v && cd ..

echo "▶ start all..."
cd Db && docker compose up -d --wait && cd ..

wait_for_container() {
  local name=$1 check_cmd=$2
  echo "   waiting $name..."
  local attempts=0
  while [ "$attempts" -lt 60 ]; do
    if docker exec "$name" sh -c "$check_cmd" >/dev/null 2>&1; then
      echo "   $name ready."
      return 0
    fi
    attempts=$((attempts + 1))
    sleep 2
  done
  echo "   ERROR: $name no response."
  exit 1
}

wait_for_container sgrc_db "mysql -h 127.0.0.1 -P 3306 -u sgrc_user -psgrc_password sgrc_db -e 'SELECT 1 FROM \`user\` LIMIT 1' 2>/dev/null"
wait_for_container sgrc_rabbitmq "rabbitmq-diagnostics -q ping"

echo "▶ starting springboot..."

free_port() {
  local port=$1 pid
  if command -v lsof >/dev/null 2>&1; then
    pid=$(lsof -ti:"$port" 2>/dev/null | head -1)
    if [ -n "$pid" ]; then kill -9 "$pid" 2>/dev/null && echo "   port $port free (PID $pid)"; fi
  else
    pid=$(netstat -ano 2>/dev/null | grep ":$port " | grep LISTENING | awk '{print $NF}' | head -1)
    if [ -n "$pid" ] && [ "$pid" != "0" ]; then
      taskkill //F //PID "$pid" 2>/dev/null && echo "   port $port free (PID $pid)"
    fi
  fi
}
free_port 3000

./mvnw spring-boot:run
