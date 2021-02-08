#!/usr/bin/env python3

"""pgAdmin

Launch pgAdmin Docker container.

Usage:
  pgadmin.py [--detach]
             [-h | --help]
             [--network <n>]
             [--rebuild]
             [-v | --version]

Options:
  --detach       Detach the container.
  -h, --help     Show this help message.
  --network <n>  Name of a Docker network to operate within. Defaults to the
                 Docker network name from the config file.
  --rebuild      Rebuild the container, if it exists.
  -v, --version  Show the scripts' version.

Envars:
  PG_USERNAME    pgAdmin admin login username.
  PG_PASSWORD    pgAdmin admin login password.

Example:
  export PG_USERNAME='user@domain.com'
  export PG_PASSWORD='SuperSecret'
  pgadmin.py --detach
"""

import os

import docopt

import utils
import teardown

CONFIG = utils.get_config(module_path=__file__)


def main() -> None:
    args = docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])

    pg_username = os.environ.get('PG_USERNAME')
    pg_password = os.environ.get('PG_PASSWORD')
    if not pg_username or not pg_password:
        utils.raise_error(
            'One or more envars have not been specified',
            usage=lambda: print(__doc__.strip('\n'))
        )

    image_name = CONFIG['DATABASE']['pgadmin_image']
    container_name = CONFIG['DATABASE']['pgadmin_container']
    network = args['--network'] if args['--network'] else CONFIG['DOCKER']['network']
    if not utils.exists_docker_item('network', network):
        utils.raise_error(f"Docker network '{network}' doesn't exist")

    def create_pgadmin_container() -> None:
        utils.log(f"Creating '{image_name}' container, name: {container_name}")
        utils.execute_cmd([
            'docker',
            'create',
            '--interactive',
            '--tty',
            '--env',
            f"PGADMIN_DEFAULT_EMAIL={pg_username}",
            '--env',
            f"PGADMIN_DEFAULT_PASSWORD={pg_password}",
            '--publish',
            f"{CONFIG['DATABASE']['pgadmin_port']}:80",
            '--network',
            network,
            '--name',
            container_name,
            image_name,
        ])

    if utils.exists_docker_item('container', container_name):
        if args['--rebuild']:
            teardown.rm_container(teardown.DockerContainer(container_name, rm_volumes=True))
            create_pgadmin_container()
    else:
        create_pgadmin_container()

    utils.log(f"Starting '{container_name}' container")
    start_cmd = [
        'docker',
        'start',
        container_name,
    ]
    if not args['--detach']:
        start_cmd[2:2] = ['--interactive']
    utils.execute_cmd(start_cmd)


if __name__ == '__main__':
    main()
