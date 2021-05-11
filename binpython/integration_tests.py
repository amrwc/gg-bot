#!/usr/bin/env python3

"""Integration Tests

Run integration tests.

Usage:
  integration_tests.py [-h | --help] [-v | --version]

Options:
  -h, --help     Show this help message.
  -v, --version  Show the scripts' version.
"""

import os
import secrets

import docopt

import database
import util.docker_utils as docker_utils
import util.utils as utils

CONFIG = utils.get_config(module_path=__file__)


def main() -> None:
    docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])

    db_container = CONFIG['DATABASE']['database_test_container']
    network = CONFIG['DOCKER']['test_network']
    db_port = CONFIG['DATABASE']['test_port']
    set_envars(db_port)

    docker_utils.create_network(network)
    database.start(container=db_container, network=network, port=db_port, migrations=True)

    utils.log('Running integration tests')
    completed_process = utils.execute_cmd(['./gradlew', 'integrationTest', '--info'], pipe_stderr=True)

    docker_utils.rm_container(docker_utils.DockerContainer(db_container, rm_volumes=True))
    docker_utils.rm_network(network)

    if completed_process.stderr:
        utils.raise_error(completed_process.stderr.decode('utf8'))


def set_envars(db_port: str) -> None:
    """Sets required envars.

    Args:
        db_port (str): Host port at which the database will be listening on.
    """
    db_name = CONFIG['DOCKER']['main_image']
    postgres_url = f"jdbc:postgresql://localhost:{db_port}"

    os.environ['POSTGRES_URL'] = postgres_url
    os.environ['POSTGRES_DB'] = db_name
    os.environ['POSTGRES_USER'] = 'postgres'
    os.environ['POSTGRES_PASSWORD'] = secrets.token_hex(16)

    os.environ['SPRING_DATASOURCE_URL'] = f"{postgres_url}/{db_name}"
    os.environ['SPRING_DATASOURCE_USERNAME'] = 'spring_user'
    os.environ['SPRING_DATASOURCE_PASSWORD'] = 'SpringUserPassword'


if __name__ == '__main__':
    main()
