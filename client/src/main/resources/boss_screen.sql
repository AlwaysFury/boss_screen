/*
 Navicat Premium Data Transfer

 Source Server         : mysql_120.25.154.29
 Source Server Type    : MySQL
 Source Server Version : 80021 (8.0.21)
 Source Host           : 120.25.154.29:3306
 Source Schema         : boss_screen

 Target Server Type    : MySQL
 Target Server Version : 80021 (8.0.21)
 File Encoding         : 65001

 Date: 04/06/2024 16:54:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_cost
-- ----------------------------
DROP TABLE IF EXISTS `tb_cost`;
CREATE TABLE `tb_cost` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` varchar(255) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `exchange_rate` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for tb_escrow_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_escrow_info`;
CREATE TABLE `tb_escrow_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_sn` varchar(255) DEFAULT NULL,
  `buyer_user_name` varchar(255) DEFAULT NULL,
  `buyer_total_amount` decimal(10,2) DEFAULT NULL,
  `buyer_paid_shipping_fee` decimal(10,2) DEFAULT NULL,
  `actual_shipping_fee` decimal(10,2) DEFAULT NULL,
  `escrow_amount` decimal(10,2) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `adjustment_amount` decimal(10,2) DEFAULT NULL,
  `adjustment_reason` text,
  PRIMARY KEY (`id`),
  KEY `orderSn_index` (`order_sn`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9705 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for tb_escrow_item
-- ----------------------------
DROP TABLE IF EXISTS `tb_escrow_item`;
CREATE TABLE `tb_escrow_item` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_sn` varchar(255) DEFAULT NULL,
  `item_id` bigint DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `item_sku` varchar(255) DEFAULT NULL,
  `model_id` bigint DEFAULT NULL,
  `model_name` varchar(255) DEFAULT NULL,
  `model_sku` varchar(255) DEFAULT NULL,
  `count` int DEFAULT NULL,
  `original_price` decimal(10,2) DEFAULT NULL,
  `selling_price` decimal(10,2) DEFAULT NULL,
  `discounted_price` decimal(10,2) DEFAULT NULL,
  `seller_discount` decimal(10,2) DEFAULT NULL,
  `activity_id` bigint DEFAULT NULL,
  `activity_type` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `orderSn_index` (`order_sn`) USING BTREE,
  KEY `itemId_index` (`item_id`) USING BTREE,
  KEY `modelId_index` (`model_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for tb_main_account
-- ----------------------------
DROP TABLE IF EXISTS `tb_main_account`;
CREATE TABLE `tb_main_account` (
  `id` int NOT NULL AUTO_INCREMENT,
  `account_id` bigint DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `auth_code` varchar(255) DEFAULT NULL,
  `access_token` varchar(255) DEFAULT NULL,
  `refresh_token` varchar(255) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `accountId_index` (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for tb_model
-- ----------------------------
DROP TABLE IF EXISTS `tb_model`;
CREATE TABLE `tb_model` (
  `id` int NOT NULL AUTO_INCREMENT,
  `model_id` bigint DEFAULT NULL,
  `model_name` varchar(255) DEFAULT NULL,
  `model_sku` varchar(255) DEFAULT NULL,
  `current_price` decimal(10,2) DEFAULT NULL,
  `original_price` decimal(10,2) DEFAULT NULL,
  `stock` int DEFAULT NULL,
  `promotion_id` bigint DEFAULT NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `image_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `item_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `modelId_index` (`model_id`) USING BTREE,
  KEY `itemId_index` (`item_id`) USING BTREE,
  KEY `status_index` (`status`) USING BTREE,
  KEY `createTime_index` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=790825 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for tb_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `tb_operation_log`;
CREATE TABLE `tb_operation_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `opt_type` varchar(255) DEFAULT NULL,
  `opt_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `status` int DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for tb_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_order`;
CREATE TABLE `tb_order` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_sn` varchar(255) DEFAULT NULL,
  `tracking_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `pay_time` bigint DEFAULT NULL,
  `buyer_uer_id` bigint DEFAULT NULL,
  `buyer_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `cancel_reason` varchar(255) DEFAULT NULL,
  `cancel_by` varchar(255) DEFAULT NULL,
  `buyer_cancel_reason` text,
  `create_time` bigint DEFAULT NULL,
  `update_time` bigint DEFAULT NULL,
  `shop_id` bigint DEFAULT NULL,
  `shipping_carrier` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `orderSn_index` (`order_sn`) USING BTREE,
  KEY `createTIme_index` (`create_time`) USING BTREE,
  KEY `shopId_index` (`shop_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9480 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for tb_order_item
-- ----------------------------
DROP TABLE IF EXISTS `tb_order_item`;
CREATE TABLE `tb_order_item` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_sn` varchar(255) DEFAULT NULL,
  `tracking_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `item_id` bigint DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `item_sku` varchar(255) DEFAULT NULL,
  `model_id` bigint DEFAULT NULL,
  `model_name` varchar(255) DEFAULT NULL,
  `model_sku` varchar(255) DEFAULT NULL,
  `count` int DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `promotion_id` bigint DEFAULT NULL,
  `promotion_type` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `cost` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `orderSn_index` (`order_sn`) USING BTREE,
  KEY `itemId_index` (`item_id`) USING BTREE,
  KEY `modelId_index` (`model_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13779 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for tb_product
-- ----------------------------
DROP TABLE IF EXISTS `tb_product`;
CREATE TABLE `tb_product` (
  `id` int NOT NULL AUTO_INCREMENT,
  `item_id` bigint DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `item_sku` varchar(255) DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `mainImg_id` varchar(255) DEFAULT NULL,
  `mainImg_url` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `shop_id` bigint DEFAULT NULL,
  `create_time` bigint DEFAULT NULL,
  `update_time` bigint DEFAULT NULL,
  `grade` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `itemId_index` (`item_id`) USING BTREE,
  KEY `shopId_index` (`item_id`) USING BTREE,
  KEY `createTime_index` (`create_time`) USING BTREE,
  KEY `grade_index` (`grade`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=22823 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for tb_return_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_return_order`;
CREATE TABLE `tb_return_order` (
  `id` int NOT NULL AUTO_INCREMENT,
  `return_sn` varchar(255) DEFAULT NULL,
  `order_sn` varchar(255) DEFAULT NULL,
  `reason` text,
  `text_reason` text,
  `refund_amount` decimal(10,2) DEFAULT NULL,
  `amount_before_discount` decimal(10,2) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `create_time` bigint DEFAULT NULL,
  `update_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=603 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for tb_return_order_item
-- ----------------------------
DROP TABLE IF EXISTS `tb_return_order_item`;
CREATE TABLE `tb_return_order_item` (
  `id` int NOT NULL AUTO_INCREMENT,
  `return_sn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `item_Id` bigint DEFAULT NULL,
  `item_sku` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `item_price` decimal(10,2) DEFAULT NULL,
  `variation_sku` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `model_id` bigint DEFAULT NULL,
  `amount` int DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=811 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for tb_rule
-- ----------------------------
DROP TABLE IF EXISTS `tb_rule`;
CREATE TABLE `tb_rule` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `grade` varchar(255) DEFAULT NULL,
  `all_or_not` tinyint(1) DEFAULT NULL,
  `rule_data` json DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `index_grade` (`grade`) USING BTREE,
  KEY `index_name` (`name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for tb_shop
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop`;
CREATE TABLE `tb_shop` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` bigint DEFAULT NULL,
  `account_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `merchant_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `auth_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `access_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `refresh_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `status` int DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `shopId_index` (`shop_id`) USING BTREE,
  KEY `accountId_index` (`account_id`) USING BTREE,
  KEY `createTime_index` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for tb_shop_copy1
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop_copy1`;
CREATE TABLE `tb_shop_copy1` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` bigint DEFAULT NULL,
  `account_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `merchant_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `auth_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `access_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `refresh_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `status` int DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `shopId_index` (`shop_id`) USING BTREE,
  KEY `accountId_index` (`account_id`) USING BTREE,
  KEY `createTime_index` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
