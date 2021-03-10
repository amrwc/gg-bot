# Working with `bin` Scripts

To work on the Python scripts in `bin/` directory, in IntelliJ IDEA, add import
the directory as a module.

1. Open Project Structure (`cmd + ;`).
1. In _Project Settings -> Modules_, choose the project name from the list.
   Press the `+` button, and choose _Import Module_ from the dropdown.
1. Open the `bin/` directory in the file explorer.
1. In the _Import Module_ modal that comes up, do the following steps:
   1. Choose _Create module from existing sources_, press Next.
   1. Check the correct location on the list, press Next.
   1. Choose correct Python interpreter on the list of SDKs (if it's not on the
      list, it must be added there [just point to the Python executable]),
      press Next.
   1. Press Finish.
   1. Press _Apply_ and exit the _Project Structure_ window with _OK_ button.

## Install requirements

```console
pip install -r bin/requirements.txt
```

## Envars

Some scripts require environment variables set. To unset them, use this
one-liner:

```bash
for var in $(export | grep -E '(POSTGRES|SPRING)' | awk -F'=' '{print $1}'); do unset "$var"; done
```

Note that in the above code snippet, `grep`'s use of `-E` flag may not work
outside of macOS.

## Warnings/errors

### No Python interpreter configured for the module

1. Press the _Configure Python interpreter_ button on the warning, or go
   straight to _Project Structure_ (`cmd + ;`).
1. Choose `bin` module on the list.
1. On the right-hand side, choose _Module SDK_ to the right Python version.
1. Save the changes by pressing _Apply_ and _OK_ buttons. The warning should go
   away after IntelliJ IDEA re-indexes the module changes.
