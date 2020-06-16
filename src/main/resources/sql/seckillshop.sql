
-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `goods_name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `goods_title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `goods_img` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `goods_detail` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `goods_price` decimal(10, 2) NULL DEFAULT 0.00,
  `goods_stock` int(10) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (1, 'iphone X', 'Apple iPhoneX 64GB', '/images/iphonex.jpg', 'Apple iPhoneX 64GB 白色 移动联通电信', 5199.00, 100);
INSERT INTO `goods` VALUES (2, '小米6', '小米6 128G ', '/images/mi6.jpg', '小米6 128G 黑色', 1999.00, 200);

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `goods_id` bigint(20) NOT NULL,
  `goods_name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `goods_count` int(10) NULL DEFAULT 0,
  `goods_price` decimal(10, 2) NULL DEFAULT 0.00,
  `status` tinyint(4) NULL DEFAULT 0,
  `create_date` datetime(0) NULL DEFAULT NULL,
  `pay_date` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 311 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for seckill_goods
-- ----------------------------
DROP TABLE IF EXISTS `seckill_goods`;
CREATE TABLE `seckill_goods`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `goods_id` bigint(20) NULL DEFAULT NULL,
  `seckill_price` decimal(10, 2) NULL DEFAULT 0.00,
  `stock_count` int(10) NULL DEFAULT 0,
  `start_date` datetime(0) NULL DEFAULT NULL,
  `end_date` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of seckill_goods
-- ----------------------------
INSERT INTO `seckill_goods` VALUES (1, 1, 10.00, 9, '2019-02-15 12:01:28', '2019-03-14 17:34:36');
INSERT INTO `seckill_goods` VALUES (2, 2, 1.00, 99, '2019-02-20 22:56:30', '2019-04-19 22:56:36');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(11) NOT NULL COMMENT '手机号,id',
  `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `salt` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `head` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `register_date` datetime(0) NULL DEFAULT NULL,
  `last_login_date` datetime(0) NULL DEFAULT NULL,
  `login_count` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (13000000000, 'user0', '5aedf3bb9fa6497f4ade2430cc9cd4a6', 'mysalt', NULL, '2019-01-16 16:00:54', NULL, 1);
