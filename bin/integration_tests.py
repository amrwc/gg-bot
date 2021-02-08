#!/usr/bin/env python3

"""Integration Tests

Run integration tests.

Usage:
  integration_tests.py [-h | --help] [-v | --version]

Options:
  -h, --help     Show this help message.
  -v, --version  Show the script's version.
"""

import docopt

import database
import teardown
import utils

CONFIG = utils.get_config(module_path=__file__)


def main() -> None:
    docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])

    database_container = CONFIG['DATABASE']['database_container']

    database.run_db_container(
        container_name=database_container,
        network=CONFIG['DOCKER']['network']
    )
    database.apply_migrations()

    utils.log('Running integration tests')
    utils.execute_cmd(['./gradlew', 'integrationTest', '--info'])

    teardown.rm_container(teardown.DockerContainer(database_container, rm_volumes=True))


if __name__ == '__main__':
    main()
