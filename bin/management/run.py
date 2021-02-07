#!/usr/bin/env python3

"""Run

Build and run the application.

Usage:
  run.py [--apply-migrations | --start-db]
         [--cache-from <c>... | --no-cache]
         [--debug | --suspend]
         [--detach]
         [-h | --help]
         [--rebuild]
         [-v | --version]

Options:
  --apply-migrations   Apply database migrations; includes `--start-db`.
  --cache-from <c>...  Docker image(s) to reuse cache from.
  --debug              Enable Tomcat debug port.
  --detach             Detach the Docker container.
  -h, --help           Show this help message.
  --no-cache           Don't use cache for the build.
  --rebuild            Recreate the build container and rebuild the main
                       container.
  --start-db           Start the database container
  --suspend            Suspend the web server until the remote debugger has
                       connected; includes `--debug`.
  -v, --version        Show the script's version.
"""

import glob
import os
import pathlib
import shutil

import docopt
import time

import database
import utils

CONFIG = utils.get_config(module_path=__file__)


def main() -> None:
    args = docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])

    create_cache_volume()
    create_network()
    build_build_image(args)
    run_build_image(args)
    build_main_image(args)
    create_main_container(args)
    start_db(args)
    start_main_container(args)


def create_cache_volume() -> None:
    """Creates Docker volume for persisting Gradle cache."""
    cache_volume = CONFIG['DOCKER']['cache_volume']
    if cache_volume not in utils.execute_cmd(['docker', 'volume', 'ls'], pipe_stdout=True).stdout.decode('utf8'):
        utils.log(f"Creating '{cache_volume}' volume")
        utils.execute_cmd(['docker', 'volume', 'create', cache_volume])


def create_network() -> None:
    """Creates Docker network."""
    network = CONFIG['DOCKER']['network']
    if network not in utils.execute_cmd(['docker', 'network', 'ls'], pipe_stdout=True).stdout.decode('utf8'):
        utils.log(f"Creating '{network}' network")
        utils.execute_cmd(['docker', 'network', 'create', network])


def build_build_image(args: dict) -> None:
    """Builds the build image.

    Args:
        args (dict): Parsed command-line arguments passed to the script.
    """
    build_image = CONFIG['DOCKER']['build_image']

    utils.log(f"Building '{build_image}' image")
    build_image_cmd = [
        'docker',
        'build',
        '--tag',
        build_image,
        '--file',
        os.path.join('docker', 'Dockerfile-gradle'),
        '.',
    ]
    if args['--no-cache']:
        build_image_cmd.insert(2, '--no-cache')
    elif args['--cache-from']:
        for item in args['--cache-from']:
            build_image_cmd[2:2] = ['--cache-from', item]
    utils.execute_cmd(build_image_cmd)


def run_build_image(args: dict) -> None:
    """Runs the build image and copies the compiled JAR file out of the container.

    Args:
        args (dict): Parsed command-line arguments passed to the script.
    """
    build_image = CONFIG['DOCKER']['build_image']
    build_container = build_image

    if args['--rebuild']:
        remove_container(build_container)

    utils.log(f"Running '{build_image}' image")
    build_container_cmd = [
        'docker',
        'run',
        '--name',
        build_container,
        '--volume',
        f"{CONFIG['DOCKER']['cache_volume']}:/home/gradle/.gradle",
        '--user',
        'gradle',
        build_image,
    ]
    build_container_cmd.extend(CONFIG['DOCKER']['build_command'].split(' '))
    if not args['--detach']:
        build_container_cmd[2:2] = ['--interactive', '--tty']
    utils.execute_cmd(build_container_cmd)

    utils.log(f"Copying JAR from '{build_container}' container")
    shutil.rmtree(os.path.join('build', 'libs'), ignore_errors=True)
    pathlib.Path(os.path.join('build')).mkdir(parents=True, exist_ok=True)
    utils.execute_cmd([
        'docker',
        'cp',
        f"{build_container}:/home/gradle/project/build/libs",
        os.path.join('build'),
    ])
    for file in glob.glob(os.path.join('build', 'libs', '*.jar')):
        if file.endswith('.jar'):
            os.rename(file, os.path.join('build', 'libs', 'app.jar'))
            break


def build_main_image(args: dict) -> None:
    """Builds the main image.

    Args:
        args (dict): Parsed command-line arguments passed to the script.
    """
    main_image = CONFIG['DEFAULT']['project_name']

    utils.log(f"Building '{main_image}' image")
    main_image_cmd = [
        'docker',
        'build',
        '--tag',
        main_image,
        '--file',
        os.path.join('docker', 'Dockerfile'),
        '.',
    ]
    if args['--no-cache']:
        main_image_cmd.insert(2, '--no-cache')
    elif args['--cache-from']:
        for item in args['--cache-from']:
            main_image_cmd[2:2] = ['--cache-from', item]
    if args['--suspend'] or args['--debug']:
        main_image_cmd[2:2] = ['--build-arg', 'suspend=true' if args['--suspend'] else 'debug=true']
    utils.execute_cmd(main_image_cmd)


def create_main_container(args: dict) -> None:
    """Creates main Docker container.

    Args:
        args (dict): Parsed command-line arguments passed to the script.
    """
    main_image = CONFIG['DEFAULT']['project_name']
    main_container = main_image

    if args['--rebuild']:
        remove_container(main_container)

    utils.log(f"Creating '{main_container}' container")
    spring_port = CONFIG['SPRING']['port']
    main_container_cmd = [
        'docker',
        'create',
        '--publish',
        f"{spring_port}:{spring_port}",
        '--name',
        main_container,
        '--network',
        CONFIG['DOCKER']['network'],
        main_image,
    ]
    if args['--suspend'] or args['--debug']:
        debug_port = CONFIG['SPRING']['debug_port']
        main_container_cmd[2:2] = ['--publish', f"{debug_port}:{debug_port}"]
    if not args['--detach']:
        main_container_cmd[2:2] = ['--interactive', '--tty']
    utils.execute_cmd(main_container_cmd)

    utils.log(f"Copying JAR into '{main_container}'")
    utils.execute_cmd([
        'docker',
        'cp',
        os.path.join('build', 'libs', 'app.jar'),
        f"{main_container}:/home/project/app.jar",
    ])


def start_db(args: dict) -> None:
    """Runs the database image, and applies migrations, depending on the command-line arguments.

    Args:
        args (dict): Parsed command-line arguments passed to the script.
    """
    if args['--apply-migrations'] or args['--start-db']:
        database.run_db_container(CONFIG['DOCKER']['database_container'], CONFIG['DOCKER']['network'])
        if args['--apply-migrations']:
            time.sleep(3)  # Wait for the database to come up
            database.apply_migrations()


def start_main_container(args: dict) -> None:
    """Builds the main image.

    Args:
        args (dict): Parsed command-line arguments passed to the script.
    """
    main_container = CONFIG['DEFAULT']['project_name']

    utils.log(f"Starting '{main_container}'")
    main_start_cmd = ['docker', 'start', main_container]
    if not args['--detach']:
        main_start_cmd[2:2] = ['--attach', '--interactive']
    utils.execute_cmd(main_start_cmd)


def remove_container(container: str) -> None:
    """Removes the given Docker container if it exists.

    Args:
         container (str): Name of the container to remove.
    """
    if container in utils.execute_cmd(['docker', 'ps', '-a'], pipe_stdout=True).stdout.decode('utf8'):
        utils.log(f"Removing '{container}' container")
        utils.execute_cmd(['docker', 'rm', container])


if __name__ == '__main__':
    main()
