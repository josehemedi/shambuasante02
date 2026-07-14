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
-- Table structure for table `horaire_travaille`
--

DROP TABLE IF EXISTS `horaire_travaille`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `horaire_travaille` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hopital_id` int NOT NULL,
  `medecin_id` int NOT NULL,
  `jour_semaine` varchar(10) NOT NULL,
  `heure_debut` time NOT NULL,
  `heure_fin` time NOT NULL,
  `pas_consultation` int NOT NULL,
  `type_autorise` varchar(20) DEFAULT 'LES_DEUX',
  PRIMARY KEY (`id`),
  KEY `fk_horaire_hopital` (`hopital_id`),
  KEY `fk_horaire_medecin` (`medecin_id`),
  CONSTRAINT `fk_horaire_hopital` FOREIGN KEY (`hopital_id`) REFERENCES `hopitaux` (`id_hopital`),
  CONSTRAINT `fk_horaire_medecin` FOREIGN KEY (`medecin_id`) REFERENCES `medecin` (`id_medecin`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `horaire_travaille`
--

LOCK TABLES `horaire_travaille` WRITE;
/*!40000 ALTER TABLE `horaire_travaille` DISABLE KEYS */;
INSERT INTO `horaire_travaille` VALUES (1,1,1,'Lundi','08:00:00','12:00:00',15,'LES_DEUX'),(2,1,1,'Lundi','08:00:00','12:00:00',15,'LES_DEUX'),(3,1,1,'Lundi','08:00:00','12:00:00',30,'LES_DEUX'),(6,1,1,'MERCREDI','08:00:00','12:00:00',0,'LES_DEUX');
/*!40000 ALTER TABLE `horaire_travaille` ENABLE KEYS */;
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
