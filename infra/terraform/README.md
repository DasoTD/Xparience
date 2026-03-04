# Terraform: AWS EC2 + RDS (Secure Baseline)

This Terraform stack provisions:
- VPC with public + private subnets
- EC2 app host in public subnet
- PostgreSQL RDS in private subnets (not publicly accessible)
- Security groups with least-privilege DB access (RDS only from EC2 SG)
- Encrypted EC2/RDS storage and RDS SSL enforcement

## 1) Configure variables

```bash
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars`:
- `admin_cidr` to your public IP CIDR (example: `203.0.113.10/32`)
- `ec2_key_pair_name` to an existing AWS key pair

## 2) Deploy

```bash
terraform init
terraform plan
terraform apply
```

## 3) Get the values you need (for GitHub + app env)

GitHub CD secrets (`EC2_HOST`, `EC2_USER`):

```bash
terraform output github_actions_secret_values
```

Database endpoint and JDBC URL:

```bash
terraform output rds_endpoint
terraform output app_spring_datasource_url
```

Database username/password for `/etc/xparience/common.env`:

```bash
terraform output app_db_username
terraform output -raw app_db_password
```

## 4) Apply values to your deployment

- GitHub repo secrets:
  - `EC2_HOST` = `terraform output -raw ec2_public_ip`
  - `EC2_USER` = `terraform output -raw ec2_ssh_user`
  - `EC2_SSH_PRIVATE_KEY` = private key content matching your key pair

- EC2 `/etc/xparience/common.env`:
  - `SPRING_DATASOURCE_URL` = `terraform output -raw app_spring_datasource_url`
  - `DB_USERNAME` = `terraform output -raw app_db_username`
  - `DB_PASSWORD` = `terraform output -raw app_db_password`

## Security notes

- Keep `admin_cidr` restricted to your IP only.
- Do not commit `terraform.tfvars` or private keys.
- `app_db_password` output is sensitive; handle securely.
