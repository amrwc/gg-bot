#!/usr/bin/env python3

"""Teardown

Stops and removes the build container and image, and main container. Also
removes the items specified in the command-line arguments.

Usage:
  teardown.py [--cache]
              [--db]
              [-h | --help]
              [--network]
              [--tmp]
              [-v | --version]

Options:
  --cache        Remove build container cache volume.
  --db           Remove the database container.
  -h, --help     Show this help message.
  --network      Remove Docker network.
  --tmp          Remove build and temporary directories.
  -v, --version  Show the scripts' version.
"""

import shutil

import docopt

import docker_utils
import utils

CONFIG = utils.get_config(module_path=__file__)

TEMP_DIRECTORIES = ['.gradle', 'build', 'tmp']


def main() -> None:
    args = docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])

    containers = [
        docker_utils.DockerContainer(CONFIG['DOCKER']['build_image']),
        docker_utils.DockerContainer(CONFIG['DOCKER']['main_image'], rm_volumes=True),
    ]
    images = [
        CONFIG['DOCKER']['build_image'],
        CONFIG['DOCKER']['main_image'],
    ]

    if args['--db']:
        containers.append(docker_utils.DockerContainer(CONFIG['DATABASE']['database_container'], rm_volumes=True))

    for container in containers:
        docker_utils.rm_container(container)
    for image in images:
        rm_image(image)

    if args['--network']:
        utils.execute_cmd(['docker', 'network', 'rm', CONFIG['DOCKER']['network']])
    if args['--cache']:
        utils.execute_cmd(['docker', 'volume', 'rm', CONFIG['DOCKER']['cache_volume']])
    if args['--tmp']:
        for tmp in TEMP_DIRECTORIES:
            shutil.rmtree(tmp, ignore_errors=True)


def rm_image(name: str) -> None:
    """Removes the given Docker image.

    Args:
        name (str): Image to remove.
    """
    utils.log(f"Removing '{name}' image")
    utils.execute_cmd(['docker', 'image', 'rm', name])


if __name__ == '__main__':
    main()
