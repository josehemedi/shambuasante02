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
-- Table structure for table `factures`
--

DROP TABLE IF EXISTS `factures`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factures` (
  `id_facture` int NOT NULL AUTO_INCREMENT,
  `id_patient` int NOT NULL,
  `id_hopital` int NOT NULL,
  `numero_facture` varchar(50) NOT NULL,
  `date_facture` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `montant_total_ht` decimal(10,2) NOT NULL,
  `tva` decimal(5,2) DEFAULT '16.00',
  `montant_total_ttc` decimal(10,2) NOT NULL,
  `statut_paiement` enum('IMPAYE','PARTIEL','PAYE','ANNULE') DEFAULT 'IMPAYE',
  `id_caissier` int DEFAULT NULL,
  PRIMARY KEY (`id_facture`),
  UNIQUE KEY `numero_facture` (`numero_facture`),
  KEY `id_patient` (`id_patient`),
  KEY `id_hopital` (`id_hopital`),
  KEY `id_caissier` (`id_caissier`),
  CONSTRAINT `factures_ibfk_1` FOREIGN KEY (`id_patient`) REFERENCES `patients` (`id_patient`) ON DELETE CASCADE,
  CONSTRAINT `factures_ibfk_2` FOREIGN KEY (`id_hopital`) REFERENCES `hopitaux` (`id_hopital`) ON DELETE CASCADE,
  CONSTRAINT `factures_ibfk_3` FOREIGN KEY (`id_caissier`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `factures`
--

LOCK TABLES `factures` WRITE;
/*!40000 ALTER TABLE `factures` DISABLE KEYS */;
INSERT INTO `factures` VALUES (3,7,1,'FAC-2026-0001','2026-06-19 12:04:16',0.00,16.00,0.00,'IMPAYE',2),(5,7,1,'FAC-2026-079','2026-06-19 12:04:53',0.00,16.00,0.00,'IMPAYE',2),(7,7,1,'FAC-2026-071','2026-06-19 12:23:51',0.00,16.00,0.00,'IMPAYE',2),(10,7,1,'FAC-2026-0201','2026-06-19 12:34:43',100.00,16.00,116.00,'IMPAYE',2),(12,7,1,'FAC-2026-0221','2026-06-19 12:35:22',100.00,16.00,116.00,'IMPAYE',2);
/*!40000 ALTER TABLE `factures` ENABLE KEYS */;
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
