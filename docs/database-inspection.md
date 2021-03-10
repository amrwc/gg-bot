# Database Inspection

There are number of ways to log into a running database instance. This document
describes how to log into and browse a PostgreSQL database.

## pgAdmin

Define login details and launch pgAdmin:

```console
export PG_USERNAME='user@domain.com'
export PG_PASSWORD='PgadminUserPassword'
./bin/pgadmin.py --detach
```

### Connect to the database

1. Visit <http://localhost:5050>.
1. Log in with the login details defined above.
1. Press `Add New Server`.
1. `General` tab
   1. Name: _<name_of_the_item>_
1. `Connection` tab
   1. Host name/address: _\<hostname\>, e.g. localhost, or Docker database
      container name_
   1. Port: usually `5432`
   1. Username: _<superuser_username>_
   1. Password: _<superuser_password>_
1. Press `Save`. The Postgres server should now be ready for browsing.

Longer explanation of the above steps and configuration can be found in the
[pgAdmin documentation][pgadmin_docs].

[pgadmin_docs]: https://www.pgadmin.org/docs/pgadmin4/latest/container_deployment.html
