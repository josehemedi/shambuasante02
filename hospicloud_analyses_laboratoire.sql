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
-- Table structure for table `analyses_laboratoire`
--

DROP TABLE IF EXISTS `analyses_laboratoire`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `analyses_laboratoire` (
  `id_analyse` int NOT NULL AUTO_INCREMENT,
  `id_patient` int NOT NULL,
  `id_medecin` int NOT NULL,
  `id_laborantin` int DEFAULT NULL,
  `id_type_analyse` int NOT NULL,
  `id_consultation` int DEFAULT NULL,
  `date_demande` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `date_prelevement` datetime DEFAULT NULL,
  `date_resultat` datetime DEFAULT NULL,
  `statut` enum('EN_ATTENTE','PRELEVE','EN_COURS','TERMINE','ANNULE') DEFAULT 'EN_ATTENTE',
  `urgence` enum('NORMALE','HAUTE','VITALE') DEFAULT 'NORMALE',
  `observations_medecin` text,
  `id_hopital` int NOT NULL,
  PRIMARY KEY (`id_analyse`),
  KEY `id_patient` (`id_patient`),
  KEY `id_medecin` (`id_medecin`),
  KEY `id_laborantin` (`id_laborantin`),
  KEY `id_type_analyse` (`id_type_analyse`),
  KEY `id_consultation` (`id_consultation`),
  KEY `fk_analyse_hopital` (`id_hopital`),
  CONSTRAINT `analyses_laboratoire_ibfk_1` FOREIGN KEY (`id_patient`) REFERENCES `patients` (`id_patient`) ON DELETE CASCADE,
  CONSTRAINT `analyses_laboratoire_ibfk_2` FOREIGN KEY (`id_medecin`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE,
  CONSTRAINT `analyses_laboratoire_ibfk_3` FOREIGN KEY (`id_laborantin`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE SET NULL,
  CONSTRAINT `analyses_laboratoire_ibfk_4` FOREIGN KEY (`id_type_analyse`) REFERENCES `types_analyses` (`id_type_analyse`) ON DELETE CASCADE,
  CONSTRAINT `analyses_laboratoire_ibfk_5` FOREIGN KEY (`id_consultation`) REFERENCES `consultations` (`id_consultation`) ON DELETE SET NULL,
  CONSTRAINT `fk_analyse_hopital` FOREIGN KEY (`id_hopital`) REFERENCES `hopitaux` (`id_hopital`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `analyses_laboratoire`
--

LOCK TABLES `analyses_laboratoire` WRITE;
/*!40000 ALTER TABLE `analyses_laboratoire` DISABLE KEYS */;
/*!40000 ALTER TABLE `analyses_laboratoire` ENABLE KEYS */;
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
