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
-- Table structure for table `bons_sortie`
--

DROP TABLE IF EXISTS `bons_sortie`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bons_sortie` (
  `id_bon_sortie` int NOT NULL AUTO_INCREMENT,
  `id_hopital` int NOT NULL,
  `id_patient` int NOT NULL,
  `id_consultation` int DEFAULT NULL,
  `numero_bon` varchar(50) DEFAULT NULL,
  `date_sortie` datetime DEFAULT CURRENT_TIMESTAMP,
  `diagnostic_final` text,
  `etat_sortie` enum('GUERI','AMELIORE','STATIONNAIRE','DECES','TRANSFERE') DEFAULT 'GUERI',
  `recommandations_post_hospitalisation` text,
  `statut_paiement_final` tinyint(1) DEFAULT '0',
  `autorise_par` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_bon_sortie`),
  KEY `fk_sortie_patient` (`id_patient`),
  KEY `idx_sortie_hopital_date` (`id_hopital`,`date_sortie`),
  CONSTRAINT `fk_sortie_hopital` FOREIGN KEY (`id_hopital`) REFERENCES `hopitaux` (`id_hopital`),
  CONSTRAINT `fk_sortie_patient` FOREIGN KEY (`id_patient`) REFERENCES `patients` (`id_patient`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bons_sortie`
--

LOCK TABLES `bons_sortie` WRITE;
/*!40000 ALTER TABLE `bons_sortie` DISABLE KEYS */;
INSERT INTO `bons_sortie` VALUES (2,1,6,2,'BS-2026-0001','2026-06-16 20:41:46','Diagnostic de test : patient en bonne santé','GUERI','Reposez-vous bien',1,'Dr. Jean Expert'),(5,1,6,NULL,'BS-2026-0001','2026-06-16 20:43:47','Appendicite aiguë opérée, évolution favorable.','GUERI','Repos strict pendant 15 jours, éviter le port de charges lourdes.',1,'Dr. Jean Dupont'),(6,1,6,NULL,'BS-2026-0001','2026-06-16 20:50:31','Appendicite aiguë opérée, évolution favorable.','GUERI','Repos strict pendant 15 jours, éviter le port de charges lourdes.',1,'Dr. Jean Dupont'),(7,1,6,NULL,'BS-2026-0001','2026-06-16 20:50:34','Appendicite aiguë opérée, évolution favorable.','GUERI','Repos strict pendant 15 jours, éviter le port de charges lourdes.',1,'Dr. Jean Dupont'),(8,1,6,NULL,'BS-2026-0001','2026-06-17 01:39:15','Appendicite aiguë opérée, évolution favorable.','GUERI','Repos strict pendant 15 jours, éviter le port de charges lourdes.',1,'Dr. Jean Dupont'),(10,1,7,15,NULL,'2026-06-19 14:17:59','Paludisme simple traité','GUERI','Contrôle après 7 jours',1,'Dr KABAMBA');
/*!40000 ALTER TABLE `bons_sortie` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-08 18:13:42
