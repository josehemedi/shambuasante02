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
-- Table structure for table `consultation`
--

DROP TABLE IF EXISTS `consultation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultation` (
  `id_consultation` int NOT NULL AUTO_INCREMENT,
  `id_hopital` int NOT NULL,
  `id_patient` int NOT NULL,
  `id_medecin` int NOT NULL,
  `id_rdv` int DEFAULT NULL,
  `poids` decimal(5,2) DEFAULT NULL,
  `temperature` decimal(4,1) DEFAULT NULL,
  `tension_arterielle` varchar(10) DEFAULT NULL,
  `frequence_cardiaque` int DEFAULT NULL,
  `glycemie` decimal(5,2) DEFAULT NULL,
  `motif_detaille` text,
  `diagnostic` text,
  `observations` text,
  `date_consultation` datetime DEFAULT CURRENT_TIMESTAMP,
  `statut_consultation` varchar(20) DEFAULT 'EN_ATTENTE',
  PRIMARY KEY (`id_consultation`),
  KEY `id_patient` (`id_patient`),
  KEY `id_rdv` (`id_rdv`),
  KEY `idx_consult_hopital_patient` (`id_hopital`,`id_patient`),
  KEY `idx_consult_medecin_statut` (`id_medecin`,`statut_consultation`),
  CONSTRAINT `fk_consultation_medecin` FOREIGN KEY (`id_medecin`) REFERENCES `medecin` (`id_medecin`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultation`
--

LOCK TABLES `consultation` WRITE;
/*!40000 ALTER TABLE `consultation` DISABLE KEYS */;
INSERT INTO `consultation` VALUES (2,1,5,1,3,75.50,37.2,'12/8',72,0.95,'Le patient présente des maux de tête depuis 3 jours.','Migraine passagère liée au stress.','Repos prescrit et hydratation augmentée.','2026-04-10 16:34:10','TERMINE');
/*!40000 ALTER TABLE `consultation` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-08 18:13:44
