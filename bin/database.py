#!/usr/bin/env python3

"""Database

Database-related tasks.

Usage:
  database.py [--apply-migrations | --start-db]
              [--attach]
              [--container <n>]
              [-h | --help]
              [--network <n>]
              [-v | --version]

Options:
  --apply-migrations  Apply database migrations; includes `--start-db`.
  --attach            Attach to the running database container. Note that bailing out will stop the database container.
  --container <n>     Name to use for the database container. Defaults to the
                      container name from the config file.
  -h, --help          Show this help.
  --network <n>       Name of a Docker network to operate within. Defaults to
                      the network name from the config file.
  --start-db          Start the database container.
  -v, --version       Show the scripts' version.
"""

import hashlib
import os
import pathlib
import shutil
import tarfile
import urllib.request

import docopt
import time

import docker_utils
import utils

CONFIG = utils.get_config(module_path=__file__)

DRIVER_URL = 'https://jdbc.postgresql.org/download/postgresql-42.2.18.jar'
SHA256_DRIVER = '0c891979f1eb2fe44432da114d09760b5063dad9e669ac0ac6b0b6bfb91bb3ba'
DRIVER_PATH = os.path.join('tmp', 'db-driver', 'postgresql.jar')

LIQUIBASE_URL = 'https://github.com/liquibase/liquibase/releases/download/v4.2.2/liquibase-4.2.2.tar.gz'
SHA256_LIQUIBASE_ARCHIVE = '807ef4b514d01fc62f7aaf4150a8435c90ccb5986f3272d3cfd1bd26c2cf7b4c'
SHA256_LIQUIBASE_JAR = 'c092425c70b76bb28b6c260c1db8ee4845b7c4888f137937869393abca03af11'
LIQUIBASE_DIR = os.path.join('tmp', 'liquibase')
LIQUIBASE_PATH = os.path.join(LIQUIBASE_DIR, 'liquibase.jar')
LIQUIBASE_ARCHIVE = os.path.join(LIQUIBASE_DIR, 'liquibase.tar.gz')
LIQUIBASE_PROPERTIES_PATH = os.path.join('src', 'main', 'resources', 'liquibase.properties')


def main() -> None:
    args = docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])

    container_name = args['--container'] if args['--container'] else CONFIG['DATABASE']['database_container']

    start(
        container=container_name,
        network=args['--network'],
        migrations=args['--apply-migrations'],
        start_db=args['--start-db']
    )

    if args['--attach'] and docker_utils.item_exists('container', container_name):
        utils.execute_cmd(['docker', 'attach', container_name])


def start(container: str = None, network: str = None, migrations: bool = False, start_db: bool = False) -> None:
    """Starts the database container.

    Args:
        container (str): Optional; The database container's name. Defaults to the config value.
        network (str): Optional; Name of the network to operate within. Defaults to the config value.
        migrations (bool): Optional; Whether to apply the migrations. Includes `start_db`.
        start_db (bool): Optional; Whether to start the database container.
    """
    container_name = container if container else CONFIG['DATABASE']['database_container']
    network_name = network if network else CONFIG['DOCKER']['network']

    if migrations or start_db:
        run_db_container(container_name, network_name)
        if migrations:
            time.sleep(3)  # Wait for the database to come up
            apply_migrations()


def run_db_container(container_name: str, network: str) -> None:
    """Runs the database Docker container.

    Args:
        container_name (str): Name to use for the database container.
        network (str): Name of a Docker network to plug the database into.
    """
    docker_image = CONFIG['DATABASE']['docker_image']
    port = CONFIG['DATABASE']['port']

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
        f"{port}:{port}",
        '--network',
        network,
        '--env-file',
        os.path.join('docker', 'postgres-envars.list'),
        docker_image,
    ])


def apply_migrations() -> None:
    """Applies database migrations."""
    if not os.path.isfile(DRIVER_PATH):
        utils.log(f"Downloading database driver to '{DRIVER_PATH}'")
        pathlib.Path(os.path.dirname(DRIVER_PATH)).mkdir(parents=True, exist_ok=True)
        urllib.request.urlretrieve(DRIVER_URL, DRIVER_PATH)
    check_sha256(DRIVER_PATH, SHA256_DRIVER)

    liquibase_cmd = ['liquibase']
    if shutil.which('liquibase') is None:
        if not os.path.isfile(LIQUIBASE_PATH):
            utils.log(f"Downloading and extracting Liquibase to '{LIQUIBASE_PATH}'")
            pathlib.Path(os.path.dirname(LIQUIBASE_PATH)).mkdir(parents=True, exist_ok=True)
            urllib.request.urlretrieve(LIQUIBASE_URL, LIQUIBASE_ARCHIVE)
            check_sha256(LIQUIBASE_ARCHIVE, SHA256_LIQUIBASE_ARCHIVE)
            with tarfile.open(LIQUIBASE_ARCHIVE) as liquibase_archive:
                jar_reader = liquibase_archive.extractfile('liquibase.jar')
                with open(LIQUIBASE_PATH, 'wb') as jar:
                    jar.write(jar_reader.read())
        check_sha256(LIQUIBASE_PATH, SHA256_LIQUIBASE_JAR)
        liquibase_cmd = ['java', '-jar', LIQUIBASE_PATH]

    liquibase_cmd.extend([
        f"--classpath={DRIVER_PATH}",
        f"--defaultsFile={LIQUIBASE_PROPERTIES_PATH}",
        'update',
    ])

    utils.log('Applying database migrations')
    utils.execute_cmd(liquibase_cmd)


def check_sha256(file_path: str, sha256_hash: str) -> None:
    """Checks whether SHA256 digest hash of the given file matches the given hash.

    Args:
        file_path (str): Path to the file to be checked.
        sha256_hash (str): Hash to compare against.
    """
    utils.log(f"Checking SHA256 digest of {file_path}")
    with open(file_path, 'rb') as file:
        file_bytes = file.read()
        driver_digest = hashlib.sha256(file_bytes).hexdigest()
        if sha256_hash != driver_digest:
            utils.raise_error(f"SHA256 checksum of '{file_path}' doesn't match '{sha256_hash}'")


if __name__ == '__main__':
    main()
