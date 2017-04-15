CREATE TABLE IF NOT EXISTS Status (
	state   CHAR(1), 
	id  	varCHAR(255) not null,
	prio	integer,
	type	CHAR(1), 
	status 	varCHAR(255) not null,
	body	varCHAR(255),
	rptdat 	timestamp,
	chkday	CHAR(10),
	chktim	TIME,
	errors	integer,	
	accerr	integer,	
	msg 	varCHAR(1),
	msgdat	timestamp,
	console	varCHAR(1),
	condat	timestamp,
	info	varCHAR(255),
	plugin	varCHAR(255),
	agent	varCHAR(255),
	PRIMARY KEY ( id, prio, type ));
	
/* Comment for the table: */
COMMENT ON TABLE status IS 'Status of the agents';
/* Comment for the columns */
COMMENT ON COLUMN status.state   IS 'A=Active I=Inactive D=Dormant';
COMMENT ON COLUMN status.id     IS 'Descriptive key';
COMMENT ON COLUMN status.prio    IS '1=7/24 2=not nigths 3=Office hours 4=Info';
COMMENT ON COLUMN status.type    IS 'R=Repeated I=Immediate S=Scheduled';
COMMENT ON COLUMN status.status  IS 'OKAY or not';
COMMENT ON COLUMN status.body    IS 'Any text delivered by the agent';
COMMENT ON COLUMN status.rptdat  IS 'The time of the agent report';
COMMENT ON COLUMN status.chkday  IS '*ALL *MON *TUE *WEEKDAY...';
COMMENT ON COLUMN status.chktim  IS 'The end time for check';
COMMENT ON COLUMN status.errors  IS 'Number of error status reported';
COMMENT ON COLUMN status.accerr  IS 'Number of errors accepted before a msg is triggered';
COMMENT ON COLUMN status.msg     IS '1=tagged for to be handled bu the msg routine';
COMMENT ON COLUMN status.msgdat  IS 'Time the msg routine was triggered';
COMMENT ON COLUMN status.console IS 'C=tagged for to be shown/removed on/from the console';
COMMENT ON COLUMN status.condat  IS 'Time the console was updated';
COMMENT ON COLUMN status.info    IS 'Description of the checkpoint';
COMMENT ON COLUMN status.plugin  IS 'The plugin to run when msg is triggered';
COMMENT ON COLUMN status.agent   IS 'Description of the reporting agent';

