-- phpMyAdmin SQL Dump
-- version 3.5.6
-- http://www.phpmyadmin.net
--
-- Počítač: localhost
-- Vygenerováno: Pon 12. kvě 2014, 10:58
-- Verze MySQL: 5.1.41-3ubuntu12.6
-- Verze PHP: 5.3.2-1ubuntu4.21

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Databáze: `runstat_hostuju_cz`
--

-- --------------------------------------------------------

--
-- Struktura tabulky `locations`
--

CREATE TABLE IF NOT EXISTS `locations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `run_id` int(11) NOT NULL,
  `run_type` int(11) NOT NULL,
  `date` varchar(50) NOT NULL,
  `steps` int(11) NOT NULL,
  `speed` float NOT NULL,
  `distance` float NOT NULL,
  `lat` double NOT NULL,
  `lng` double NOT NULL,
  `time` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=4328 ;

-- --------------------------------------------------------

--
-- Struktura tabulky `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `firstname` varchar(10) NOT NULL,
  `organization` varchar(20) NOT NULL,
  `username` varchar(16) DEFAULT NULL,
  `password` char(50) DEFAULT NULL,
  `email` varchar(20) NOT NULL,
  `is_active` int(1) NOT NULL,
  `role` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=32 ;

--
-- Vypisuji data pro tabulku `users`
--

INSERT INTO `users` (`id`, `firstname`, `organization`, `username`, `password`, `email`, `is_active`, `role`) VALUES
(31, 'Admin', 'Organizace', 'admin', '21232f297a57a5a743894a0e4a801fc3', 'admin@seznam.cz', 1, 1);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
