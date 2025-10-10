#!/bin/bash
# ===============================================
# 📦 PhotoVault Backlog Import Script (GitHub CLI)
# ===============================================
# Autor: Vinicius Tertuliano da Silva
# Descrição: Cria labels e issues (user stories + tasks)
# ===============================================

echo "🚀 Iniciando importação do backlog do PhotoVault..."

# ========================
# 🔖 CRIAÇÃO DAS LABELS
# ========================
echo "🏷️  Criando labels..."

declare -A labels=(
  ["backend"]="1D76DB"
  ["frontend"]="0E8A16"
  ["devops"]="FBCA04"
  ["user-story"]="C5DEF5"
  ["task"]="BFD4F2"
  ["sprint-1"]="E99695"
  ["sprint-2"]="E99695"
  ["sprint-3"]="E99695"
  ["sprint-4"]="E99695"
  ["sprint-5"]="E99695"
  ["sprint-6"]="E99695"
)

for label in "${!labels[@]}"; do
  gh label create "$label" --color "${labels[$label]}" --force >/dev/null 2>&1
done

echo "✅ Labels criadas com sucesso."

# ========================
# 🗂️ CRIAÇÃO DAS ISSUES
# ========================
echo "📋 Criando issues..."

# Função auxiliar para criar issues
create_issue() {
  local title="$1"
  local body="$2"
  local labels="$3"
  gh issue create --title "$title" --body "$body" --label "$labels" >/dev/null 2>&1
}

# ========== SPRINT 1 ==========
create_issue "[US01] Configurar projeto Spring Boot" "Como desenvolvedor, quero configurar o projeto Spring Boot com Web, JPA, Security e AWS SDK." "backend,user-story,sprint-1"
create_issue "[US02] Cadastrar usuários (fotógrafos e clientes)" "Como administrador, quero cadastrar usuários no sistema." "backend,user-story,sprint-1"
create_issue "[US03] Criptografar senhas" "Como sistema, quero armazenar senhas de forma segura." "backend,user-story,sprint-1"
create_issue "[T01] Estrutura inicial do projeto" "Criar pacote base com.photovault.*" "backend,task,sprint-1"
create_issue "[T02] Configurar banco PostgreSQL" "Definir datasource e credenciais locais." "backend,task,sprint-1"
create_issue "[T03] Criar entidades User, Photographer, Client" "Modelar e mapear entidades no JPA." "backend,task,sprint-1"
create_issue "[T04] Configurar Spring Security com JWT" "Implementar autenticação com tokens JWT." "backend,task,sprint-1"
create_issue "[T05] Testes unitários iniciais" "Testar autenticação e persistência de usuários." "backend,task,sprint-1"

# ========== SPRINT 2 ==========
create_issue "[US04] Gerenciamento de pastas" "Como fotógrafo, quero criar, renomear e excluir pastas." "backend,user-story,sprint-2"
create_issue "[US05] Upload e exclusão de arquivos" "Como fotógrafo, quero fazer upload e deletar fotos." "backend,user-story,sprint-2"
create_issue "[US06] Download de fotos compartilhadas" "Como cliente, quero baixar fotos recebidas." "backend,user-story,sprint-2"
create_issue "[T06] Criar entidades Folder e File" "Modelar estrutura de diretórios e arquivos." "backend,task,sprint-2"
create_issue "[T07] Criar FolderService e FileService" "Implementar lógica de criação e listagem." "backend,task,sprint-2"
create_issue "[T08] Integração com AWS EFS" "Conectar o sistema ao EFS via AWS SDK." "backend,task,sprint-2"
create_issue "[T09] Endpoints REST /folders e /files" "Expor endpoints REST para gerenciamento de arquivos." "backend,task,sprint-2"
create_issue "[T10] Controle de acesso via JWT" "Proteger operações com autenticação." "backend,task,sprint-2"

