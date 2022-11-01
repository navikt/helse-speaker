DO $$ BEGIN
    IF EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'speaker-kafka')
    THEN
        ALTER DEFAULT PRIVILEGES FOR USER "speaker-backend" IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO "speaker-kafka";
        ALTER DEFAULT PRIVILEGES FOR USER "speaker-backend" IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO "speaker-kafka";
    END IF;
END $$;
