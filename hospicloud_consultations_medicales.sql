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
-- Table structure for table `consultations_medicales`
--

DROP TABLE IF EXISTS `consultations_medicales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultations_medicales` (
  `id_consultation` bigint NOT NULL AUTO_INCREMENT,
  `id_hopital` int NOT NULL,
  `id_medecin` int NOT NULL,
  `id_patient` int NOT NULL,
  `id_rdv` int DEFAULT NULL,
  `date_consultation` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `motif_visite` varchar(255) DEFAULT NULL,
  `poids` decimal(5,2) DEFAULT NULL,
  `taille` int DEFAULT NULL,
  `tension_arterielle` varchar(20) DEFAULT NULL,
  `temperature` decimal(4,2) DEFAULT NULL,
  `frequence_cardiaque` int DEFAULT NULL,
  `observations` text,
  `diagnostic` text,
  `analyses_prescrites` text COMMENT 'JSON: examens/analyses de la consultation',
  PRIMARY KEY (`id_consultation`),
  KEY `idx_consult_med_tenant` (`id_hopital`,`id_medecin`),
  KEY `idx_consult_med_patient` (`id_patient`),
  KEY `idx_consult_med_rdv` (`id_rdv`),
  CONSTRAINT `fk_consult_med_patient` FOREIGN KEY (`id_patient`) REFERENCES `patients` (`id_patient`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultations_medicales`
--

LOCK TABLES `consultations_medicales` WRITE;
/*!40000 ALTER TABLE `consultations_medicales` DISABLE KEYS */;
INSERT INTO `consultations_medicales` VALUES (6,1,1,5,202,'2026-06-09 05:45:37','Douleurs abdominales chroniques',75.50,175,'12/8',37.20,72,'',''),(7,1,1,5,202,'2026-06-09 05:46:00','Douleurs abdominales chroniques',75.50,175,'12/8',37.20,72,'Le patient va mieux, bonne réactivité au traitement.','Rétablissement complet.'),(8,1,1,5,202,'2026-06-09 05:50:03','Douleurs abdominales chroniques',75.50,175,'12/8',37.20,72,'Bien',NULL),(9,1,1,5,202,'2026-06-09 05:51:28','Douleurs abdominales chroniques',75.50,175,'12/8',37.20,72,'Bien','rigueur'),(10,1,1,5,202,'2026-06-09 05:57:53','Douleurs abdominales chroniques',75.50,175,'12/8',37.20,72,'Bien','rigueur'),(11,1,1,5,202,'2026-06-09 06:09:16','Douleurs abdominales chroniques',78.20,175,'13/9',37.50,80,'',''),(12,1,1,5,202,'2026-06-09 06:09:28','Douleurs abdominales chroniques',46.20,175,'13/9',37.50,80,'',''),(13,1,2,6,15,'2026-06-16 10:13:29',NULL,72.50,NULL,'12/8',36.80,75,NULL,NULL);
/*!40000 ALTER TABLE `consultations_medicales` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-08 18:13:47
