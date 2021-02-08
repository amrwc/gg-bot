#!/usr/bin/env python3

"""Teardown

Stops and removes the build container and image, and main container. Also
removes the items specified in the command-line arguments.

Usage:
  teardown.py [--cache] [--db] [-h | --help] [--tmp] [-v | --version]

Options:
  --cache        Remove build container cache volume.
  --db           Remove the database container.
  -h, --help     Show this help message.
  --tmp          Remove build and temporary directories.
  -v, --version  Show the script's version.
"""

import shutil

import docopt

import utils

CONFIG = utils.get_config(module_path=__file__)

TEMP_DIRECTORIES = ['.gradle', 'build', 'tmp']


class DockerContainer:
    def __init__(self, name: str, rm_volumes: bool = False):
        self.name = name
        self.rm_volumes = rm_volumes


def main() -> None:
    args = docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])

    containers = [
        DockerContainer(CONFIG['DOCKER']['build_image']),
        DockerContainer(CONFIG['DOCKER']['main_image'], rm_volumes=True),
    ]
    images = [
        CONFIG['DOCKER']['build_image'],
        CONFIG['DOCKER']['main_image'],
    ]

    if args['--db']:
        containers.append(DockerContainer(CONFIG['DOCKER']['database_container'], rm_volumes=True))

    for container in containers:
        rm_container(container)
    for image in images:
        rm_image(image)

    if args['--db']:
        # Nothing should exist within the network at this point
        utils.execute_cmd(['docker', 'network', 'rm', CONFIG['DOCKER']['network']])
    if args['--cache']:
        utils.execute_cmd(['docker', 'volume', 'rm', CONFIG['DOCKER']['cache_volume']])
    if args['--tmp']:
        for tmp in TEMP_DIRECTORIES:
            shutil.rmtree(tmp, ignore_errors=True)


def rm_container(container: DockerContainer) -> None:
    """Removes the given Docker container.

    Args:
        container (DockerContainer): Container to remove.
    """
    utils.log(f"Stopping '{container.name}' container")
    utils.execute_cmd(['docker', 'container', 'stop', container.name])

    utils.log(f"Removing '{container.name}' container")
    rm_cmd = ['docker', 'container', 'rm', container.name]
    if container.rm_volumes:
        rm_cmd[3:3] = ['--volumes']
    utils.execute_cmd(rm_cmd)


def rm_image(name: str) -> None:
    """Removes the given Docker image.

    Args:
        name (str): Image to remove.
    """
    utils.log(f"Removing '{name}' image")
    utils.execute_cmd(['docker', 'image', 'rm', name])


if __name__ == '__main__':
    main()
