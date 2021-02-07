#!/usr/bin/env python3

"""Run

Build and run the application.

Usage:
  run.py [--cache-from <cache>... | --no-cache] [--start-db | --apply-migrations] [--debug | --suspend] [--detach]

Options:
  --apply-migrations       Apply database migrations; equivalent to `--start-db --apply-migrations`
  --cache-from <cache>...  Docker image(s) to reuse cache from
  --debug                  Enable Tomcat debug port
  --detach                 Detach the Docker container
  -h, --help               Show this help
  --no-cache               Don't use cache for the build
  --start-db               Start the database container
  --suspend                Suspend the web server until the remote debugger has connected; equivalent to
                           `--debug --suspend`
  -v, --version            Show the version
"""

import configparser
import glob
import os
import pathlib
import shutil
import time

import docker
import docopt

import database
import utils

SCRIPT_PATH = os.path.realpath(__file__)
SCRIPT_DIR = os.path.dirname(SCRIPT_PATH)
CONFIG = configparser.ConfigParser()
CONFIG.read(f"{SCRIPT_DIR}/config.ini")

MAIN_IMAGE = CONFIG['DEFAULT']['project_name']
MAIN_CONTAINER = MAIN_IMAGE
SPRING_PORT = CONFIG['SPRING']['port']
DEBUG_PORT = CONFIG['SPRING']['debug_port']

CACHE_VOLUME = CONFIG['DOCKER']['cache_volume']
NETWORK = f"{MAIN_CONTAINER}-network"

BUILD_IMAGE = f"{MAIN_IMAGE}-gradle-build"
BUILD_CONTAINER = BUILD_IMAGE
BUILD_COMMAND = 'gradle build --stacktrace --exclude-task test'

DATABASE_CONTAINER = f"{MAIN_IMAGE}-database"


def main() -> None:
    args = docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])
    docker_client = docker.from_env()

    if not utils.docker_item_exists(docker_client, 'volumes', CACHE_VOLUME):
        utils.log(f"Creating '{CACHE_VOLUME}' volume")
        # docker_client.volumes.create(name=CACHE_VOLUME)
        utils.execute_cmd(['docker', 'volume', 'create', CACHE_VOLUME])

    if not utils.docker_item_exists(docker_client, 'networks', NETWORK):
        utils.log(f"Creating '{NETWORK}' network")
        # docker_client.networks.create(name=NETWORK)
        utils.execute_cmd(['docker', 'network', 'create', NETWORK])


    utils.log(f"Building '{BUILD_IMAGE}' image")
    # build_image_id, build_log_generator = docker_client.images.build(
    #     cache_from=([] if args['--no-cache'] else args['--cache-from']),
    #     dockerfile='./docker/Dockerfile-gradle',
    #     nocache=args['--no-cache'],
    #     path='.',
    #     tag=BUILD_IMAGE,
    # )
    # for line in build_log_generator:
    #     # Each line is a JSON-decoded object
    #     if 'stream' in line:
    #         stripped_line = line['stream'].strip()
    #         if stripped_line:
    #             print(stripped_line)
    #     else:
    #         print(line)
    build_image_cmd = [
        'docker',
        'build',
        '--tag',
        BUILD_IMAGE,
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


    utils.log(f"Running '{BUILD_CONTAINER}' image")
    # build_container = docker_client.containers.run(
    #     command=BUILD_COMMAND,
    #     detach=True,
    #     image=BUILD_IMAGE,
    #     name=BUILD_CONTAINER,
    #     volumes={
    #         CACHE_VOLUME: {
    #             'bind': '/home/gradle/.gradle',
    #             'mode': 'rw',
    #         },
    #     },
    #     user='gradle',
    # )
    # for line in build_container.logs(stream=True):
    #     print(bytes.decode(line).strip())
    build_container_cmd = [
        'docker',
        'run',
        '--name',
        BUILD_CONTAINER,
        '--volume',
        f"{CACHE_VOLUME}:/home/gradle/.gradle",
        '--user',
        'gradle',
        BUILD_IMAGE,
    ]
    build_container_cmd.extend(BUILD_COMMAND.split(' '))
    if not args['--detach']:
        build_container_cmd[2:2] = ['--interactive', '--tty']
    utils.execute_cmd(build_container_cmd)


    utils.log(f"Copying JAR from '{BUILD_CONTAINER}'")
    shutil.rmtree(os.path.join('build', 'libs'), ignore_errors=True)
    pathlib.Path(os.path.join('build')).mkdir(parents=True, exist_ok=True)
    # bits, stat = docker_client.containers.get(BUILD_CONTAINER).get_archive('/home/gradle/project/build/libs')
    # with open('./build/app_archive.tar.gz', 'wb') as local_jar_archive:
    #     for chunk in bits:
    #         local_jar_archive.write(chunk)
    # with tarfile.open('./build/app_archive.tar.gz') as local_jar_archive:
    #     local_jar_archive.extractall(path='./build')
    utils.execute_cmd(['docker', 'cp', f"{BUILD_CONTAINER}:/home/gradle/project/build/libs", os.path.join('build')])
    for file in glob.glob(os.path.join('build', 'libs', '*.jar')):
        if file.endswith('.jar'):
            os.rename(file, os.path.join('build', 'libs', 'app.jar'))
            break


    utils.log(f"Building '{MAIN_IMAGE}' image")
    main_image_cmd = [
        'docker',
        'build',
        '--tag',
        MAIN_IMAGE,
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


    utils.log(f"Creating '{MAIN_CONTAINER}' container")
    main_container_cmd = [
        'docker',
        'create',
        '--publish',
        f"{SPRING_PORT}:{SPRING_PORT}",
        '--name',
        MAIN_CONTAINER,
        '--network',
        NETWORK,
        MAIN_IMAGE,
    ]
    if args['--suspend'] or args['--debug']:
        main_container_cmd[2:2] = ['--publish', f"{DEBUG_PORT}:{DEBUG_PORT}"]
    if not args['--detach']:
        main_container_cmd[2:2] = ['--interactive', '--tty']
    utils.execute_cmd(main_container_cmd)


    utils.log(f"Copying JAR into '{MAIN_CONTAINER}'")
    utils.execute_cmd([
        'docker',
        'cp',
        os.path.join('build', 'libs', 'app.jar'),
        f"{MAIN_CONTAINER}:/home/project/app.jar",
    ])


    if args['--apply-migrations'] or args['--start-db']:
        database.run_db_container(DATABASE_CONTAINER, NETWORK)
        if args['--apply-migrations']:
            time.sleep(3)  # Wait for the database to come up
            database.apply_migrations()


    utils.log(f"Starting '{MAIN_CONTAINER}'")
    main_start_cmd = ['docker', 'start', MAIN_CONTAINER]
    if not args['--detach']:
        main_start_cmd[2:2] = ['--attach', '--interactive']
    utils.execute_cmd(main_start_cmd)


if __name__ == '__main__':
    main()
