-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: hospicloud
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `utilisateurs`
-- Colonne `role` : rôles SaaS alignés sur hospicloud.model.Role
-- SUPER_ADMIN | TENANT_ADMIN | MEDECIN | RECEPTION | PATIENT | LABORANTIN | CAISSIER | USER
-- Anciens libellés migrés : ADMIN/HOSPITAL_ADMIN -> TENANT_ADMIN, DOCTOR -> MEDECIN,
-- RECEPTIONNISTE/RECEPTIONIST -> RECEPTION, LAB_TECH/LABORATOIRE -> LABORANTIN, CASHIER -> CAISSIER
--

DROP TABLE IF EXISTS `utilisateurs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilisateurs` (
  `id_utilisateur` int NOT NULL AUTO_INCREMENT,
  `id_hopital` int DEFAULT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `role` varchar(50) NOT NULL,
  `est_actif` tinyint(1) DEFAULT '1',
  `date_creation` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `date_modification` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `id_medecin` int DEFAULT NULL,
  `id_patient` bigint DEFAULT NULL,
  PRIMARY KEY (`id_utilisateur`),
  UNIQUE KEY `email` (`email`),
  KEY `id_hopital` (`id_hopital`),
  CONSTRAINT `utilisateurs_ibfk_1` FOREIGN KEY (`id_hopital`) REFERENCES `hopitaux` (`id_hopital`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utilisateurs`
--

LOCK TABLES `utilisateurs` WRITE;
/*!40000 ALTER TABLE `utilisateurs` DISABLE KEYS */;
INSERT INTO `utilisateurs` VALUES (1,1,'Jose','Siku','siku.jose@hospicloud.cd','$2a$12$EXAMPLE_HASHED_PWD',NULL,'RECEPTION',1,'2026-04-04 11:20:35','2026-07-08 15:18:45',NULL,NULL),(2,1,'Mwemba','Jean','jean.medecin@hospicloud.cd','$2a$12$EXAMPLE_HASHED_PWD',NULL,'RECEPTION',1,'2026-04-04 11:20:35','2026-07-08 15:18:45',NULL,NULL),(3,NULL,'Okonkwo','Adaeze','adaeze@shambua.cloud','$2a$10$9kpfWmL.GtgpmvE2Y58eCuQC/4CeUlTKLoSR/fkvs7BijNBGeksry',NULL,'SUPER_ADMIN',1,'2026-07-08 12:34:47','2026-07-08 16:05:56',NULL,NULL),(4,1,'Mensah','Kwame','kwame.mensah@shambua.health','$2a$10$9kpfWmL.GtgpmvE2Y58eCuQC/4CeUlTKLoSR/fkvs7BijNBGeksry',NULL,'TENANT_ADMIN',1,'2026-07-08 12:34:47','2026-07-08 16:05:56',NULL,NULL),(5,1,'Achebe','Ngozi','ngozi.achebe@shambua.health','$2a$10$9kpfWmL.GtgpmvE2Y58eCuQC/4CeUlTKLoSR/fkvs7BijNBGeksry',NULL,'MEDECIN',0,'2026-07-08 12:34:47','2026-07-08 16:05:56',1,NULL),(6,1,'Diallo','Amara','amara.diallo@gmail.com','$2a$10$9kpfWmL.GtgpmvE2Y58eCuQC/4CeUlTKLoSR/fkvs7BijNBGeksry',NULL,'PATIENT',1,'2026-07-08 12:34:47','2026-07-08 16:05:56',NULL,1),(7,1,'Cisse','Ibrahim','ibrahim.cisse@shambua.health','$2a$10$9kpfWmL.GtgpmvE2Y58eCuQC/4CeUlTKLoSR/fkvs7BijNBGeksry',NULL,'LABORANTIN',1,'2026-07-08 12:34:47','2026-07-08 16:05:56',NULL,NULL),(8,1,'Ndiaye','Fatou','fatou.ndiaye@shambua.health','$2a$10$9kpfWmL.GtgpmvE2Y58eCuQC/4CeUlTKLoSR/fkvs7BijNBGeksry',NULL,'RECEPTION',1,'2026-07-08 12:34:47','2026-07-08 16:05:56',NULL,NULL),(12,1,'Kouassi','Marie','marie.kouassi@shambua.health','$2a$10$9kpfWmL.GtgpmvE2Y58eCuQC/4CeUlTKLoSR/fkvs7BijNBGeksry',NULL,'CAISSIER',1,'2026-07-08 12:34:47','2026-07-08 16:05:56',NULL,NULL),(9,1,'emedi','siku','emedisiku@gmail.com','$2a$10$4GQUZ4KYX3OHcvjzQKw6XuIVKDg6TgzK4Jw5H27hEMVtwpSDcbqPS','0850377919','RECEPTION',1,'2026-07-08 15:05:58','2026-07-08 15:05:58',NULL,NULL),(10,1,'emedie','sikueeeee','emedeisiku@gmail.com','$2a$10$ExKGlQml.EMmMdxfwQV5wOwLjfbef64TsuCe/ntUa8GaVhaxugXrm','0850377919','RECEPTION',1,'2026-07-08 15:06:51','2026-07-08 15:06:50',NULL,NULL),(11,1,'emedie','sikueeeee','ddddd@gmail.com','$2a$10$ww3/daWF1wFmDK0wmTYrAesTHsYNYKVEphMVFsWKCWuk42nT5W7zm','ddddddd','MEDECIN',1,'2026-07-08 15:13:01','2026-07-08 15:53:37',NULL,NULL);
/*!40000 ALTER TABLE `utilisateurs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Migration des anciens libellés de rôles (idempotent)
--
-- UPDATE utilisateurs SET role = 'RECEPTION'   WHERE UPPER(role) IN ('RECEPTIONNISTE', 'RECEPTIONIST');
-- UPDATE utilisateurs SET role = 'MEDECIN'      WHERE UPPER(role) = 'DOCTOR';
-- UPDATE utilisateurs SET role = 'LABORANTIN'   WHERE UPPER(role) IN ('LAB_TECH', 'LABORATOIRE');
-- UPDATE utilisateurs SET role = 'TENANT_ADMIN' WHERE UPPER(role) IN ('HOSPITAL_ADMIN', 'ADMIN');
-- UPDATE utilisateurs SET role = 'CAISSIER'     WHERE UPPER(role) IN ('CASHIER');

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-08 18:13:44
