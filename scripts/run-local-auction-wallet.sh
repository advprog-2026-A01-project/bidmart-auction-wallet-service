set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo ">>> Project root: $PROJECT_ROOT"
cd "$PROJECT_ROOT"

CONTAINER_NAME="bidmart-auction-wallet-db"

if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
  echo ""
  echo "ERROR: Container '$CONTAINER_NAME' is not running."
  echo "Jalankan database terlebih dahulu:"
  echo "  ./scripts/setup-local-auction-wallet-db.sh"
  exit 1
fi

export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5435/auction_wallet_db"
export SPRING_DATASOURCE_USERNAME="auction_wallet"
export SPRING_DATASOURCE_PASSWORD="auction_wallet"

export SPRING_RABBITMQ_HOST="localhost"
export SPRING_RABBITMQ_PORT="5672"
export SPRING_RABBITMQ_USERNAME="guest"
export SPRING_RABBITMQ_PASSWORD="guest"

export GATEWAY_SECRET="${GATEWAY_SECRET:-local-dev-gateway-secret}"
export AUTH_SERVICE_URL="${AUTH_SERVICE_URL:-http://localhost:8081}"

export SERVER_PORT="8083"

echo "=========================================="
echo "  Starting bidmart-auction-wallet-service"
echo "  Port     : $SERVER_PORT"
echo "  DB URL   : $SPRING_DATASOURCE_URL"
echo "=========================================="

./gradlew bootRun