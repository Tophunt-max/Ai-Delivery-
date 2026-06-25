-- D1 Database Schema for Delivery Intelligence App
CREATE TABLE IF NOT EXISTS profile (
    id INTEGER PRIMARY KEY,
    name TEXT,
    aiEfficiencyScore REAL,
    fuelSavedLiters REAL,
    distanceTravelledKm REAL,
    timestamp INTEGER
);

CREATE TABLE IF NOT EXISTS parcels (
    parcelId TEXT PRIMARY KEY,
    customerName TEXT,
    customerMobile TEXT,
    fullAddress TEXT,
    latitude REAL,
    longitude REAL,
    codAmount REAL,
    deliveryNotes TEXT,
    company TEXT,
    status TEXT,
    failedReason TEXT
);

CREATE TABLE IF NOT EXISTS learning_records (
    area TEXT PRIMARY KEY,
    landmark TEXT,
    notes TEXT,
    preferredTime TEXT,
    customerAvailability REAL
);
