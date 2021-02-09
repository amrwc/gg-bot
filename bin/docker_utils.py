"""Docker Utils

Common Docker-related utilities.
"""

import utils

CONFIG = utils.get_config(module_path=__file__)


def item_exists(command: str, item: str) -> bool:
    """Determines whether the given Docker item exists.

    Args:
        command (str): Docker command dictating the type of the given item. E.g. `container`, `volume`, `network`. See
                       `docker --help` for a full list of commands.
        item (str): Name of the checked Docker item.
    """
    cmd = ['docker']
    # Special treatment for containers
    cmd.extend(['ps', '-a'] if 'container' == command else [command, 'ls'])
    return item in utils.execute_cmd(cmd, pipe_stdout=True).stdout.decode('utf8')


def create_network(name: str = None) -> None:
    """Creates a Docker network if the name isn't taken.

    Args:
        name (str): Optional; Name of the network. Defaults to the network name specified in the config file.
    """
    network_name = name if name else CONFIG['DOCKER']['network']
    if item_exists('network', network_name):
        utils.warn(f"Network '{network_name}' already exists. Skipping")
    else:
        utils.log(f"Creating '{network_name}' network")
        utils.execute_cmd(['docker', 'network', 'create', network_name])


def create_volume(name: str) -> None:
    """Creates a Docker volume if the name isn't taken.

    Args:
        name (str): Name of the volume.
    """
    if item_exists('volume', name):
        utils.warn(f"Volume '{name}' already exists. Skipping")
    else:
        utils.log(f"Creating '{name}' volume")
        utils.execute_cmd(['docker', 'volume', 'create', name])


class DockerContainer:
    def __init__(self, name: str, rm_volumes: bool = False):
        self.name = name
        self.rm_volumes = rm_volumes


def rm_container(container: DockerContainer) -> None:
    """Removes the given Docker container.

    Args:
        container (DockerContainer): Container to remove.
    """
    if not item_exists('container', container.name):
        utils.warn(f"Container '{container.name}' doesn't exist, not removing")
        return

    utils.log(f"Stopping '{container.name}' container")
    utils.execute_cmd(['docker', 'container', 'stop', container.name])

    utils.log(f"Removing '{container.name}' container")
    rm_cmd = ['docker', 'container', 'rm', container.name]
    if container.rm_volumes:
        rm_cmd[3:3] = ['--volumes']
    utils.execute_cmd(rm_cmd)
