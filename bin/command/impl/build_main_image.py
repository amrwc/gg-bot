import os

from command.command import Command
from command.command_result import CommandResult
from context.impl.run_chain_context import RunChainContext
from util import utils, docker_utils


class BuildMainImage(Command):
    """Builds the main image."""

    context: RunChainContext

    def __init__(self, context: RunChainContext) -> None:
        self.context = context

    def execute(self) -> CommandResult:
        main_image = self.context.config['DOCKER']['main_image']

        if not self.context.args['--rebuild'] and docker_utils.item_exists('image', main_image):
            utils.warn(f"Image '{main_image}' already exists, not building")
            return CommandResult.SKIPPED

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

        if self.context.args['--no-cache']:
            main_image_cmd.insert(2, '--no-cache')
        else:
            for item in self.context.args['--cache-from']:
                main_image_cmd[2:2] = ['--cache-from', item]

        if self.context.args['--suspend'] or self.context.args['--debug']:
            main_image_cmd[2:2] = ['--build-arg', 'suspend=true' if self.context.args['--suspend'] else 'debug=true']

        completed_process = utils.execute_cmd(main_image_cmd)
        if 0 != completed_process.returncode:
            utils.warn(f"Failed running the following command: {main_image_cmd}")
            return CommandResult.FAILED

        return CommandResult.OK
