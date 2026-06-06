ALTER TABLE usage_data RENAME COLUMN "communityProduced" TO community_produced;
ALTER TABLE usage_data RENAME COLUMN "communityUsed" TO community_used;
ALTER TABLE usage_data RENAME COLUMN "gridUsed" TO grid_used;

ALTER TABLE percentage RENAME COLUMN "communityDepleted" TO community_depleted;
ALTER TABLE percentage RENAME COLUMN "gridPortion" TO grid_portion;