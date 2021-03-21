from configparser import ConfigParser
from typing import Any

from context.context import Context


class RunChainContext(Context):
    """Context for the Run Chain CoR."""

    def __init__(self, config: ConfigParser, args: dict[str, Any]) -> None:
        super().__init__(config, args)
