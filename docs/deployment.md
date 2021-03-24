# Deployment

## DigitalOcean

### Terraform

#### Prerequisites

- [DigitalOcean Personal Access Token](https://www.digitalocean.com/docs/apis-clis/api/create-personal-access-token)
- [GitHub Personal Access Token](https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token)

#### Prepare envars

- `DO_PAT` – DigitalOcean Personal Access Token
- `DO_VPC` (optional) – UUID of a DigitalOcean Virtual Private Cloud (VPC).
  Obtain it via
  [DO's API](https://developers.digitalocean.com/documentation/v2/#list-all-vpcs).
  Leave empty to add the resource to the default project.
- `TERRAFORM_SSH_KEY` – Path to the passphrase-free SSH private key that's been
  added to DigitalOcean for use with Terraform provisioning. E.g.
  `$HOME/.ssh/id_ed25519_digitalocean_terraform`.
- `GH_USERNAME` – GitHub username used for logging in to GitHub Container
  Registry (`ghcr.io`).
- `GH_PAT` – GitHub Personal Access Token used for authenticating with
  `ghcr.io`.

```console
export DO_PAT=
export DO_VPC=
export TERRAFORM_SSH_KEY=
export GH_USERNAME=
export GH_PAT=
```

#### `plan` and `apply`

```console
cd terraform/digitalocean
terraform plan \
    -var "do_token=${DO_PAT}" \
    -var "vpc_uuid=${DO_VPC}" \
    -var "private_ssh_key=${TERRAFORM_SSH_KEY}" \
    -var "gh_username=${GH_USERNAME}" \
    -var "gh_pat=${GH_PAT}"
```

Analyse the above plan, and if all looks good, run `apply`:

```console
terraform apply \
    -var "do_token=${DO_PAT}" \
    -var "vpc_uuid=${DO_VPC}" \
    -var "private_ssh_key=${TERRAFORM_SSH_KEY}" \
    -var "gh_username=${GH_USERNAME}" \
    -var "gh_pat=${GH_PAT}"
```
