CREATE TABLE vehicles (
                          id                  BIGSERIAL    PRIMARY KEY,
                          vehicle_number      VARCHAR(50)  NOT NULL UNIQUE,
                          vehicle_type        VARCHAR(30)  NOT NULL,
                          brand               VARCHAR(100) NOT NULL,
                          model               VARCHAR(100) NOT NULL,
                          manufacturing_year  INTEGER,
                          color               VARCHAR(50),
                          fuel_type           VARCHAR(30)  NOT NULL,
                          seating_capacity    INTEGER,
                          driver_name         VARCHAR(100),
                          driver_phone        VARCHAR(20),
                          status              VARCHAR(30)  NOT NULL,
                          created_at          TIMESTAMP,
                          updated_at          TIMESTAMP
);