#!/usr/bin/env python3

"""API Tests

Tests for the Spring Boot application.

Usage:
  api_tests.py [-h | --version] [-v | --version]

Options:
  -h, --help     Show this help.
  -v, --version  Show the version.
"""

import docopt
import requests

import util.utils as utils

CONFIG = utils.get_config(module_path=__file__)

URL = 'http://localhost:8080/actuator/info'


def main() -> None:
    docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])

    utils.log('Running API tests')
    response = requests.get(URL)
    if 200 != response.status_code:
        container = CONFIG['DOCKER']['main_image']
        utils.log(f"Logs from '{container}':")
        utils.execute_cmd(['docker', 'exec', container, 'tail', '-n', '500', f"/home/project/log/{container}.log"])
        utils.raise_error(
            f"::error::Expected 200 response code but received {response.status_code} from '{URL}', see logs above"
        )


if __name__ == '__main__':
    main()
