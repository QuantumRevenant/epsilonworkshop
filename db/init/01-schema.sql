CREATE TYPE order_status AS ENUM ('pending', 'in_progress', 'finished', 'canceled');

CREATE TABLE "users"(
    "id" bigserial NOT NULL,
    "username" VARCHAR(255) NOT NULL,
    "password_hash" VARCHAR(255) NOT NULL,
    "role" VARCHAR(255) CHECK ("role" IN('admin', 'mechanic')) NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "paternal_surname" VARCHAR(255) NOT NULL,
    "maternal_surname" VARCHAR(255) NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE "users" ADD PRIMARY KEY("id");
ALTER TABLE "users" ADD CONSTRAINT "users_username_unique" UNIQUE("username");

CREATE TABLE "clients"(
    "id" bigserial NOT NULL,
    "document_type" VARCHAR(10) NOT NULL,
    "document" VARCHAR(255) NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "paternal_surname" VARCHAR(255) NULL,
    "maternal_surname" VARCHAR(255) NULL,
    "phone" VARCHAR(255) NOT NULL,
    "email" VARCHAR(255) NULL,
    "notes" TEXT NULL,
    "active" BOOLEAN NOT NULL DEFAULT TRUE,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE "clients" ADD PRIMARY KEY("id");
ALTER TABLE "clients" ADD CONSTRAINT "clients_document_unique" UNIQUE("document_type", "document");

CREATE TABLE "vehicles"(
    "id" bigserial NOT NULL,
    "client_id" BIGINT NOT NULL,
    "plate" VARCHAR(255) NOT NULL,
    "brand" VARCHAR(255) NOT NULL,
    "model" VARCHAR(255) NOT NULL,
    "year" INTEGER NULL,
    "notes" TEXT NULL,
    "active" BOOLEAN NOT NULL DEFAULT TRUE,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE "vehicles" ADD PRIMARY KEY("id");
ALTER TABLE "vehicles" ADD CONSTRAINT "vehicles_plate_unique" UNIQUE("plate");

CREATE TABLE "mechanics"(
    "id" bigserial NOT NULL,
    "user_id" BIGINT NOT NULL,
    "specialty" VARCHAR(255) NOT NULL,
    "active" BOOLEAN NOT NULL DEFAULT TRUE,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE "mechanics" ADD PRIMARY KEY("id");
ALTER TABLE "mechanics" ADD CONSTRAINT "mechanics_user_id_unique" UNIQUE("user_id");

CREATE TABLE "work_orders"(
    "id" bigserial NOT NULL,
    "vehicle_id" BIGINT NOT NULL,
    "mechanic_id" BIGINT NULL,
    "status" order_status NOT NULL,
    "description" TEXT NOT NULL,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "closed_at" TIMESTAMP(0) WITHOUT TIME ZONE NULL
);
ALTER TABLE "work_orders" ADD PRIMARY KEY("id");

CREATE TABLE "work_order_history"(
    "id" bigserial NOT NULL,
    "work_order_id" BIGINT NOT NULL,
    "user_id" BIGINT NOT NULL,
    "change_type" VARCHAR(255) CHECK ("change_type" IN('status_change', 'mechanic_reassignment')) NOT NULL,
    "old_value" VARCHAR(255) NOT NULL,
    "new_value" VARCHAR(255) NOT NULL,
    "changed_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE "work_order_history" ADD PRIMARY KEY("id");

CREATE TABLE "services"(
    "id" bigserial NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "description" TEXT NOT NULL,
    "reference_price" DECIMAL(10, 2) NOT NULL,
    "active" BOOLEAN NOT NULL DEFAULT TRUE
);
ALTER TABLE "services" ADD PRIMARY KEY("id");

CREATE TABLE "work_order_services"(
    "id" bigserial NOT NULL,
    "work_order_id" BIGINT NOT NULL,
    "service_id" BIGINT NOT NULL,
    "price_at_time" DECIMAL(10, 2) NOT NULL
);
ALTER TABLE "work_order_services" ADD PRIMARY KEY("id");

CREATE TABLE "parts"(
    "id" bigserial NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "stock" INTEGER NOT NULL CHECK ("stock" >= 0),
    "min_stock_threshold" INTEGER NOT NULL,
    "unit_price" DECIMAL(10, 2) NOT NULL,
    "active" BOOLEAN NOT NULL DEFAULT TRUE
);
ALTER TABLE "parts" ADD PRIMARY KEY("id");

CREATE TABLE "work_order_parts"(
    "id" bigserial NOT NULL,
    "work_order_id" BIGINT NOT NULL,
    "part_id" BIGINT NOT NULL,
    "quantity" INTEGER NOT NULL CHECK ("quantity" > 0),
    "price_at_time" DECIMAL(10, 2) NOT NULL
);
ALTER TABLE "work_order_parts" ADD PRIMARY KEY("id");

ALTER TABLE "work_order_services" ADD CONSTRAINT "work_order_services_service_id_foreign" FOREIGN KEY("service_id") REFERENCES "services"("id") ON DELETE RESTRICT;
ALTER TABLE "vehicles" ADD CONSTRAINT "vehicles_client_id_foreign" FOREIGN KEY("client_id") REFERENCES "clients"("id") ON DELETE RESTRICT;
ALTER TABLE "work_order_history" ADD CONSTRAINT "work_order_history_work_order_id_foreign" FOREIGN KEY("work_order_id") REFERENCES "work_orders"("id") ON DELETE RESTRICT;
ALTER TABLE "work_order_services" ADD CONSTRAINT "work_order_services_work_order_id_foreign" FOREIGN KEY("work_order_id") REFERENCES "work_orders"("id") ON DELETE RESTRICT;
ALTER TABLE "work_orders" ADD CONSTRAINT "work_orders_mechanic_id_foreign" FOREIGN KEY("mechanic_id") REFERENCES "mechanics"("id") ON DELETE RESTRICT;
ALTER TABLE "mechanics" ADD CONSTRAINT "mechanics_user_id_foreign" FOREIGN KEY("user_id") REFERENCES "users"("id") ON DELETE RESTRICT;
ALTER TABLE "work_order_parts" ADD CONSTRAINT "work_order_parts_work_order_id_foreign" FOREIGN KEY("work_order_id") REFERENCES "work_orders"("id") ON DELETE RESTRICT;
ALTER TABLE "work_order_history" ADD CONSTRAINT "work_order_history_user_id_foreign" FOREIGN KEY("user_id") REFERENCES "users"("id") ON DELETE RESTRICT;
ALTER TABLE "work_orders" ADD CONSTRAINT "work_orders_vehicle_id_foreign" FOREIGN KEY("vehicle_id") REFERENCES "vehicles"("id") ON DELETE RESTRICT;
ALTER TABLE "work_order_parts" ADD CONSTRAINT "work_order_parts_part_id_foreign" FOREIGN KEY("part_id") REFERENCES "parts"("id") ON DELETE RESTRICT;