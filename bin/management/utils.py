#!/usr/bin/env python3

"""Common utilities."""

import configparser
import datetime
import subprocess
from typing import Callable, List

import sys


def get_config(config_path: str) -> configparser.ConfigParser:
    """Reads config on the given path.

    Args:
        config_path (str): Path to the config file.

    Returns:
        `ConfigParser` instance with the loaded config.
    """
    config = configparser.ConfigParser()
    config.read(config_path)
    return config


def raise_error(message: str, cmd: List[str] = None, usage: Callable[[], None] = None) -> None:
    """Prints the given error message and exits with a non-zero code.

    Args:
        message (str): Error message to display.
        cmd (list): Optional; The command that caused the error. If defined, it's displayed for reference.
        usage (Callable): Optional; Closure that displays usage instructions upon calling.
    """
    print_coloured(f"[{get_time()}] ", 'white')
    print_coloured('ERROR: ', 'red', 'bold')
    if cmd:
        print_coloured(f"{message}\n", 'red')
        print_cmd(cmd)
        print('')
    else:
        print_coloured(f"{message}\n", 'red')
    if usage:
        usage()
    sys.exit(1)


def log(message: str) -> None:
    """Logs the given message to the command line.

    Args:
        message (str): Log message to be displayed.
    """
    print_coloured(f"[{get_time()}] ➜ {message}\n", colour='purple', effect='bold')


def print_cmd(cmd: List[str]) -> None:
    """Prints the given command to the command line.

    Args:
        cmd (list): Command-line directive in a form of a list.
    """
    print_coloured(f"{' '.join(cmd)}\n", 'grey')


def get_time() -> str:
    """Returns current time.

    Returns:
        Time in HH:MM:SS format.
    """
    return datetime.datetime.now().strftime('%H:%M:%S')


def print_coloured(text: str, colour: str, effect: str = '') -> None:
    """Prints the given text in the given colour and effect.

    Args:
        text (str): Message to print out.
        colour (str): Display colour.
        effect (str): Optional; Effect to use, such as 'bold' or 'underline'.
    """
    text_effect = get_text_effect(effect)
    text_colour = get_colour(colour)
    reset = get_text_effect('reset')
    sys.stdout.write(f"{text_effect}{text_colour}{text}{reset}")


def get_colour(colour: str) -> str:
    """Returns an ANSI escape sequence for the given colour.

    Args:
        colour (str): Name of the colour.

    Returns:
        Escape sequence for the given colour.
    """
    sequence_base = '\033['
    colours = {
        'red': '31m',
        'yellow': '33m',
        'green': '32m',
        'violet': '34m',
        'purple': '35m',
        'grey': '37m',
        'white': '97m'
    }
    return f"{sequence_base}{colours[colour]}"


def get_text_effect(effect: str) -> str:
    """Returns an ASCII escape sequence for a text effect, such as 'bold'.

    Args:
        effect (str): Name of the effect.

    Returns:
        Escape sequence for the given effect.
    """
    sequence_base = '\033['
    effects = {
        '': '',
        'reset': '0m',
        'bold': '1m',
        'underline': '4m'
    }
    return f"{sequence_base}{effects[effect]}"


def execute_cmd(cmd: List[str], pipe_stdout: bool = False) -> subprocess.CompletedProcess:
    """Executes the given shell command.

    Args:
        cmd (list): Shell directive to execute.
        pipe_stdout (bool): Whether to pipe stdout into the `stdout` field in the `CompletedProcess` object.

    Returns:
        `CompletedProcess` object.
    """
    try:
        stdout = subprocess.PIPE if pipe_stdout else None
        return subprocess.run(cmd, stdout=stdout)
    except subprocess.CalledProcessError:
        raise_error('Exception occurred while running the following command:', cmd)
    except KeyboardInterrupt:
        print_coloured(f"\n[{get_time()}] ", 'white')
        print_coloured('KeyboardInterrupt: ', 'yellow', 'bold')
        print_coloured('User halted the execution of the following command:\n', 'yellow')
        print_cmd(cmd)
        sys.exit(1)
