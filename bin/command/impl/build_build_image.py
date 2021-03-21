import os

from command.command import Command
from command.command_result import CommandResult
from context.impl.run_chain_context import RunChainContext
from util import docker_utils, utils


class BuildBuildImage(Command):
    """Builds the build image."""

    context: RunChainContext

    def __init__(self, context: RunChainContext) -> None:
        self.context = context

    def execute(self) -> CommandResult:
        build_image = self.context.config['DOCKER']['build_image']

        if not self.context.args['--rebuild'] and docker_utils.item_exists('image', build_image):
            utils.warn(f"Image '{build_image}' already exists, not building")
            return CommandResult.SKIPPED

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
        if self.context.args['--no-cache']:
            build_image_cmd.insert(2, '--no-cache')
        else:
            for item in self.context.args['--cache-from']:
                build_image_cmd[2:2] = ['--cache-from', item]

        completed_process = utils.execute_cmd(build_image_cmd)
        return CommandResult.OK if 0 == completed_process.returncode else CommandResult.FAILED
