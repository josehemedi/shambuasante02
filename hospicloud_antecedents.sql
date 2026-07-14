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
-- Table structure for table `antecedents`
--

DROP TABLE IF EXISTS `antecedents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `antecedents` (
  `id_antecedent` int NOT NULL AUTO_INCREMENT,
  `id_patient` int NOT NULL,
  `id_hopital` int NOT NULL,
  `type_antecedent` varchar(100) DEFAULT NULL,
  `libelle` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `est_critique` tinyint(1) DEFAULT '0',
  `date_diagnostic` date DEFAULT NULL,
  `statut` enum('ACTIF','GUERI','CHRONIQUE') DEFAULT 'ACTIF',
  `date_enregistrement` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_antecedent`),
  KEY `idx_ant_patient` (`id_patient`),
  CONSTRAINT `fk_ant_patient` FOREIGN KEY (`id_patient`) REFERENCES `patients` (`id_patient`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `antecedents`
--

LOCK TABLES `antecedents` WRITE;
/*!40000 ALTER TABLE `antecedents` DISABLE KEYS */;
INSERT INTO `antecedents` VALUES (8,5,1,'MEDICAL','Hypertension artérielle','Hypertension depuis 2024',0,'2024-01-15','ACTIF','2026-06-08 00:00:00'),(9,5,1,'MEDICAL','Hypertension artérielle','Hypertension',0,'2024-01-15','ACTIF','2026-06-08 00:00:00'),(10,5,1,' HYPERTENSION','Hypertension artérielle','Hypertension',0,'2024-01-15','ACTIF','2026-06-08 00:00:00'),(11,5,1,'HYPERTENSION','Hypertension artérielle','Hypertension',0,'2024-01-15','ACTIF','2026-06-08 00:00:00'),(12,5,1,'HYPERTENSION','Hypertension artérielle','Hypertension',0,'2024-01-15','ACTIF','2026-06-08 00:00:00');
/*!40000 ALTER TABLE `antecedents` ENABLE KEYS */;
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
