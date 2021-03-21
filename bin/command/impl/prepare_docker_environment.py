from command.command import Command
from command.command_result import CommandResult
from context.impl.run_chain_context import RunChainContext
from util import docker_utils


class PrepareDockerEnvironment(Command):
    """Prepares the Docker environment; creates volumes, networks."""

    context: RunChainContext

    def __init__(self, context: RunChainContext) -> None:
        self.context = context

    def execute(self) -> CommandResult:
        created_volume = docker_utils.create_volume(self.context.config['DOCKER']['cache_volume'])
        created_network = docker_utils.create_network(self.context.config['DOCKER']['network'])
        return CommandResult.OK if created_volume and created_network else CommandResult.FAILED
