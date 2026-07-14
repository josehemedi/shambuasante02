-- Réclamations support SaaS — remontées par les hôpitaux (multi-tenant)

DROP TABLE IF EXISTS `support_tickets`;
CREATE TABLE `support_tickets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hopital_id` int NOT NULL,
  `created_by_user_id` int DEFAULT NULL,
  `created_by_email` varchar(150) DEFAULT NULL,
  `created_by_role` varchar(50) DEFAULT NULL,
  `subject` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `module` varchar(100) DEFAULT NULL,
  `priority` enum('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL DEFAULT 'MEDIUM',
  `status` enum('OPEN','IN_PROGRESS','RESOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
  `request_id` varchar(100) DEFAULT NULL,
  `assigned_to` varchar(150) DEFAULT NULL,
  `resolution_notes` text,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_support_tickets_hopital_id` (`hopital_id`),
  KEY `idx_support_tickets_status` (`status`),
  KEY `idx_support_tickets_module` (`module`),
  KEY `idx_support_tickets_request_id` (`request_id`),
  KEY `idx_support_tickets_created_at` (`created_at`),
  CONSTRAINT `support_tickets_ibfk_1` FOREIGN KEY (`hopital_id`) REFERENCES `hopitaux` (`id_hopital`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
