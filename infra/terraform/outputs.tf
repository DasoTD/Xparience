locals {
  ec2_host_output = var.allocate_eip ? aws_eip.app[0].public_ip : aws_instance.app.public_ip
}

output "ec2_public_ip" {
  description = "EC2 public IP (use this as EC2_HOST in GitHub secrets)."
  value       = local.ec2_host_output
}

output "ec2_public_dns" {
  description = "EC2 public DNS name."
  value       = aws_instance.app.public_dns
}

output "ec2_ssh_user" {
  description = "Default SSH user for this AMI."
  value       = "ubuntu"
}

output "rds_endpoint" {
  description = "Private RDS endpoint hostname."
  value       = aws_db_instance.postgres.address
}

output "rds_port" {
  description = "RDS PostgreSQL port."
  value       = aws_db_instance.postgres.port
}

output "app_spring_datasource_url" {
  description = "Datasource URL value for /etc/xparience/common.env"
  value       = "jdbc:postgresql://${aws_db_instance.postgres.address}:${aws_db_instance.postgres.port}/${var.db_name}?sslmode=require"
}

output "app_db_username" {
  description = "Database username for app config."
  value       = var.db_username
}

output "app_db_password" {
  description = "Database password for app config."
  value       = local.effective_db_password
  sensitive   = true
}

output "github_actions_secret_values" {
  description = "Values to set in GitHub secrets for CD workflow."
  value = {
    EC2_HOST = local.ec2_host_output
    EC2_USER = "ubuntu"
  }
}
