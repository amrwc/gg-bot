"""Helper class for database-related tasks."""

import hashlib
import os
import pathlib
import shutil
import tarfile
import urllib.request
from typing import List

import util.utils as utils

DRIVER_URL = 'https://jdbc.postgresql.org/download/postgresql-42.2.18.jar'
SHA256_DRIVER = '0c891979f1eb2fe44432da114d09760b5063dad9e669ac0ac6b0b6bfb91bb3ba'
DRIVER_PATH = os.path.join('tmp', 'db-driver', 'postgresql.jar')

LIQUIBASE_URL = 'https://github.com/liquibase/liquibase/releases/download/v4.2.2/liquibase-4.2.2.tar.gz'
SHA256_LIQUIBASE_ARCHIVE = '807ef4b514d01fc62f7aaf4150a8435c90ccb5986f3272d3cfd1bd26c2cf7b4c'
SHA256_LIQUIBASE_JAR = 'c092425c70b76bb28b6c260c1db8ee4845b7c4888f137937869393abca03af11'
LIQUIBASE_DIR = os.path.join('tmp', 'liquibase')
LIQUIBASE_PATH = os.path.join(LIQUIBASE_DIR, 'liquibase.jar')
LIQUIBASE_ARCHIVE = os.path.join(LIQUIBASE_DIR, 'liquibase.tar.gz')
LIQUIBASE_PROPERTIES_PATH = os.path.join('src', 'main', 'resources', 'liquibase.properties')


def fetch_dependencies() -> List[str]:
    """Fetches external file dependencies and prepares base Liquibase command.

    Returns:
        Base Liquibase command with prefilled required arguments.
    """
    fetch_db_driver()

    liquibase_cmd = ['liquibase']
    if shutil.which('liquibase') is None:
        fetch_liquibase_jar()
        liquibase_cmd = ['java', '-jar', LIQUIBASE_PATH]

    liquibase_cmd.extend([
        f"--classpath={DRIVER_PATH}",
        f"--defaultsFile={LIQUIBASE_PROPERTIES_PATH}",
        f"--url={os.environ.get('POSTGRES_URL')}/{os.environ.get('POSTGRES_DB')}",
        f"--username={os.environ.get('POSTGRES_USER')}",
        f"--password={os.environ.get('POSTGRES_PASSWORD')}",
    ])

    return liquibase_cmd


def fetch_db_driver() -> None:
    """Downloads database driver if it doesn't exist."""
    if not os.path.isfile(DRIVER_PATH):
        utils.log(f"Downloading database driver to '{DRIVER_PATH}'")
        pathlib.Path(os.path.dirname(DRIVER_PATH)).mkdir(parents=True, exist_ok=True)
        urllib.request.urlretrieve(DRIVER_URL, DRIVER_PATH)
    check_sha256(DRIVER_PATH, SHA256_DRIVER)


def fetch_liquibase_jar() -> None:
    """Downloads Liquibase archive and extracts the JAR file if it doesn't exist."""
    if not os.path.isfile(LIQUIBASE_PATH):
        utils.log(f"Downloading and extracting Liquibase to '{LIQUIBASE_PATH}'")
        pathlib.Path(os.path.dirname(LIQUIBASE_PATH)).mkdir(parents=True, exist_ok=True)
        urllib.request.urlretrieve(LIQUIBASE_URL, LIQUIBASE_ARCHIVE)
        check_sha256(LIQUIBASE_ARCHIVE, SHA256_LIQUIBASE_ARCHIVE)
        with tarfile.open(LIQUIBASE_ARCHIVE) as liquibase_archive:
            jar_reader = liquibase_archive.extractfile('liquibase.jar')
            with open(LIQUIBASE_PATH, 'wb') as jar:
                jar.write(jar_reader.read())
    check_sha256(LIQUIBASE_PATH, SHA256_LIQUIBASE_JAR)


def check_sha256(file_path: str, sha256_hash: str) -> None:
    """Checks whether SHA256 digest hash of the given file matches the given hash.

    Args:
        file_path (str): Path to the file to be checked.
        sha256_hash (str): Hash to compare against.
    """
    utils.log(f"Checking SHA256 digest of {file_path}")
    with open(file_path, 'rb') as file:
        file_bytes = file.read()
        driver_digest = hashlib.sha256(file_bytes).hexdigest()
        if sha256_hash != driver_digest:
            utils.raise_error(f"SHA256 checksum of '{file_path}' doesn't match '{sha256_hash}'")
