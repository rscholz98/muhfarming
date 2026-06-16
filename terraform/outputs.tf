output "ecr_repository_url" {
  value = aws_ecr_repository.app.repository_url
}

output "s3_bucket_name" {
  value = aws_s3_bucket.main.id
}

output "ec2_instance_id" {
  value = aws_instance.main.id
}

output "ec2_public_ip" {
  value = aws_instance.main.public_ip
}
