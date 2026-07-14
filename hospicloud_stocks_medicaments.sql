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
-- Table structure for table `stocks_medicaments`
--

DROP TABLE IF EXISTS `stocks_medicaments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stocks_medicaments` (
  `id_stock` int NOT NULL AUTO_INCREMENT,
  `id_hopital` int NOT NULL,
  `id_medicament` int NOT NULL,
  `quantite_disponible` int DEFAULT '0',
  `seuil_alerte` int DEFAULT '10',
  `date_peremption` date DEFAULT NULL,
  `emplacement_rayon` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id_stock`),
  KEY `id_hopital` (`id_hopital`),
  KEY `id_medicament` (`id_medicament`),
  CONSTRAINT `stocks_medicaments_ibfk_1` FOREIGN KEY (`id_hopital`) REFERENCES `hopitaux` (`id_hopital`) ON DELETE CASCADE,
  CONSTRAINT `stocks_medicaments_ibfk_2` FOREIGN KEY (`id_medicament`) REFERENCES `medicaments` (`id_medicament`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stocks_medicaments`
--

LOCK TABLES `stocks_medicaments` WRITE;
/*!40000 ALTER TABLE `stocks_medicaments` DISABLE KEYS */;
INSERT INTO `stocks_medicaments` VALUES (1,1,1,500,10,'2027-12-31',NULL);
/*!40000 ALTER TABLE `stocks_medicaments` ENABLE KEYS */;
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
