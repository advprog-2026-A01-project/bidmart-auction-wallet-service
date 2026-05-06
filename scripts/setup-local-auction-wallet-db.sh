set -e

CONTAINER_NAME="bidmart-auction-wallet-db"
DB_NAME="auction_wallet_db"
DB_USER="auction_wallet"
DB_PASSWORD="auction_wallet"
HOST_PORT="5435"
CONTAINER_PORT="5432"
PG_VERSION="16"

echo ">>> Removing existing container (if any)..."
docker rm -f "$CONTAINER_NAME" 2>/dev/null || true

echo ">>> Starting PostgreSQL container: $CONTAINER_NAME"
docker run --name "$CONTAINER_NAME" \
  -e POSTGRES_DB="$DB_NAME" \
  -e POSTGRES_USER="$DB_USER" \
  -e POSTGRES_PASSWORD="$DB_PASSWORD" \
  -p "$HOST_PORT:$CONTAINER_PORT" \
  -d postgres:"$PG_VERSION"

echo ""
echo ">>> Waiting for PostgreSQL to be ready..."
sleep 3

echo ""
echo ">>> Container status:"
docker ps | grep "$CONTAINER_NAME" || echo "WARNING: Container not found in docker ps"

echo ""
echo "=========================================="
echo "  auction_wallet_db is running!"
echo "  Host     : localhost"
echo "  Port     : $HOST_PORT"
echo "  DB       : $DB_NAME"
echo "  User     : $DB_USER"
echo "=========================================="