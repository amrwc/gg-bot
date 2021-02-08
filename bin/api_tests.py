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
import sys

import utils

CONFIG = utils.get_config(module_path=__file__)

URL = 'http://localhost:8080/actuator/info'


def main() -> None:
    docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])

    utils.log('Running API tests')
    response = requests.get(URL)
    if 200 != response.status_code:
        print(f"::error::Expected 200 response code but received {response.status_code} from ${URL}")
        sys.exit(1)


if __name__ == '__main__':
    main()
