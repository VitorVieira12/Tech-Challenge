# 🟢 GCP Infrastructure - GKE + Cloud SQL

## 📋 Em Desenvolvimento

A infraestrutura GCP está planejada e será implementada com:

### Recursos Provisionados

- **VPC:** Auto mode network
- **GKE:** Google Kubernetes Engine v1.28
- **Nodes:** 2-4x e2-medium (2 vCPU, 4GB RAM)
- **Cloud SQL:** PostgreSQL 15
- **Firewall Rules:** Configuradas
- **Service Account:** Para GKE

### 💰 Custos Estimados

~$168/mês (intermediário entre Azure e AWS)

### 📦 Arquivos a serem criados

```
gcp/
├── provider.tf
├── variables.tf
├── main.tf
├── gke.tf
├── cloudsql.tf
├── outputs.tf
└── terraform.tfvars.example
```

### 🚀 Quick Start (Futuro)

```bash
cd infra/gcp
gcloud auth login
gcloud config set project YOUR_PROJECT_ID
terraform init
terraform apply
gcloud container clusters get-credentials tech-challenge-gke --zone us-central1-a
kubectl get nodes
```

---

**💡 Use a implementação AWS completa enquanto isso!**

Veja [../aws/README.md](../aws/README.md) para infraestrutura pronta para uso.

