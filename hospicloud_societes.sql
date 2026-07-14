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
-- Table structure for table `societes`
--

DROP TABLE IF EXISTS `societes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `societes` (
  `id_societe` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nom_societe` varchar(150) NOT NULL,
  `adresse_facturation` text,
  `telephone_contact` varchar(20) DEFAULT NULL,
  `taux_couverture` int DEFAULT NULL,
  `email_contact` varchar(100) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `hospital_id` int NOT NULL,
  PRIMARY KEY (`id_societe`),
  UNIQUE KEY `id_societe` (`id_societe`),
  KEY `fk_societe_hospital` (`hospital_id`),
  KEY `idx_societe_nom_hopital` (`nom_societe`,`hospital_id`),
  CONSTRAINT `fk_societe_hospital` FOREIGN KEY (`hospital_id`) REFERENCES `hopitaux` (`id_hopital`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `societes`
--

LOCK TABLES `societes` WRITE;
/*!40000 ALTER TABLE `societes` DISABLE KEYS */;
INSERT INTO `societes` VALUES (4,'MUSA','C/Gombe, Kinshasa','+243810000001',80,'contact@musa.cd','2026-04-08 12:55:15',1),(5,'CNSS','C/Gombe, Kinshasa','+243810000002',100,'info@cnss.cd','2026-04-08 12:55:15',1),(6,'SOCIETE GENERALE','C/Limete, Kinshasa','+243810000003',70,'admin@socgen.cd','2026-04-08 12:55:15',1),(7,'MUSA','C/Gombe, Kinshasa','+243810000001',1,'contact@musa.cd','2026-04-08 13:06:06',1),(8,'MUSA','C/Gombe, Kinshasa','+243810000001',90,'contact@musa.cd','2026-04-08 13:06:29',1),(9,'MUSA','C/Gombe, Kinshasa','+243810000001',100,'contact@musa.cd','2026-04-08 13:15:49',1),(11,'SONAS','Kinshasa Gombe','+243810000000',80,'contact@sonas.cd','2026-06-08 14:02:16',1),(12,'SONASZ','Kinshasa Gombe','+243810000000',80,'contact@sonas.cd','2026-06-08 14:19:09',1);
/*!40000 ALTER TABLE `societes` ENABLE KEYS */;
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
