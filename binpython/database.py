#!/usr/bin/env python3

"""Database

Database-related tasks.

Usage:
  database.py [--apply-migrations | --rollback=<c> | --start-db]
              [--attach]
              [--container <n>]
              [-h | --help]
              [--network <n>]
              [--port <p>]
              [-v | --version]

Options:
  --apply-migrations  Apply database migrations. Includes `--start-db`.
  --attach            Attach to the running database container. Note that
                      bailing out will stop the database container.
  --container <n>     Name to use for the database container. Defaults to the
                      container name from the config file.
  -h, --help          Show this help.
  --network <n>       Name of a Docker network to operate within. Defaults to
                      the network name from the config file.
  --port=<p>          Host port at which the database is listening on.
  --rollback=<c>      Rollback count. It specifies the number of change sets to
                      roll back. Includes `--start-db`.
  --start-db          Start the database container.
  -v, --version       Show the scripts' version.

Envars:
  POSTGRES_URL       URL at which the database server can be reached.
  POSTGRES_DB        Name of the default database that is created when the
                     image is first started.
  POSTGRES_USER      Superuser username for PostgreSQL.
  POSTGRES_PASSWORD  Superuser password for PostgreSQL.

  Read more on https://hub.docker.com/_/postgres.

Example:
  export POSTGRES_URL='jdbc:postgresql://localhost:5432'
  export POSTGRES_DB='dbname'
  export POSTGRES_USER='postgres'
  export POSTGRES_PASSWORD='SuperuserPassword'
  ./bin/database.py --apply-migrations
"""

import os
import time

import docopt

import helper.database_helper as database_helper
import util.docker_utils as docker_utils
import util.utils as utils

CONFIG = utils.get_config(module_path=__file__)
REQUIRED_ENVARS = [
    'POSTGRES_URL',
    'POSTGRES_DB',
    'POSTGRES_USER',
    'POSTGRES_PASSWORD',
]


def main() -> None:
    args = docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])
    utils.verify_envars(REQUIRED_ENVARS, 'Postgres', __doc__)

    container_name = args['--container'] if args['--container'] else CONFIG['DATABASE']['database_container']
    network_name = args['--network'] if args['--network'] else CONFIG['DOCKER']['network']

    if args['--rollback']:
        port = args['--port'] if args['--port'] else CONFIG['DATABASE']['port']
        run_db_container(container_name, network_name, port)
        roll_back(args['--rollback'])
    else:
        start(
            container=container_name,
            network=network_name,
            port=args['--port'],
            migrations=args['--apply-migrations'],
            start_db=args['--start-db']
        )

    if args['--attach'] and docker_utils.item_exists('container', container_name):
        utils.execute_cmd(['docker', 'attach', container_name])


def start(
        container: str = None,
        network: str = None,
        port: str = None,
        migrations: bool = False,
        start_db: bool = False
) -> None:
    """Starts the database container.

    Args:
        container (str): Optional; The database container's name. Defaults to the config value.
        network (str): Optional; Name of the network to operate within. Defaults to the config value.
        port (str): Optional; Host port at which the database will be listening on. Defaults to the config value.
        migrations (bool): Optional; Whether to apply the migrations. Includes `start_db`.
        start_db (bool): Optional; Whether to start the database container.
    """
    utils.verify_envars(REQUIRED_ENVARS, 'Postgres', __doc__)

    container_name = container if container else CONFIG['DATABASE']['database_container']
    network_name = network if network else CONFIG['DOCKER']['network']
    port = port if port else CONFIG['DATABASE']['port']

    if migrations or start_db:
        run_db_container(container_name, network_name, port)
        if migrations:
            apply_migrations()


def run_db_container(container_name: str, network: str, port: str) -> None:
    """Runs the database Docker container.

    Args:
        container_name (str): Name to use for the database container.
        network (str): Name of a Docker network to plug the database into.
        port (str): Host port at which the database will be listening on.
    """
    docker_image = CONFIG['DATABASE']['docker_image']

    if docker_utils.item_exists('container', container_name):
        utils.log(f"Container '{container_name}' already exists, not running '{docker_image}' image")
        return
    if not docker_utils.item_exists('network', network):
        utils.raise_error(f"Docker network '{network}' doesn't exist")

    utils.log(f"Running '{docker_image}' container, name: {container_name}")
    utils.execute_cmd([
        'docker',
        'run',
        '--detach',
        '--name',
        container_name,
        '--publish',
        f"{port}:5432",  # <host_port>:<container_port>
        '--network',
        network,
        '--env',
        f"POSTGRES_DB={os.environ.get('POSTGRES_DB')}",
        '--env',
        f"POSTGRES_USER={os.environ.get('POSTGRES_USER')}",
        '--env',
        f"POSTGRES_PASSWORD={os.environ.get('POSTGRES_PASSWORD')}",
        docker_image,
    ])
    time.sleep(3)  # Wait for the database to come up


def apply_migrations() -> None:
    """Applies database migrations."""
    liquibase_cmd = database_helper.fetch_dependencies()
    liquibase_cmd.append('update')

    utils.log('Applying database migrations')
    utils.execute_cmd(liquibase_cmd)


def roll_back(count: str) -> None:
    """Rolls back the given number of database change sets.

    Args:
        count (str): Number of latest change sets to roll back.
    """
    liquibase_cmd = database_helper.fetch_dependencies()
    liquibase_cmd.extend(['rollbackCount', count])

    utils.log('Applying database migrations')
    utils.execute_cmd(liquibase_cmd)


if __name__ == '__main__':
    main()
