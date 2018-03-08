FROM postgres
ENV POSTGRES_DB analytics
COPY ./src/sql/analytics.sql /docker-entrypoint-initdb.d/
