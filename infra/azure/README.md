# 🔵 Azure Infrastructure - AKS + PostgreSQL

## 📋 Em Desenvolvimento

A infraestrutura Azure está planejada e será implementada com:

### Recursos Provisionados

- **Resource Group:** tech-challenge-rg
- **Virtual Network:** 10.0.0.0/16
- **AKS:** Azure Kubernetes Service v1.28
- **Nodes:** 2-4x Standard_D2s_v3 (2 vCPU, 8GB RAM)
- **PostgreSQL:** Azure Database for PostgreSQL Flexible Server
- **Network Security Groups:** Configurados
- **Managed Identity:** Para AKS

### 💰 Custos Estimados

~$115/mês (mais econômico que AWS e GCP!)

### 📦 Arquivos a serem criados

```
azure/
├── provider.tf
├── variables.tf
├── main.tf
├── aks.tf
├── postgresql.tf
├── outputs.tf
└── terraform.tfvars.example
```

### 🚀 Quick Start (Futuro)

```bash
cd infra/azure
az login
terraform init
terraform apply
az aks get-credentials --name tech-challenge-aks --resource-group tech-challenge-rg
kubectl get nodes
```

---

**💡 Use a implementação AWS completa enquanto isso!**

Veja [../aws/README.md](../aws/README.md) para infraestrutura pronta para uso.

