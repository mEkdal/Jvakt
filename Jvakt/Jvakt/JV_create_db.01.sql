CREATE DATABASE IF NOT EXISTS Jvakt;
use JVakt;
CREATE TABLE IF NOT EXISTS Status (
	sts  CHAR(1), 
	type	CHAR(1), 
	prio	integer,
	id1  	varCHAR(255) not null,
	id2  	varCHAR(255) not null,
	id3  	varCHAR(255) not null,
	status 	varCHAR(255) not null,
	expsts 	varCHAR(255) not null,
	rptsts 	varCHAR(255) not null,
	rptdat 	DATE,
	rpttim 	TIME,
	chkday	CHAR(10),
	chkfrom	TIME,
	chktom	TIME,
	plugin	CHAR(10),
	errors	integer,	
	acc_err	integer,	
	body	varCHAR(255),
	msg 	CHAR(1),
	console	CHAR(1),
	msgdate	DATE,
	msgtime	TIME,
	info	varCHAR(255));
	
/* Comment for the table: */
COMMENT ON TABLE status IS 'Status of the agents';
/* Comment for the columns */
COMMENT ON COLUMN status.sts     IS 'A=Active I=Inactive D=Dormant';
COMMENT ON COLUMN status.type    IS 'A=Repeated I=Immediate S=Scheduled';
COMMENT ON COLUMN status.prio    IS '1=7/24 2=not nigths 3=Office hours 4=Info';
COMMENT ON COLUMN status.id1     IS 'Key of first signifigance';
COMMENT ON COLUMN status.id2     IS 'Key of second signifigance';
COMMENT ON COLUMN status.id3     IS 'Key of third signifigance';
COMMENT ON COLUMN status.status  IS 'OKAY or not';
COMMENT ON COLUMN status.expsts  IS 'The positive status expected from the agent';
COMMENT ON COLUMN status.rptsts  IS 'The status reported from the agent';
COMMENT ON COLUMN status.rptdat  IS 'The date of the agent report';
COMMENT ON COLUMN status.rpttim  IS 'The time of the agent report';
COMMENT ON COLUMN status.chkday  IS '*ALL *MON *TUE *WEEKDAY...';
COMMENT ON COLUMN status.chkfrom IS 'The start time for check';
COMMENT ON COLUMN status.chktom  IS 'The end time for check';
COMMENT ON COLUMN status.plugin  IS 'The plugin to run when msg is triggered';
COMMENT ON COLUMN status.errors  IS 'Number of error status reported';
COMMENT ON COLUMN status.acc_err IS 'Number of errors accepted before a msg is triggered';
COMMENT ON COLUMN status.body    IS 'Any text delifered by the agent';
COMMENT ON COLUMN status.msg     IS '1=tagged for to be handeled bu the msg routine';
COMMENT ON COLUMN status.console IS '1=tagged for to be shown in theconsole';
COMMENT ON COLUMN status.msgdate IS 'Date the msg routinge was triggered';
COMMENT ON COLUMN status.msgtime IS 'Time the msg routinge was triggered';
COMMENT ON COLUMN status.info    IS 'Description of the checkpoint';


	CREATE TABLE IF NOT EXISTS JV_Console (
	count	integer,	
	date	DATE,
	time	TIME,
	prio	integer,
	body	varCHAR(255);


CREATE TABLE IF NOT EXISTS JV_Messages (
	id	CHAR(10) not null default 'JMS0000000',
	subject	varCHAR(255) not null,
	body	varCHAR(255),
	type	CHAR(1),
	prio	CHAR(1),
	mailed	CHAR(1),
	msgtime	varCHAR(25),
	date	DATE,
	time	TIME,
	client	CHAR(25) not null,
	server	CHAR(25) not null);

CREATE TABLE IF NOT EXISTS JV_Clients (
	client	CHAR(25) not null,
	server	CHAR(25),
	body	varCHAR(255),
	type	CHAR(1),
	date	DATE,
	time	TIME);

CREATE TABLE IF NOT EXISTS JV_Servers (
	server	CHAR(25) not null,
	body	varCHAR(255),
	date	DATE,
	time	TIME);

CREATE TABLE IF NOT EXISTS JV_MsgDesc (
	id	CHAR(10) not null,
	body	varCHAR(255),
	type	CHAR(1),
	prio	CHAR(1),
	mailto	varCHAR(255),
	date	DATE,
	time	TIME);


	
