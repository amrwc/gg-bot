"""Docker Utils

Common Docker-related utilities.
"""

import utils

CONFIG = utils.get_config(module_path=__file__)


def create_network(name: str = None) -> None:
    """Creates a Docker network.

    Args:
        name (str): Name of the network.
    """
    network = name if name else CONFIG['DOCKER']['network']
    if not utils.exists_docker_item('network', network):
        utils.log(f"Creating '{network}' network")
        utils.execute_cmd(['docker', 'network', 'create', network])
