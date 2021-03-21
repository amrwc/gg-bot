import glob
import os
import pathlib
import shutil

from command.command import Command
from command.command_result import CommandResult
from context.impl.run_chain_context import RunChainContext
from util import utils


class CopyJarOut(Command):
    """Copies the compiled JAR file out of the build container."""

    context: RunChainContext

    def __init__(self, context: RunChainContext) -> None:
        self.context = context

    def execute(self) -> CommandResult:
        build_container = self.context.config['DOCKER']['build_image']

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
