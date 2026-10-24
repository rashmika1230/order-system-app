-- Runs ONLY the first time the container starts with an empty data volume.
-- These are throwaway local development credentials, not real secrets.
-- Production credentials live in AWS SSM Parameter Store, never in Git.

CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS order_db;

CREATE USER IF NOT EXISTS 'product_user'@'%' IDENTIFIED BY 'product_pass';
CREATE USER IF NOT EXISTS 'order_user'@'%' IDENTIFIED BY 'order_pass';

-- Each user can only access its own database (database-per-service)
GRANT ALL PRIVILEGES ON product_db.* TO 'product_user'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'order_user'@'%';
