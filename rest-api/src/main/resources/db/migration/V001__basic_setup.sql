CREATE TABLE IF NOT EXISTS "usage-data" (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "hour" Timestamp NOT NULL,
    "communityProduced" float NOT NULL,
    "communityUsed" float NOT NULL,
    "gridUsed" float NOT NULL
    );