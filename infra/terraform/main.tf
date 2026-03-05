locals {
  name_prefix = "${var.project_name}-${var.environment}"
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}

data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"]

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

resource "random_password" "db" {
  count   = var.db_password == null ? 1 : 0
  length  = 28
  special = true
  override_special = "!#$%^&*()-_=+[]{}:;,.?"
}

locals {
  effective_db_password = var.db_password != null ? var.db_password : random_password.db[0].result
}

resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = merge(local.common_tags, { Name = "${local.name_prefix}-vpc" })
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.main.id
  tags   = merge(local.common_tags, { Name = "${local.name_prefix}-igw" })
}

resource "aws_subnet" "public" {
  count                   = length(var.public_subnet_cidrs)
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidrs[count.index]
  map_public_ip_on_launch = true
  availability_zone       = data.aws_availability_zones.available.names[count.index]

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-public-${count.index + 1}"
    Tier = "public"
  })
}

resource "aws_subnet" "private" {
  count                   = length(var.private_subnet_cidrs)
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.private_subnet_cidrs[count.index]
  map_public_ip_on_launch = false
  availability_zone       = data.aws_availability_zones.available.names[count.index]

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-private-${count.index + 1}"
    Tier = "private"
  })
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }

  tags = merge(local.common_tags, { Name = "${local.name_prefix}-public-rt" })
}

resource "aws_route_table_association" "public" {
  count          = length(aws_subnet.public)
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_db_subnet_group" "main" {
  name       = "${replace(local.name_prefix, "_", "-")}-db-subnet-group"
  subnet_ids = aws_subnet.private[*].id

  tags = merge(local.common_tags, { Name = "${local.name_prefix}-db-subnet-group" })
}

resource "aws_security_group" "ec2" {
  name        = "${local.name_prefix}-ec2-sg"
  description = "EC2 ingress for app and administration"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "SSH from trusted admin and optional CI CIDRs"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = distinct(concat([var.admin_cidr], var.additional_ssh_cidrs))
  }

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.common_tags, { Name = "${local.name_prefix}-ec2-sg" })
}

resource "aws_security_group" "rds" {
  name        = "${local.name_prefix}-rds-sg"
  description = "RDS ingress from EC2 only"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "PostgreSQL from EC2 security group"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ec2.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.common_tags, { Name = "${local.name_prefix}-rds-sg" })
}

resource "aws_db_parameter_group" "postgres" {
  name   = "${replace(local.name_prefix, "_", "-")}-pg-params"
  family = "postgres16"

  parameter {
    name  = "rds.force_ssl"
    value = "1"
  }

  tags = merge(local.common_tags, { Name = "${local.name_prefix}-pg-params" })
}

resource "aws_db_instance" "postgres" {
  identifier                 = "${replace(local.name_prefix, "_", "-")}-db"
  engine                     = "postgres"
  engine_version             = var.db_engine_version
  instance_class             = var.db_instance_class
  allocated_storage          = var.db_allocated_storage
  storage_encrypted          = true
  db_name                    = var.db_name
  username                   = var.db_username
  password                   = local.effective_db_password
  db_subnet_group_name       = aws_db_subnet_group.main.name
  vpc_security_group_ids     = [aws_security_group.rds.id]
  publicly_accessible        = false
  backup_retention_period    = var.db_backup_retention_days
  deletion_protection        = var.db_deletion_protection
  multi_az                   = var.db_multi_az
  auto_minor_version_upgrade = true
  apply_immediately          = false
  skip_final_snapshot        = false
  final_snapshot_identifier  = "${replace(local.name_prefix, "_", "-")}-final-${formatdate("YYYYMMDDhhmm", timestamp())}"
  parameter_group_name       = aws_db_parameter_group.postgres.name

  tags = merge(local.common_tags, { Name = "${local.name_prefix}-postgres" })
}

resource "aws_iam_role" "ec2_ssm" {
  name = "${replace(local.name_prefix, "_", "-")}-ec2-ssm-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ec2.amazonaws.com"
      }
    }]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "ec2_ssm_core" {
  role       = aws_iam_role.ec2_ssm.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "ec2" {
  name = "${replace(local.name_prefix, "_", "-")}-ec2-profile"
  role = aws_iam_role.ec2_ssm.name
}

resource "aws_instance" "app" {
  ami                         = data.aws_ami.ubuntu.id
  instance_type               = var.ec2_instance_type
  subnet_id                   = aws_subnet.public[0].id
  key_name                    = var.ec2_key_pair_name
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  iam_instance_profile        = aws_iam_instance_profile.ec2.name
  associate_public_ip_address = true

  user_data = templatefile("${path.module}/templates/ec2-user-data.sh.tftpl", {
    github_repo_url = var.github_repo_url
  })

  root_block_device {
    volume_size           = 30
    volume_type           = "gp3"
    encrypted             = true
    delete_on_termination = true
  }

  metadata_options {
    http_tokens                 = "required"
    http_endpoint               = "enabled"
    instance_metadata_tags      = "disabled"
    http_put_response_hop_limit = 2
  }

  tags = merge(local.common_tags, { Name = "${local.name_prefix}-app" })
}

resource "aws_eip" "app" {
  count    = var.allocate_eip ? 1 : 0
  instance = aws_instance.app.id
  domain   = "vpc"

  tags = merge(local.common_tags, { Name = "${local.name_prefix}-app-eip" })
}
