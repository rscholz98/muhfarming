variable "aws_region" {
  description = "AWS region for the bootstrap resources"
  type        = string
  default     = "eu-central-1"
}

variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
  default     = "muhfarming"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "development"
}
