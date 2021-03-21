from configparser import ConfigParser
from typing import Any


class Context:
    """Abstract Chain of Responsibility context."""

    config: ConfigParser
    args: dict[str, Any]
    skip: set[str]

    def __init__(self, config: ConfigParser, args: dict[str, Any]) -> None:
        self.config = config
        self.args = args
        self.skip = set()