# ========== SPRINT 3 ==========
create_issue "[US07] Compartilhamento de links" "Como fotógrafo, quero gerar link público para cliente." "backend,user-story,sprint-3"
create_issue "[US08] Acesso público por token" "Como cliente, quero acessar link e visualizar fotos." "backend,user-story,sprint-3"
create_issue "[US09] Expiração automática de links" "Como sistema, quero expirar links após prazo definido." "backend,user-story,sprint-3"
create_issue "[T11] Criar entidade ShareLink" "Modelar link compartilhado com UUID e expiração." "backend,task,sprint-3"
create_issue "[T12] Gerar URLs públicas com UUID" "Implementar geração e controle de validade." "backend,task,sprint-3"
create_issue "[T13] Endpoint /share/{token}" "Permitir download via token." "backend,task,sprint-3"
create_issue "[T14] Scheduler de limpeza de links expirados" "Remover registros vencidos periodicamente." "backend,task,sprint-3"
create_issue "[T15] Testes de integração" "Testar fluxo completo de upload e compartilhamento." "backend,task,sprint-3"

# ========== SPRINT 4 ==========
create_issue "[US10] Registro e login" "Como usuário, quero me registrar e fazer login." "frontend,user-story,sprint-4"
create_issue "[US11] Painel do fotógrafo" "Como fotógrafo, quero acessar meu painel de controle." "frontend,user-story,sprint-4"
create_issue "[T16] Criar projeto Angular/React" "Inicializar estrutura base do frontend." "frontend,task,sprint-4"
create_issue "[T17] Configurar rotas e layout base" "Criar Login, Dashboard, Header e Sidebar." "frontend,task,sprint-4"
create_issue "[T18] Serviço de autenticação JWT" "Implementar interceptador e armazenamento de token." "frontend,task,sprint-4"
create_issue "[T19] Integração com API /auth" "Testar login e registro com backend." "frontend,task,sprint-4"

# ========== SPRINT 5 ==========
create_issue "[US12] Visualização de pastas e arquivos" "Como fotógrafo, quero ver minhas pastas e arquivos." "frontend,user-story,sprint-5"
create_issue "[US13] Upload e exclusão pelo frontend" "Como fotógrafo, quero gerenciar fotos na interface." "frontend,user-story,sprint-5"
create_issue "[US14] Visualização do cliente via link" "Como cliente, quero visualizar e baixar fotos." "frontend,user-story,sprint-5"
create_issue "[T20] Componentes FolderList, FileList, UploadModal, ShareLinkDialog" "Implementar estrutura visual dos componentes." "frontend,task,sprint-5"
create_issue "[T21] Integração APIs /folders, /files, /share" "Conectar frontend aos endpoints." "frontend,task,sprint-5"
create_issue "[T22] Preview e botão de download" "Exibir miniaturas e botão de baixar." "frontend,task,sprint-5"
create_issue "[T23] Interface responsiva (Tailwind)" "Garantir UX fluida em diferentes telas." "frontend,task,sprint-5"

# ========== SPRINT 6 ==========
create_issue "[US15] Deploy AWS ECS" "Como PO, quero o sistema rodando na AWS." "devops,user-story,sprint-6"
create_issue "[US16] Monitoramento e logs" "Como DevOps, quero logs e métricas via CloudWatch." "devops,user-story,sprint-6"
create_issue "[T24] Dockerfile backend e frontend" "Criar containers para produção." "devops,task,sprint-6"
create_issue "[T25] CI/CD com GitHub Actions" "Configurar pipeline automatizada." "devops,task,sprint-6"
create_issue "[T26] Deploy ECS Fargate" "Subir containers para ECS." "devops,task,sprint-6"
create_issue "[T27] Banco RDS e montagem EFS" "Configurar persistência e volumes." "devops,task,sprint-6"
create_issue "[T28] Frontend via S3 + CloudFront" "Distribuir o front publicamente." "devops,task,sprint-6"
create_issue "[T29] Monitoramento CloudWatch" "Integrar logs e alertas." "devops,task,sprint-6"

echo "✅ Todas as issues criadas com sucesso!"
echo "🎯 Importação concluída — confira no GitHub!"
