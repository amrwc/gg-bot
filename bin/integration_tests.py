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
import docker_utils
import utils

CONFIG = utils.get_config(module_path=__file__)


def main() -> None:
    docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])

    db_container = CONFIG['DATABASE']['database_test_container']
    network = CONFIG['DOCKER']['network']
    set_envars()

    docker_utils.create_network(network)
    database.start(container=db_container, network=network, migrations=True)

    utils.log('Running integration tests')
    utils.execute_cmd(['./gradlew', 'integrationTest', '--info'])

    docker_utils.rm_container(docker_utils.DockerContainer(db_container, rm_volumes=True))


def set_envars() -> None:
    """Sets required envars."""
    db_port = CONFIG['DATABASE']['port']
    db_name = CONFIG['DOCKER']['main_image']
    db_password = secrets.token_hex(16)

    os.environ['POSTGRES_URL'] = f"jdbc:postgresql://localhost:{db_port}"
    os.environ['POSTGRES_DB'] = db_name
    os.environ['POSTGRES_USER'] = 'postgres'
    os.environ['POSTGRES_PASSWORD'] = db_password

    os.environ['SPRING_DATASOURCE_URL'] = f"jdbc:postgresql://localhost:{db_port}/{db_name}"
    os.environ['SPRING_DATASOURCE_USERNAME'] = 'postgres'
    os.environ['SPRING_DATASOURCE_PASSWORD'] = db_password


if __name__ == '__main__':
    main()
