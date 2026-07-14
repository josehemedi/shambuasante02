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
-- Table structure for table `patients`
--

DROP TABLE IF EXISTS `patients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patients` (
  `id_patient` int NOT NULL AUTO_INCREMENT,
  `id_hopital` int NOT NULL,
  `code_patient` varchar(20) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `sexe` enum('M','F') NOT NULL,
  `date_naissance` date NOT NULL,
  `groupe_sanguin` enum('A+','A-','B+','B-','AB+','AB-','O+','O-') DEFAULT NULL,
  `adresse` text,
  `telephone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `profession` varchar(100) DEFAULT NULL,
  `est_actif` tinyint(1) DEFAULT '1',
  `date_enregistrement` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `contact_urgence` json DEFAULT NULL,
  `id_societe` int DEFAULT NULL,
  `matricule_employe` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id_patient`),
  UNIQUE KEY `code_patient` (`code_patient`),
  UNIQUE KEY `idx_patient_code` (`code_patient`),
  KEY `idx_patient_nom_prenom` (`nom`,`prenom`),
  KEY `idx_patient_hopital` (`id_hopital`),
  CONSTRAINT `patients_ibfk_1` FOREIGN KEY (`id_hopital`) REFERENCES `hopitaux` (`id_hopital`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patients`
--

LOCK TABLES `patients` WRITE;
/*!40000 ALTER TABLE `patients` DISABLE KEYS */;
INSERT INTO `patients` VALUES (5,1,'PAT-2026-0002','Hemedi','Siku Jose','M','1998-04-04','O+','Boulevard du 30 Juin, Kinshasa','+243810000000','jose.hemedi@exemple.com','Ingénieur Logiciel',1,'2026-04-04 14:23:01',NULL,NULL,NULL),(6,1,'PAT-2026-0003','Hemedi','buana saidi','M','1998-04-04','O+','Boulevard du 30 Juin, Kinshasa','+243810000000','jose.hemedi@exemple.com','Ingénieur Logiciel',1,'2026-04-04 14:23:41',NULL,NULL,NULL),(7,1,'PAT-2026-0004','Dupont','Jean','M','1985-05-15',NULL,'12 rue des Lilas, Paris','+33612345678',NULL,NULL,0,'2026-04-07 13:43:01',NULL,NULL,NULL),(8,1,'PAT-2026-0005','Dupont','Jean','M','1985-05-15',NULL,' avenue Mafuta','+33612345678',NULL,NULL,0,'2026-04-07 13:44:16',NULL,NULL,NULL),(9,1,'PAT-2026-0006','Kabuya','Jean','M','1995-04-12',NULL,'Kinshasa','+243810000000',NULL,NULL,0,'2026-06-08 10:03:07',NULL,NULL,NULL);
/*!40000 ALTER TABLE `patients` ENABLE KEYS */;
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
