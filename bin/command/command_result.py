from enum import Enum, auto, unique


@unique
class CommandResult(Enum):
    """Result of a command execution."""

    OK = auto()
    FAILED = auto()
    SKIPPED = auto()

    def is_failure(self) -> bool:
        """Determines whether the result status indicates a failure.

        Returns:
            Whether it's a failure result.
        """
        return self in {CommandResult.FAILED}

    def is_success(self) -> bool:
        """Determines whether the result status indicates a success.

        Returns:
            Whether it's a success result.
        """
        return self in {CommandResult.OK, CommandResult.SKIPPED}
