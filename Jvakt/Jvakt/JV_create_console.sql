CREATE TABLE IF NOT EXISTS Console (
	count   integer, 
	id  	varCHAR(255) not null,
	prio	integer,
	type	CHAR(1), 
	condat 	timestamp,
	status 	varCHAR(255) not null,
	body	varCHAR(255),
	PRIMARY KEY ( id, prio, type, condat ));

/* Comment for the table: */
COMMENT ON TABLE status IS 'Status to operator';
/* Comment for the columns */
COMMENT ON COLUMN Console.count   IS '#Duplicates';
COMMENT ON COLUMN Console.id      IS 'Id';
COMMENT ON COLUMN Console.prio    IS '1=7/24 2=not nigths 3=Office hours 4=Info';
COMMENT ON COLUMN Console.type    IS 'R=Repeated I=Immediate S=Scheduled';
COMMENT ON COLUMN Console.condat  IS 'The time of latest console update';
COMMENT ON COLUMN Console.status  IS 'OKAY or not';
COMMENT ON COLUMN Console.body    IS 'text by the agent';

