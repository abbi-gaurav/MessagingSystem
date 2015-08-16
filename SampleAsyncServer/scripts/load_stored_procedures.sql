
\c asl_project_db

CREATE OR REPLACE FUNCTION create_client(client_role role) RETURNS integer AS $_$
DECLARE
	client_id integer; 
BEGIN
	INSERT INTO client(id, client_role) VALUES(DEFAULT, client_role) RETURNING id INTO client_id;
	RETURN client_id;
END $_$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION create_queue() RETURNS integer AS $_$
DECLARE
	queue_id integer; 
BEGIN
	INSERT INTO queue(id) VALUES(DEFAULT) RETURNING id INTO queue_id;
	RETURN queue_id;
END $_$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION delete_queue(queues_id integer[]) RETURNS void AS $_$
BEGIN
	DELETE FROM queue WHERE id = ANY(queues_id);
END $_$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION list_queues() RETURNS SETOF integer AS
'
SELECT id FROM queue;
'
LANGUAGE 'sql';


		  --eg call select post(row(2019,2019,2,10,'dsadsa'),'{10011}');
CREATE OR REPLACE FUNCTION post(m message_type, queues_id integer[]) RETURNS integer AS $_$
DECLARE
	len integer;
	queue integer;
	mess_id integer;
	t timestamp;
BEGIN
	t = now();
	len = array_upper(queues_id,1);
	INSERT INTO message(id,from_id,to_id,context,priority,time,body,message_counter) values(DEFAULT,m.from_id,m.to_id,m.context,m.priority,t,m.body,len) RETURNING id INTO mess_id;
	FOREACH queue IN ARRAY queues_id
	LOOP
		INSERT INTO message_in_queue(mess_id,queue_id,priority,time,mess_from_id,mess_to_id) values(mess_id,queue,m.priority,t,m.from_id,m.to_id);
	END LOOP;
	RETURN mess_id;
END $_$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION broadcast(m message_type) RETURNS integer AS $_$
DECLARE
	m_id integer;
	t timestamp;
BEGIN
	t = now();
	INSERT INTO message(from_id,context,priority,time,body,message_counter) SELECT m.from_id,m.context,m.priority,t,m.body,COUNT(id) FROM queue RETURNING id INTO m_id;
	INSERT INTO message_in_queue(mess_id,queue_id,priority,time,mess_from_id) SELECT m_id,id,m.priority,t,m.from_id FROM queue;
	RETURN m_id;
END $_$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION list_queues_with_message(client integer) RETURNS SETOF integer AS
'
SELECT DISTINCT queue_id FROM message_in_queue WHERE mess_to_id IS NULL OR mess_to_id = $1;
'
LANGUAGE 'sql';


CREATE OR REPLACE FUNCTION is_reader(c_id integer) RETURNS void AS $_$
DECLARE
	c_r role;
BEGIN
	SELECT client_role INTO c_r FROM client WHERE id=c_id;
	IF c_r = 'w' THEN
		RAISE EXCEPTION 'Client is write only and cannot check_message_from, retrieve_message, read (ie read) messages.';
	END IF;
END $_$ LANGUAGE 'plpgsql';


-- mess not deleted afterward.
CREATE OR REPLACE FUNCTION check_message_from(f_id integer, client integer) RETURNS message AS $_$
DECLARE
	res message;
BEGIN
	PERFORM is_reader(client);
	SELECT * INTO res FROM message WHERE id = (SELECT mess_id FROM message_in_queue WHERE mess_from_id = $1 AND (mess_to_id IS NULL OR mess_to_id = $2) ORDER BY priority DESC LIMIT 1);
	RETURN res;
END $_$ LANGUAGE 'plpgsql';


--Not done directly in message table because the indexes are in message_in_queue table.
CREATE OR REPLACE FUNCTION retrieve_message(q_id integer,c_id integer,order_by_time boolean) RETURNS message AS $_$
DECLARE
	m_id integer;
	res message;
BEGIN
	PERFORM is_reader(c_id);
	CASE WHEN order_by_time THEN
		SELECT mess_id INTO m_id FROM message_in_queue WHERE queue_id = q_id AND (mess_to_id IS NULL OR mess_to_id = c_id) ORDER BY time LIMIT 1;
	ELSE
		SELECT mess_id INTO m_id FROM message_in_queue WHERE queue_id = q_id AND (mess_to_id IS NULL OR mess_to_id = c_id) ORDER BY priority DESC LIMIT 1;
	END CASE;
	SELECT * INTO res FROM message WHERE id = m_id;
	DELETE FROM message_in_queue WHERE mess_id=m_id AND queue_id=q_id;
	RETURN res;
END $_$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION read(queue integer, client integer) RETURNS message AS $_$
DECLARE
	res message;
BEGIN
	PERFORM is_reader(client);
	SELECT * INTO res FROM message WHERE id = (SELECT mess_id FROM message_in_queue WHERE queue_id = $1 AND (mess_to_id IS NULL OR mess_to_id = $2) ORDER BY priority DESC LIMIT 1);
	RETURN res;
END $_$ LANGUAGE 'plpgsql';


--------------------------------------MANAGEMENT CONSOLE-------------------------------------

CREATE OR REPLACE FUNCTION mgmt_fetch_queues(start integer, lim integer) RETURNS SETOF record AS
'
SELECT *,0 FROM queue WHERE id NOT IN (SELECT DISTINCT queue_id FROM message_in_queue) UNION SELECT queue_id, COUNT(mess_id) FROM message_in_queue GROUP BY queue_id ORDER BY id LIMIT $2 OFFSET $1;

'
LANGUAGE 'sql';


CREATE OR REPLACE FUNCTION mgmt_fetch_messages(start integer, lim integer, queue integer) RETURNS SETOF record AS
'
SELECT * FROM message_in_queue WHERE queue_id=$3 ORDER BY time LIMIT $2 OFFSET $1;

'
LANGUAGE 'sql';


CREATE OR REPLACE FUNCTION mgmt_fetch_message_details(m integer) RETURNS message AS
'
SELECT * FROM message WHERE id = $1;
'
LANGUAGE 'sql';


--------------------------------------POPULATE DB-------------------------------------

CREATE OR REPLACE FUNCTION empty_db() RETURNS void AS $_$
BEGIN
	DELETE FROM message;
	DELETE FROM queue;
	DELETE FROM client;
	DELETE FROM message_in_queue; -- should be empty already.
END $_$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION create_N_clients(N integer, client_role role) RETURNS void AS $_$
BEGIN
	FOR i IN 1..N LOOP
		PERFORM create_client(client_role);
	END LOOP;
END $_$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION create_N_queues(N integer) RETURNS void AS $_$
BEGIN
	FOR i IN 1..N LOOP
		PERFORM create_queue();
	END LOOP;
END $_$ LANGUAGE 'plpgsql';


		 -- enter m as row(to_id INT, from_id INT, context INT, priority INT, body VARCHAR(2000))
	   	--eg call select post_N_messages(1000, row(2019,2019,2,10,'dsadsa'),'{10011}');
CREATE OR REPLACE FUNCTION post_N_messages(N integer, m message_type, queues_id integer[]) RETURNS void AS $_$
BEGIN
	FOR i IN 1..N LOOP
		PERFORM post(m, queues_id);
	END LOOP;
END $_$ LANGUAGE 'plpgsql';















\q







