#!/usr/bin/env python3

"""Integration Tests

Run integration tests.

Usage:
  integration_tests.py [-h | --help] [-v | --version]

Options:
  -h, --help     Show this help message.
  -v, --version  Show the scripts' version.

Envars:
  SPRING_DATASOURCE_URL       Database URL.
  SPRING_DATASOURCE_USERNAME  Database username.
  SPRING_DATASOURCE_PASSWORD  Database password.

  The above are equivalent to setting `spring.datasource.*` in `application.yml`.

Example:
  export SPRING_DATASOURCE_URL='jdbc:postgresql://test-database-container:5432/dbname'
  export SPRING_DATASOURCE_USERNAME='springuser'
  export SPRING_DATASOURCE_PASSWORD='SuperSecret'
  ./bin/integration_tests.py
"""

import docopt

import database
import docker_utils
import utils

CONFIG = utils.get_config(module_path=__file__)
REQUIRED_ENVARS = [
    'SPRING_DATASOURCE_URL',
    'SPRING_DATASOURCE_USERNAME',
    'SPRING_DATASOURCE_PASSWORD',
]


def main() -> None:
    docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])
    utils.verify_envars(REQUIRED_ENVARS, 'Spring', __doc__)

    database_container = CONFIG['DATABASE']['database_test_container']
    network = CONFIG['DOCKER']['network']

    if not docker_utils.item_exists('network', network):
        docker_utils.create_network(network)

    database.start(container=database_container, network=network, migrations=True)

    utils.log('Running integration tests')
    utils.execute_cmd(['./gradlew', 'integrationTest', '--info'])

    docker_utils.rm_container(docker_utils.DockerContainer(database_container, rm_volumes=True))


if __name__ == '__main__':
    main()
