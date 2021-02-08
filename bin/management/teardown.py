#!/usr/bin/env python3

"""Teardown

Stops and removes the build container and image, and main container. Also
removes the items specified in the command-line arguments.

Usage:
  teardown.py [--db] [--cache] [--tmp]

Options:
  --cache              Remove build container cache volume.
  --db                 Remove the database container.
  -h, --help           Show this help message.
  --tmp                Remove build and temporary directories.
  -v, --version        Show the script's version.
"""

import shutil

import docopt

import utils

CONFIG = utils.get_config(module_path=__file__)

TEMP_DIRECTORIES = ['.gradle', 'build', 'tmp']


def main() -> None:
    args = docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])

    build_container_and_image()
    main_container_and_image()
    if args['--db']:
        database()
        # Nothing should exist within the network at this point
        utils.execute_cmd(['docker', 'network', 'rm', CONFIG['DOCKER']['network']])
    if args['--cache']:
        utils.execute_cmd(['docker', 'volume', 'rm', CONFIG['DOCKER']['cache_volume']])
    if args['--tmp']:
        for tmp in TEMP_DIRECTORIES:
            shutil.rmtree(tmp, ignore_errors=True)


def build_container_and_image() -> None:
    build_image = CONFIG['DOCKER']['build_image']
    build_container = build_image
    container(build_container)
    image(build_image)


def main_container_and_image() -> None:
    main_image = CONFIG['DOCKER']['main_image']
    main_container = main_image
    container(main_container, rm_volumes=True)
    image(main_image)


def database() -> None:
    database_container = CONFIG['DOCKER']['database_container']
    container(database_container, rm_volumes=True)


def container(name: str, rm_volumes: bool = False) -> None:
    utils.log(f"Stopping '{name}' container")
    utils.execute_cmd(['docker', 'container', 'stop', name])

    utils.log(f"Removing '{name}' container")
    rm_cmd = ['docker', 'container', 'rm', name]
    if rm_volumes:
        rm_cmd[3:3] = ['--volumes']
    utils.execute_cmd(rm_cmd)


def image(name: str) -> None:
    utils.log(f"Removing '{name}' image")
    utils.execute_cmd(['docker', 'image', 'rm', name])


if __name__ == '__main__':
    main()
