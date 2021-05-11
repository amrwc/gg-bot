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
  --apply-migrations   Apply database migrations. Includes `--start-db`.
  --cache-from <c>...  Docker image(s) to reuse cache from.
  --debug              Enable Tomcat debug port.
  --detach             Detach the Docker container.
  -h, --help           Show this help message.
  --no-cache           Don't use cache for the build.
  --rebuild            Recreate the build container and rebuild the main
                       container.
  --start-db           Start the database container
  --suspend            Suspend the web server until the remote debugger has
                       connected. Includes `--debug`.
  -v, --version        Show the scripts' version.

Envars:
  SPRING_DATASOURCE_URL       Database URL.
  SPRING_DATASOURCE_USERNAME  Database username.
  SPRING_DATASOURCE_PASSWORD  Database password.

  The above are equivalent to setting `spring.datasource.*` in `application.yml`.

Example:
  export SPRING_DATASOURCE_URL='jdbc:postgresql://database-container:5432/dbname'
  export SPRING_DATASOURCE_USERNAME='spring_user'
  export SPRING_DATASOURCE_PASSWORD='SpringUserPassword'
  ./bin/run.py --apply-migrations
"""

import glob
import os
import pathlib
import shutil

import docopt

import database
import util.docker_utils as docker_utils
import util.utils as utils

CONFIG = utils.get_config(module_path=__file__)
REQUIRED_ENVARS = [
    'SPRING_DATASOURCE_URL',
    'SPRING_DATASOURCE_USERNAME',
    'SPRING_DATASOURCE_PASSWORD',
]


def main() -> None:
    args = docopt.docopt(__doc__, version=CONFIG['DEFAULT']['script_version'])
    utils.verify_envars(REQUIRED_ENVARS, 'Spring', __doc__)

    main_image = CONFIG['DOCKER']['main_image']
    build_image = CONFIG['DOCKER']['build_image']

    if args['--rebuild']:
        docker_utils.rm_container(docker_utils.DockerContainer(main_image, rm_volumes=True))
        docker_utils.rm_image(main_image)
        docker_utils.rm_container(docker_utils.DockerContainer(build_image))
        docker_utils.rm_image(build_image)

    docker_utils.create_volume(CONFIG['DOCKER']['cache_volume'])
    docker_utils.create_network(CONFIG['DOCKER']['network'])
    build_build_image(args, build_image)
    run_build_image(args, build_image)
    build_main_image(args, main_image)
    create_main_container(args, main_image)
    database.start(migrations=args['--apply-migrations'], start_db=args['--start-db'])
    start_main_container(args)
    docker_utils.export_logs(main_image)


def build_build_image(args: dict, build_image: str = None) -> None:
    """Builds the build image.

    Args:
        args (dict): Parsed command-line arguments passed to the script.
        build_image (str): Optional; Build image name. Defaults to the value from config file.
    """
    build_image = build_image if build_image else CONFIG['DOCKER']['build_image']

    if not args['--rebuild'] and docker_utils.item_exists('image', build_image):
        utils.warn(f"Image '{build_image}' already exists, not building")
        return

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


def run_build_image(args: dict, build_image: str = None) -> None:
    """Runs the build image and copies the compiled JAR file out of the container.

    Args:
        args (dict): Parsed command-line arguments passed to the script.
        build_image (str): Optional; Build image name. Defaults to the value from config file.
    """
    build_image = build_image if build_image else CONFIG['DOCKER']['build_image']
    build_container = build_image

    if not args['--rebuild'] and docker_utils.item_exists('container', build_container):
        utils.warn(f"Container '{build_container}' already exists, not running")
        return

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


def build_main_image(args: dict, main_image: str = None) -> None:
    """Builds the main image.

    Args:
        args (dict): Parsed command-line arguments passed to the script.
        main_image (str): Optional; Main image name. Defaults to the value from config file.
    """
    main_image = main_image if main_image else CONFIG['DOCKER']['main_image']

    if not args['--rebuild'] and docker_utils.item_exists('image', main_image):
        utils.warn(f"Image '{main_image}' already exists, not building")
        return

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


def create_main_container(args: dict, main_image: str = None) -> None:
    """Creates main Docker container.

    Args:
        args (dict): Parsed command-line arguments passed to the script.
        main_image (str): Optional; Main image name. Defaults to the value from config file.
    """
    main_image = main_image if main_image else CONFIG['DOCKER']['main_image']
    main_container = main_image

    if not args['--rebuild'] and docker_utils.item_exists('container', main_container):
        utils.warn(f"Container '{main_container}' already exists, not creating")
        return

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
        '--env',
        f"SPRING_DATASOURCE_URL={os.environ.get('SPRING_DATASOURCE_URL')}",
        '--env',
        f"SPRING_DATASOURCE_USERNAME={os.environ.get('SPRING_DATASOURCE_USERNAME')}",
        '--env',
        f"SPRING_DATASOURCE_PASSWORD={os.environ.get('SPRING_DATASOURCE_PASSWORD')}",
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
