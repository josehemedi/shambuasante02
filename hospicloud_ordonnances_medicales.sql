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
-- Table structure for table `ordonnances_medicales`
--

DROP TABLE IF EXISTS `ordonnances_medicales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ordonnances_medicales` (
  `id_ordonnance` bigint NOT NULL AUTO_INCREMENT,
  `numero_ordonnance` varchar(50) DEFAULT NULL,
  `id_patient` int NOT NULL,
  `hospital_id` int NOT NULL,
  `id_medecin` int NOT NULL,
  `date_prescription` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `diagnostic` text,
  `contenu_ordonnance` text NOT NULL,
  `observations` text,
  `statut` varchar(20) DEFAULT 'ACTIVE',
  `date_expiration` date DEFAULT NULL,
  PRIMARY KEY (`id_ordonnance`),
  UNIQUE KEY `numero_ordonnance` (`numero_ordonnance`),
  KEY `idx_ordonnance_tenant` (`hospital_id`),
  KEY `idx_ordonnance_patient` (`id_patient`,`hospital_id`),
  KEY `idx_ordonnance_medecin` (`id_medecin`,`hospital_id`),
  CONSTRAINT `fk_ordonnance_medecin` FOREIGN KEY (`id_medecin`) REFERENCES `medecin` (`id_medecin`),
  CONSTRAINT `fk_patient` FOREIGN KEY (`id_patient`) REFERENCES `patients` (`id_patient`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ordonnances_medicales`
--

LOCK TABLES `ordonnances_medicales` WRITE;
/*!40000 ALTER TABLE `ordonnances_medicales` DISABLE KEYS */;
INSERT INTO `ordonnances_medicales` VALUES (2,NULL,5,1,1,'2026-06-09 21:24:01',NULL,'Amoxicilline 500mg, 2 fois par jour pendant 7 jours.',NULL,'RENOUVELEE','2026-12-31'),(3,NULL,5,1,1,'2026-06-09 21:27:44',NULL,'Renouvellement : Amoxicilline 500mg, cure prolongée.',NULL,'RENOUVELEE','2027-06-09'),(4,NULL,6,1,1,'2026-06-09 22:10:57',NULL,'Amoxicilline 500mg - 2 fois par jour pendant 7 jours.',NULL,'ANNULEE','2026-07-09'),(5,NULL,6,1,1,'2026-06-09 22:18:40',NULL,'Amoxicilline 500mg renouvellement - 2 fois par jour pendant 7 jours.',NULL,'ACTIVE','2026-07-09'),(8,NULL,5,1,1,'2026-06-10 11:25:45',NULL,'Paracétamol 500mg 3x/jour',NULL,'ACTIVE','2026-06-20');
/*!40000 ALTER TABLE `ordonnances_medicales` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-08 18:13:45
