variable "aws_region" {
  description = "AWS region for deployment."
  type        = string
  default     = "eu-west-2"
}

variable "project_name" {
  description = "Project name used for tagging and naming resources."
  type        = string
  default     = "xparience"
}

variable "environment" {
  description = "Environment label (e.g. prod, staging)."
  type        = string
  default     = "prod"
}

variable "vpc_cidr" {
  description = "VPC CIDR block."
  type        = string
  default     = "10.20.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "CIDRs for public subnets (for EC2/ingress)."
  type        = list(string)
  default     = ["10.20.1.0/24", "10.20.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDRs for private subnets (for RDS)."
  type        = list(string)
  default     = ["10.20.11.0/24", "10.20.12.0/24"]
}

variable "admin_cidr" {
  description = "Trusted CIDR allowed to SSH to EC2 (required for security)."
  type        = string
}

variable "ec2_instance_type" {
  description = "EC2 instance type for app host."
  type        = string
  default     = "t3.medium"
}

variable "ec2_key_pair_name" {
  description = "Existing AWS EC2 key pair name used for SSH access."
  type        = string
}

variable "allocate_eip" {
  description = "Allocate and attach an Elastic IP to EC2 for stable endpoint."
  type        = bool
  default     = true
}

variable "db_name" {
  description = "PostgreSQL database name."
  type        = string
  default     = "xpdb"
}

variable "db_username" {
  description = "PostgreSQL master username."
  type        = string
  default     = "postgres"
}

variable "db_password" {
  description = "Optional PostgreSQL master password. If null, Terraform generates one."
  type        = string
  default     = null
  sensitive   = true
}

variable "db_instance_class" {
  description = "RDS instance class."
  type        = string
  default     = "db.t4g.micro"
}

variable "db_allocated_storage" {
  description = "RDS allocated storage in GB."
  type        = number
  default     = 30
}

variable "db_engine_version" {
  description = "PostgreSQL engine version."
  type        = string
  default     = "16.4"
}

variable "db_backup_retention_days" {
  description = "RDS automated backup retention in days."
  type        = number
  default     = 7
}

variable "db_deletion_protection" {
  description = "Enable RDS deletion protection."
  type        = bool
  default     = true
}

variable "db_multi_az" {
  description = "Enable Multi-AZ RDS for high availability."
  type        = bool
  default     = false
}

variable "github_repo_url" {
  description = "Optional repository URL for convenience clone on EC2 bootstrap."
  type        = string
  default     = ""
}
