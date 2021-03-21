from configparser import ConfigParser
from typing import Any, Dict, Set


class Context:
    """Abstract Chain of Responsibility context."""

    config: ConfigParser
    args: Dict[str, Any]
    skip: Set[str]

    def __init__(self, config: ConfigParser, args: Dict[str, Any]) -> None:
        self.config = config
        self.args = args
        self.skip = set()
