variable "image_type" {
  # https://slugs.do-api.dev
  default = "docker-20-04"
}
variable "region" {
  # https://slugs.do-api.dev
  default = "fra1"
}
variable "size" {
  # https://slugs.do-api.dev
  default = "s-1vcpu-1gb"
}
variable "vpc_uuid" {
  # UUID of the DigitalOcean project. Leave empty to add the resource to the default project.
  # Use the API to obtain the list of VPCs: https://developers.digitalocean.com/documentation/v2/#list-all-vpcs
  default = ""
}
variable "gh_username" {
  # GitHub username used for logging in to Container Registry.
  # Provided in the CLI.
}
variable "gh_pat" {
  # GitHub Personal Access Token for authenticating with Container Registry.
  # Provided in the CLI.
}

resource "digitalocean_droplet" "gg-bot" {
  image = var.image_type
  name = "GG-Bot-Terraform"
  region = var.region
  size = var.size
  vpc_uuid = var.vpc_uuid
  private_networking = true
  ssh_keys = [
    data.digitalocean_ssh_key.terraform.id
  ]

  connection {
    host = self.ipv4_address
    user = "root"
    type = "ssh"
    private_key = file(var.private_ssh_key)
    timeout = "2m"
  }

  provisioner "remote-exec" {
    inline = [
      "export PATH=$PATH:/usr/bin",
      "echo '${var.gh_pat}' | docker login --username '${var.gh_username}' --password-stdin 'ghcr.io'",
      "docker pull 'ghcr.io/amrwc/gg-bot/ggbot:latest'"
    ]
  }
}
