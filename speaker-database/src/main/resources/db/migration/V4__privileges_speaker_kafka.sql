DO $$BEGIN
    IF EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'speaker-kafka') THEN
        GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "speaker-kafka";
        GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO "speaker-kafka";
    END IF;
END$$;
