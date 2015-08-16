
DROP DATABASE IF EXISTS asl_project_db;

CREATE DATABASE asl_project_db;

\c asl_project_db

CREATE TYPE role AS ENUM ('r', 'w', 'rw');

CREATE TYPE message_type AS (to_id INT, from_id INT, context INT, priority INT, body VARCHAR(2000));

CREATE TABLE IF NOT EXISTS client(	--primary key is NOT NULL and UNIQUE
	id serial PRIMARY KEY,		--serial is autoincrementing 4byte int, (reference it with int)
	client_role role NOT NULL
);

CREATE TABLE IF NOT EXISTS queue(
	id serial PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS message(
	id serial PRIMARY KEY,
	from_id INT REFERENCES Client ON UPDATE CASCADE ON DELETE SET NULL,
	to_id INT REFERENCES Client ON UPDATE CASCADE ON DELETE SET NULL,	--to_id is null in case of a broadcast
	context INT,		-- null in case of one way messages
	priority INT CHECK (priority >=1 and priority <=10) NOT NULL,		--10 highest, 1 lowest
	time timestamp NOT NULL,
	body varchar(2000),
	message_counter INT NOT NULL
);

CREATE TABLE IF NOT EXISTS message_in_queue(
	mess_id INT REFERENCES message ON UPDATE CASCADE ON DELETE CASCADE,
	queue_id INT REFERENCES queue ON UPDATE CASCADE ON DELETE CASCADE,		--restrict or cascade?
	priority INT CHECK (priority >=1 and priority <=10) NOT NULL,			--10 highest, 1 lowest
	time timestamp NOT NULL,
	mess_from_id INT REFERENCES Client ON UPDATE CASCADE ON DELETE SET NULL,
	mess_to_id INT REFERENCES Client ON UPDATE CASCADE ON DELETE SET NULL,	--to_id is null in case of a broadcast
	PRIMARY KEY (mess_id, queue_id)
);

CREATE INDEX ON message_in_queue (queue_id, mess_to_id, priority);
CREATE INDEX ON message_in_queue (queue_id, mess_to_id, time);
CREATE INDEX ON message_in_queue (mess_from_id, mess_to_id, priority);
CREATE INDEX ON message_in_queue (mess_id, queue_id);

CREATE OR REPLACE FUNCTION update_message_record() RETURNS TRIGGER AS $_$
DECLARE
	counter integer; 
BEGIN
	SELECT message_counter INTO counter FROM message WHERE id=OLD.mess_id;
	IF counter > 1 THEN
		UPDATE message SET message_counter=counter-1 WHERE id=OLD.mess_id;
	ELSE
		DELETE FROM message WHERE id=OLD.mess_id;
	END IF;
	RETURN OLD;		--must return a non null value. postgresql doc: "A useful idiom in delete triggers might be to return OLD."
END $_$ LANGUAGE 'plpgsql';

CREATE TRIGGER message_retrieved AFTER DELETE ON message_in_queue FOR EACH ROW
EXECUTE PROCEDURE update_message_record();	--right now PostgreSQL only allows execution of user defined func for the triggered action

CREATE OR REPLACE FUNCTION is_writer() RETURNS TRIGGER AS $_$
DECLARE
	c_r role;
BEGIN
	SELECT client_role INTO c_r FROM client WHERE id=NEW.from_id;
	IF c_r = 'r' THEN
		RAISE EXCEPTION 'Client is read only and cannot post (ie write) messages.';
	END IF;
	RETURN NEW;
END $_$ LANGUAGE 'plpgsql';

--trigger not on message_in_queue because otherwise posting to multiple queues would launch it more times than necessary.
CREATE TRIGGER check_is_reader BEFORE INSERT ON message FOR EACH ROW
EXECUTE PROCEDURE is_writer();


\q

