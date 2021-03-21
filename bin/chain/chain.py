from typing import List

from command.command import Command
from context.context import Context
from util import utils


class Chain:
    """Chain of Responsibility parent class."""

    commands: List[Command]
    context: Context

    def __init__(self) -> None:
        self.commands = []

    def execute(self) -> bool:
        """Executes the chain.

        Returns:
            Whether the execution was successful.
        """
        for command in self.commands:
            command_name = command.__class__.__name__
            if command_name in self.context.skip:
                utils.warn(f"Skipping command: '{command_name}'")
                continue

            result = command.execute()
            if result.is_failure():
                utils.warn(f"Execution status of '{command_name}' command: {result.name}")
                return False

        return True

    def append_commands(self, commands: List[Command]) -> None:
        """Appends the given commands to the chain.

        Args:
            commands (List[Command]): List of commands to append to the chain.
        """
        self.commands.extend(commands)
