# Database Inspection

There are number of ways to log into a running database instance. This document
describes how to log into and browse a PostgreSQL database.

## pgAdmin

```console
./bin/setup.sh && ./bin/pgadmin.sh
```

1. Visit <http://localhost:5050>.
1. Log in with the details specified in `bin/pgadmin.sh`.
1. Press `Add New Server`.
1. `General` tab
   1. Name: _write anything_
1. `Connection` tab
   1. Host name/address: _hostname from `application.yml`_
   1. Port: `5432`
   1. Maintenance database: _database name from `application.yml`_
   1. Username: _username from `application.yml`_
   1. Password: _password from `application.yml`_
1. Press `Save`. The Postgres server should now be ready for browsing.

Longer explanation of the above steps and configuration can be found [here][1].

[1]: https://www.pgadmin.org/docs/pgadmin4/latest/container_deployment.html
