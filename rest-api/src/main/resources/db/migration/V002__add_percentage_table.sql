CREATE TABLE IF NOT EXISTS "percentage" (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "hour" Timestamp NOT NULL,
    "communityDepleted" float NOT NULL,
    "gridPortion" float NOT NULL
    );