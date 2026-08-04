-- Initial schema migration for LieDetector
-- This script corresponds to the tables created by SchemaUtils initially.

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    firebase_uid VARCHAR(128) UNIQUE NOT NULL,
    email VARCHAR(255),
    display_name VARCHAR(255),
    occupation VARCHAR(255),
    preferences JSONB,
    additional_data JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_devices (
    id SERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    device_id VARCHAR(255) NOT NULL,
    model VARCHAR(255),
    os_version VARCHAR(50),
    app_version VARCHAR(50),
    language VARCHAR(10),
    last_sync_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS subjects (
    id UUID PRIMARY KEY,
    owner_id UUID REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    avatar TEXT,
    is_default_avatar BOOLEAN DEFAULT TRUE,
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    personality_config JSONB,
    stats JSONB,
    additional_data JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
