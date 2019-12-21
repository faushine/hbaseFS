CREATE DATABASE IF NOT EXISTS HOS_SERVICE DEFAULT CHARACTER SET UTF8 COLLATE UTF8_GENERAL_CI;

USE HOS_SERVICE;

CREATE TABLE USER_INFO(
	USER_ID VARCHAR(32) NOT NULL,
	USER_NAME VARCHAR(32) NOT NULL,
	PASSWORD VARCHAR(64) NOT NULL COMMENT 'BY MD5',
	SYSTEM_ROLE VARCHAR(32) NOT NULL,
	DETAIL VARCHAR(256),
	CREATE_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (USER_ID),
	UNIQUE KEY AK_UQ_USER_NAME (USER_NAME)
)

CREATE TABLE TOKEN_INFO(
	TOKEN VARCHAR(32) NOT NULL,
	EXPIRE_TIME VARCHAR(32) NOT NULL,
	CREATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	REFRESH_TIME TIMESTAMP NOT NULL,
	ACTIVE TINYINT NOT NULL,
	CREATOR VARCHAR(32) NOT NULL,
	PRIMARY KEY (TOKEN)
)


CREATE TABLE SERVICE_AUTH(
	BUCKET_NAME VARCHAR(32) NOT NULL,
	TARGET_TOKEN VARCHAR(32) NOT NULL,
	AUTH_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (BUCKET_NAME,TARGET_TOKEN)
)


CREATE TABLE HOS_BUCKET(
	BUCKET_ID VARCHAR(32) NOT NULL,
	BUCKET_NAME VARCHAR(32) NOT NULL,
	CREATOR VARCHAR(32) NOT NULL,
	DETAIL VARCHAR(256),
	CREATE_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (BUCKET_ID),
	UNIQUE KEY AK_UQ_USER_NAME (BUCKET_NAME)
)


USE HOS_SERVICE

SHOW TABLES