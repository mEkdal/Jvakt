CREATE DATABASE IF NOT EXISTS Jvakt;
use JVakt;
CREATE TABLE IF NOT EXISTS Status (
	state   CHAR(1), 
	id1  	varCHAR(255) not null,
	id2  	varCHAR(255) not null,
	id3  	varCHAR(255) not null,
	prio	integer,
	type	CHAR(1), 
	status 	varCHAR(255) not null,
	rptdat 	DATE,
	rpttim 	TIME,
	chkday	CHAR(10),
	chkfr	TIME,
	chkto	TIME,
	plugin	CHAR(10),
	errors	integer,	
	accerr	integer,	
	body	varCHAR(255),
	msg 	CHAR(1),
	console	CHAR(1),
	msgdat	DATE,
	msgtim	TIME,
	info	varCHAR(255),
	PRIMARY KEY ( id1, id2, id3, prio, type ));
	
/* Comment for the table: */
COMMENT ON TABLE status IS 'Status of the agents';
/* Comment for the columns */
COMMENT ON COLUMN status.state   IS 'A=Active I=Inactive D=Dormant';
COMMENT ON COLUMN status.id1     IS 'Key of first signifigance';
COMMENT ON COLUMN status.id2     IS 'Key of second signifigance';
COMMENT ON COLUMN status.id3     IS 'Key of third signifigance';
COMMENT ON COLUMN status.prio    IS '1=7/24 2=not nigths 3=Office hours 4=Info';
COMMENT ON COLUMN status.type    IS 'R=Repeated I=Immediate S=Scheduled';
COMMENT ON COLUMN status.status  IS 'OKAY or not';
COMMENT ON COLUMN status.rptdat  IS 'The date of the agent report';
COMMENT ON COLUMN status.rpttim  IS 'The time of the agent report';
COMMENT ON COLUMN status.chkday  IS '*ALL *MON *TUE *WEEKDAY...';
COMMENT ON COLUMN status.chkfr   IS 'The start time for check';
COMMENT ON COLUMN status.chkto   IS 'The end time for check';
COMMENT ON COLUMN status.plugin  IS 'The plugin to run when msg is triggered';
COMMENT ON COLUMN status.errors  IS 'Number of error status reported';
COMMENT ON COLUMN status.accerr  IS 'Number of errors accepted before a msg is triggered';
COMMENT ON COLUMN status.body    IS 'Any text delivered by the agent';
COMMENT ON COLUMN status.msg     IS '1=tagged for to be handeled bu the msg routine';
COMMENT ON COLUMN status.console IS '1=tagged for to be shown/removed on/from the console';
COMMENT ON COLUMN status.msgdat  IS 'Date the msg routinge was triggered';
COMMENT ON COLUMN status.msgtim  IS 'Time the msg routinge was triggered';
COMMENT ON COLUMN status.info    IS 'Description of the checkpoint';


CREATE TABLE IF NOT EXISTS Console (
	count   integer, 
	id1  	varCHAR(255) not null,
	id2  	varCHAR(255) not null,
	id3  	varCHAR(255) not null,
	prio	integer,
	type	CHAR(1), 
	condat 	DATE,
	contim 	TIME,
	status 	varCHAR(255) not null,
	body	varCHAR(255),
	PRIMARY KEY ( id1, id2, id3, prio, type, condat, contim ));

/* Comment for the table: */
COMMENT ON TABLE status IS 'Status to operator';
/* Comment for the columns */
COMMENT ON COLUMN Console.count   IS '#Duplicates';
COMMENT ON COLUMN Console.id1     IS 'Key of first signifigance';
COMMENT ON COLUMN Console.id2     IS 'Key of second signifigance';
COMMENT ON COLUMN Console.id3     IS 'Key of third signifigance';
COMMENT ON COLUMN Console.prio    IS '1=7/24 2=not nigths 3=Office hours 4=Info';
COMMENT ON COLUMN Console.type    IS 'R=Repeated I=Immediate S=Scheduled';
COMMENT ON COLUMN Console.condat  IS 'The date of latest console update';
COMMENT ON COLUMN Console.contim  IS 'The time of latest console update';
COMMENT ON COLUMN Console.status  IS 'OKAY or not';
COMMENT ON COLUMN Console.body    IS 'text by the agent';

