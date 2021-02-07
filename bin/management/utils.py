#!/usr/bin/env python3

"""Common utilities."""

import datetime
import subprocess
from typing import Callable, List

import sys


def docker_item_exists(docker_client, kind: str, name: str) -> bool:
    return bool(docker_client.__getattribute__(kind).list(filters={'name': [name]}))


def raise_error(message: str, cmd: List[str] = None, usage: Callable[[], None] = None) -> None:
    """Prints the given error message and exits with a non-zero code.

    Args:
        message (str): Error message to display.
        cmd (list): Optional; The command that caused the error. If defined, it's displayed for reference.
        usage: Optional; Closure that displays usage instructions upon calling.
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
    print_coloured(f"[{get_time()}] {message}\n", 'white')


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
    sys.stdout.write(f"{text_effect}{get_colour(colour)}{text}{get_text_effect('reset')}")


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
        'blue': '34m',
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


def execute_cmd(cmd: List[str]) -> subprocess.CompletedProcess:
    """Executes the given shell command.

    Args:
        cmd (list): Shell directive to execute.

    Returns:
        `CompletedProcess` object.
    """
    try:
        return subprocess.run(cmd)
    except subprocess.CalledProcessError:
        raise_error('Exception occurred while running the following command:', cmd)
    except KeyboardInterrupt:
        print_coloured(f"\n[{get_time()}] ", 'white')
        print_coloured('KeyboardInterrupt: ', 'yellow', 'bold')
        print_coloured('User halted the execution of the following command:\n', 'yellow')
        print_cmd(cmd)
        sys.exit(1)
