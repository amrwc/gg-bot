from abc import ABC, abstractmethod

from command.command_result import CommandResult


class Command(ABC):
    """Abstract command in Chain of Responsibility."""

    @abstractmethod
    def execute(self) -> CommandResult:
        """Executes the command, and returns the result status.

        Returns:
            Result of the execution.
        """
        raise NotImplementedError
