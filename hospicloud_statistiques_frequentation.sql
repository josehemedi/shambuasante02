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
-- Table structure for table `statistiques_frequentation`
--

DROP TABLE IF EXISTS `statistiques_frequentation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `statistiques_frequentation` (
  `id_stat` int NOT NULL AUTO_INCREMENT,
  `id_hopital` int NOT NULL,
  `date_stat` date NOT NULL,
  `nombre_consultations` int DEFAULT '0',
  `nombre_hospitalisations` int DEFAULT '0',
  `nombre_nouveaux_patients` int DEFAULT '0',
  `chiffre_affaire_journalier` decimal(15,2) DEFAULT '0.00',
  `id_medecin` int DEFAULT NULL,
  PRIMARY KEY (`id_stat`),
  KEY `fk_stat_medecin` (`id_medecin`),
  KEY `idx_stat_hopital_medecin_date` (`id_hopital`,`id_medecin`,`date_stat`),
  CONSTRAINT `fk_stat_medecin` FOREIGN KEY (`id_medecin`) REFERENCES `medecin` (`id_medecin`) ON DELETE CASCADE,
  CONSTRAINT `statistiques_frequentation_ibfk_1` FOREIGN KEY (`id_hopital`) REFERENCES `hopitaux` (`id_hopital`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `statistiques_frequentation`
--

LOCK TABLES `statistiques_frequentation` WRITE;
/*!40000 ALTER TABLE `statistiques_frequentation` DISABLE KEYS */;
INSERT INTO `statistiques_frequentation` VALUES (1,1,'2026-06-23',5,2,3,150.50,2);
/*!40000 ALTER TABLE `statistiques_frequentation` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-08 18:13:49
