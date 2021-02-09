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
  -v, --version        Show the scripts' version.
"""

import glob
import os
import pathlib
import shutil

import docopt

import database
import docker_utils
import utils

CONFIG = utils.get_config(module_path=__file__)


def main() -> None:
    args = docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])

    docker_utils.create_volume(CONFIG['DOCKER']['cache_volume'])
    docker_utils.create_network(CONFIG['DOCKER']['network'])
    build_build_image(args)
    run_build_image(args)
    build_main_image(args)
    create_main_container(args)
    database.start(migrations=args['--apply-migrations'], start_db=args['--start-db'])
    start_main_container(args)


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
        docker_utils.rm_container(docker_utils.DockerContainer(build_container))

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
    main_image = CONFIG['DOCKER']['main_image']

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
    main_image = CONFIG['DOCKER']['main_image']
    main_container = main_image

    if args['--rebuild']:
        docker_utils.rm_container(docker_utils.DockerContainer(main_container, rm_volumes=True))

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


def start_main_container(args: dict) -> None:
    """Builds the main image.

    Args:
        args (dict): Parsed command-line arguments passed to the script.
    """
    main_container = CONFIG['DOCKER']['main_image']

    utils.log(f"Starting '{main_container}'")
    main_start_cmd = ['docker', 'start', main_container]
    if not args['--detach']:
        main_start_cmd[2:2] = ['--attach', '--interactive']
    utils.execute_cmd(main_start_cmd)


if __name__ == '__main__':
    main()
