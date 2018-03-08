CREATE SCHEMA web;

CREATE TABLE web.clicks(
   epoch_hour INT,
   user_id TEXT
);

CREATE INDEX ON web.clicks ( epoch_hour );

CREATE TABLE web.impressions(
   epoch_hour INT,
   user_id TEXT
);

CREATE INDEX ON web.impressions ( epoch_hour );

CREATE VIEW web.measures AS
   SELECT c.epoch_hour, TEXT 'clicks' AS measure_name, COUNT(*) AS measure_value
   FROM web.clicks AS c
   GROUP BY measure_name, c.epoch_hour
   UNION
   SELECT i.epoch_hour, TEXT 'impressions' AS measure_name, COUNT(*) AS measure_value
   FROM web.impressions AS i
   GROUP BY measure_name, i.epoch_hour
   UNION
   SELECT x.epoch_hour, TEXT 'unique_users' AS measure_name, COUNT( DISTINCT( x.user_id ) ) AS measure_value
   FROM (
      SELECT c.epoch_hour, c.user_id
      FROM web.clicks AS c
      UNION
      SELECT i.epoch_hour, i.user_id
      FROM web.impressions AS i
      ) AS x
   GROUP BY x.epoch_hour;
