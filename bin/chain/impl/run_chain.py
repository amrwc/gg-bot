from configparser import ConfigParser
from typing import Any, Dict

from chain.chain import Chain
from command.impl.build_build_image import BuildBuildImage
from command.impl.build_main_image import BuildMainImage
from command.impl.copy_jar_out import CopyJarOut
from command.impl.prepare_docker_environment import PrepareDockerEnvironment
from command.impl.run_build_image import RunBuildImage
from context.impl.run_chain_context import RunChainContext


class RunChain(Chain):
    """Run Chain that builds and executes the application."""

    context: RunChainContext

    def __init__(self, config: ConfigParser, args: Dict[str, Any]) -> None:
        super().__init__()
        self.context = RunChainContext(config, args)
        self.build_chain()

    def execute(self) -> bool:
        return super().execute()

    def build_chain(self) -> None:
        super().append_commands([
            PrepareDockerEnvironment(self.context),
            BuildBuildImage(self.context),
            RunBuildImage(self.context),
            CopyJarOut(self.context),
            BuildMainImage(self.context),
        ])
