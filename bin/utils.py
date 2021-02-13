#!/usr/bin/env python3

"""Common utilities."""

import configparser
import datetime
import os
import subprocess
from typing import Callable, List

import sys


def get_config(config_path: str = None, module_path: str = None) -> configparser.ConfigParser:
    """Reads config on the given path.

    Args:
        config_path (str): Optional; Path to the config file.
        module_path (str): Optional; Path to the calling module. Normally, the value of `__file__`.

    Returns:
        `ConfigParser` instance with the loaded config.

    Raises:
        ValueError: If neither or both parameters have a value.
    """
    if not config_path and not module_path:
        raise ValueError('One of `config_path`, `module_path` parameters must be provided')
    if config_path and module_path:
        raise ValueError('Only one of `config_path`, `module_path` parameters must be provided')

    config = configparser.ConfigParser()
    if config_path:
        config.read(config_path)
        return config
    if module_path:
        script_path = os.path.realpath(module_path)
        script_dir = os.path.dirname(script_path)
        config.read(os.path.join(script_dir, 'config.ini'))
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


def warn(message: str) -> None:
    """Logs the given warning to the command line.

    Args:
        message (str): Warning message to be displayed.
    """
    print_coloured(f"[{get_time()}] ➜ {message}\n", colour='yellow', effect='bold')


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


def execute_cmd(cmd: List[str], pipe_stdout: bool = False, pipe_stderr: bool = False) -> subprocess.CompletedProcess:
    """Executes the given shell command.

    Args:
        cmd (list): Shell directive to execute.
        pipe_stdout (bool): Whether to pipe stdout into the `stdout` field in the `CompletedProcess` object.
        pipe_stderr (bool): Whether to pipe stderr into the `stderr` field in the `CompletedProcess` object.

    Returns:
        `CompletedProcess` object.
    """
    try:
        stdout = subprocess.PIPE if pipe_stdout else None
        stderr = subprocess.PIPE if pipe_stderr else None
        return subprocess.run(cmd, stdout=stdout, stderr=stderr)
    except subprocess.CalledProcessError:
        raise_error('Exception occurred while running the following command:', cmd)
    except KeyboardInterrupt:
        print_coloured(f"\n[{get_time()}] ", 'white')
        print_coloured('KeyboardInterrupt: ', 'yellow', 'bold')
        print_coloured('User halted the execution of the following command:\n', 'yellow')
        print_cmd(cmd)
        sys.exit(1)


def verify_envars(
        envars: List[str],
        envars_category: str,
        doc_string: str
) -> None:
    """Verifies that the required envars are defined. Results in an error otherwise.

    Args:
        envars (List[str]): List of envar names to be verified.
        envars_category (str): Category of the envars for the error message.
        doc_string (str): Docstring (__doc__) of the caller.
    """
    if not all([os.environ.get(envar) for envar in envars]):
        raise_error(
            f"One or more of the required {envars_category} envars have not been defined",
            usage=lambda: print(doc_string.strip('\n'))
        )
