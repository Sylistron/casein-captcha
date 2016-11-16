CREATE TABLE `challenge` (
  `challenge_id` bigint(20) NOT NULL,
  `language` char(2) NOT NULL,
  `active` char(1) NOT NULL default 'Y',
  `created` bigint(20) NOT NULL default '10',
  `created_by` varchar(50) default NULL,
  `last_modified` bigint(20) NOT NULL default '10',
  `last_modified_by` varchar(50) default NULL,
  `question` text NOT NULL,  
  PRIMARY KEY  (`challenge_id`)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `response` (
  `response_id` bigint(20) NOT NULL,
  `active` char(1) NOT NULL default 'Y',
  `answer` text NOT NULL,
  `challenge` bigint(20) NOT NULL,
  `created` bigint(20) NOT NULL default '10',
  `created_by` varchar(50) default NULL,
  `last_modified` bigint(20) NOT NULL default '10',
  `last_modified_by` varchar(50) default NULL,    
  PRIMARY KEY  (`response_id`),
  KEY `ch_idx` (`challenge`)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8;