ALTER TABLE `vodafone_transaction` ADD COLUMN `profile_id` VARCHAR(30) NULL  AFTER `acr_trunc` ;

ALTER TABLE `apple_receipt` CHANGE COLUMN `username` `username` VARCHAR(255) NULL  ;
ALTER TABLE `apple_receipt` ADD COLUMN `profile_id` VARCHAR(45) NULL  AFTER `lastUpdated`, ADD INDEX `PROFILEID_PRODUCT` (`profile_id` ASC, `lastUpdated` ASC) ;

ALTER TABLE `transaction` ADD COLUMN `profile_id` VARCHAR(45) NULL  AFTER `username` 
, ADD INDEX `IX_profile_id_expiry_date` (`profile_id` ASC, `expiry_date` ASC) ;

ALTER TABLE `transaction` CHANGE COLUMN `transaction_id` `transaction_id` VARCHAR(255) NULL DEFAULT NULL  ;



ALTER TABLE `transaction_audit` ADD COLUMN `profile_id` VARCHAR(45) NULL  AFTER `username` 
, ADD INDEX `IX_profile_id_expiry_date` (`profile_id` ASC, `expiry_date` ASC) ;


ALTER TABLE `transaction_audit` CHANGE COLUMN `username` `username` VARCHAR(255) NULL  ;
ALTER TABLE `transaction_audit` CHANGE COLUMN `app_item_id` `app_item_id` VARCHAR(255) NULL DEFAULT NULL  , CHANGE COLUMN `bid` `bid` VARCHAR(255) NULL DEFAULT NULL  , CHANGE COLUMN `bvrs` `bvrs` VARCHAR(255) NULL DEFAULT NULL  , CHANGE COLUMN `expiry_date` `expiry_date` DATETIME NULL DEFAULT NULL  , CHANGE COLUMN `original_purchase_date` `original_purchase_date` DATETIME NULL DEFAULT NULL  , CHANGE COLUMN `original_transaction_id` `original_transaction_id` VARCHAR(255) NULL DEFAULT NULL  , CHANGE COLUMN `product_id` `product_id` VARCHAR(255) NULL DEFAULT NULL  , CHANGE COLUMN `purchase_date` `purchase_date` DATETIME NULL DEFAULT NULL  , CHANGE COLUMN `quantity` `quantity` INT(11) NULL DEFAULT NULL  , CHANGE COLUMN `status` `status` VARCHAR(255) NULL DEFAULT NULL  , CHANGE COLUMN `transaction_id` `transaction_id` VARCHAR(255) NULL DEFAULT NULL  ;



ALTER TABLE `android_transaction_audit` ADD COLUMN `profile_id` VARCHAR(30) NULL  AFTER `username` ;
ALTER TABLE `android_transaction_audit` CHANGE COLUMN `username` `username` VARCHAR(255) NULL DEFAULT NULL  ;

ALTER TABLE `android_transaction` CHANGE COLUMN `username` `username` VARCHAR(255) NULL DEFAULT NULL  , ADD COLUMN `profile_id` VARCHAR(30) NULL DEFAULT NULL  AFTER `username` ;

ALTER TABLE `android_receipt` CHANGE COLUMN `username` `username` VARCHAR(255) NULL DEFAULT NULL  , ADD COLUMN `profile_id` VARCHAR(30) NULL DEFAULT NULL  AFTER `username` ;

ALTER TABLE `vodafone_token` CHANGE COLUMN `skyid` `skyid` VARCHAR(40) NULL DEFAULT NULL  , ADD COLUMN `profile_id` VARCHAR(45) NULL DEFAULT NULL  AFTER `udid` ;



