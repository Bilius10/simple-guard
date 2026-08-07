#!/bin/bash
set -e

psql --set ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
  --set keycloak_database="$KEYCLOAK_DB" <<-'EOSQL'
	CREATE DATABASE :"keycloak_database";
EOSQL
