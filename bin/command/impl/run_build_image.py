import glob
import os
import pathlib
import shutil

from command.command import Command
from command.command_result import CommandResult
from context.impl.run_chain_context import RunChainContext
from util import docker_utils, utils


class RunBuildImage(Command):
    """Runs the build image and copies the compiled JAR file out of the container."""

    context: RunChainContext

    def __init__(self, context: RunChainContext) -> None:
        self.context = context

    def execute(self) -> CommandResult:
        build_image = self.context.config['DOCKER']['build_image']
        build_container = build_image

        if not self.context.args['--rebuild'] and docker_utils.item_exists('container', build_container):
            utils.warn(f"Container '{build_container}' already exists, not running")
            return CommandResult.SKIPPED

        utils.log(f"Running '{build_image}' image")
        build_container_cmd = [
            'docker',
            'run',
            '--name',
            build_container,
            '--volume',
            f"{self.context.config['DOCKER']['cache_volume']}:/home/gradle/.gradle",
            '--user',
            'gradle',
            build_image,
        ]
        build_container_cmd.extend(self.context.config['DOCKER']['build_command'].split(' '))
        if not self.context.args['--detach']:
            build_container_cmd[2:2] = ['--interactive', '--tty']
        completed_process = utils.execute_cmd(build_container_cmd)
        if 0 != completed_process.returncode:
            utils.warn(f"Failed running the following command: {build_container_cmd}")
            return CommandResult.FAILED

        # TODO: Move to another command
        utils.log(f"Copying JAR from '{build_container}' container")
        shutil.rmtree(os.path.join('build', 'libs'), ignore_errors=True)
        pathlib.Path(os.path.join('build')).mkdir(parents=True, exist_ok=True)
        copy_jar_cmd = [
            'docker',
            'cp',
            f"{build_container}:/home/gradle/project/build/libs",
            os.path.join('build'),
        ]
        completed_process = utils.execute_cmd(copy_jar_cmd)
        if 0 != completed_process.returncode:
            utils.warn(f"Failed running the following command: {copy_jar_cmd}")
            return CommandResult.FAILED

        for file in glob.glob(os.path.join('build', 'libs', '*.jar')):
            if file.endswith('.jar'):
                os.rename(file, os.path.join('build', 'libs', 'app.jar'))
                break

        return CommandResult.OK
