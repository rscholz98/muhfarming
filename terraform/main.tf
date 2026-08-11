locals {
  name   = "muhfarming"
  region = "eu-central-1"
}

provider "aws" {
  region = local.region
}

# S3 bucket
resource "aws_s3_bucket" "main" {
  bucket        = "${local.name}-data"
  force_destroy = true

  tags = {
    Name    = "${local.name}-data"
    Project = local.name
  }
}

# Block all public access to the data bucket (it holds the DB replica).
resource "aws_s3_bucket_public_access_block" "main" {
  bucket                  = aws_s3_bucket.main.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# IAM role for EC2 (SSM access + ECR pull)
data "aws_iam_policy_document" "ec2_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ec2" {
  name               = "${local.name}-ec2"
  assume_role_policy = data.aws_iam_policy_document.ec2_assume.json
}

resource "aws_iam_role_policy_attachment" "ec2_ssm" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "ec2_ecr" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

# Allow the instance (via Litestream) to replicate/restore the SQLite DB
# to the data bucket. Scoped to the muhfarming-data bucket only.
data "aws_iam_policy_document" "ec2_s3" {
  statement {
    actions   = ["s3:ListBucket"]
    resources = [aws_s3_bucket.main.arn]
  }
  statement {
    actions = [
      "s3:GetObject",
      "s3:PutObject",
      "s3:DeleteObject",
    ]
    resources = ["${aws_s3_bucket.main.arn}/*"]
  }
}

resource "aws_iam_role_policy" "ec2_s3" {
  name   = "${local.name}-ec2-s3"
  role   = aws_iam_role.ec2.id
  policy = data.aws_iam_policy_document.ec2_s3.json
}

resource "aws_iam_instance_profile" "ec2" {
  name = "${local.name}-ec2"
  role = aws_iam_role.ec2.name
}

# ECR repository
resource "aws_ecr_repository" "app" {
  name                 = local.name
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}

# Security group
resource "aws_security_group" "ec2" {
  name        = "${local.name}-ec2"
  description = "Allow HTTP on 8080 and HTTPS"

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
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
}

# EC2 instance
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]
  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

resource "aws_instance" "main" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = "t3.micro"
  iam_instance_profile   = aws_iam_instance_profile.ec2.name
  vpc_security_group_ids = [aws_security_group.ec2.id]

  # The AMI defaults to a 2 GB root volume, which is too small for the OS
  # plus the app image (Go server + Litestream). Grow it; gp3 supports
  # online resize so this is an in-place change, not a replacement.
  root_block_device {
    volume_size = 16
    volume_type = "gp3"
  }

  # Install docker and ssm agent on first boot
  user_data = <<-EOF
    #!/bin/bash
    dnf install -y docker amazon-ssm-agent ec2-instance-connect
    systemctl enable --now docker
    systemctl enable --now amazon-ssm-agent
  EOF

  tags = {
    Name    = local.name
    Project = local.name
  }

  # The AMI data source uses most_recent, so a newly published AL2023 image
  # would otherwise force instance replacement (destroying local state and
  # changing the public IP). Pin to the AMI the instance already runs; bump
  # deliberately by removing this and applying when a rebuild is intended.
  lifecycle {
    ignore_changes = [ami]
  }
}
