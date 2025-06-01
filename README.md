# Itaú Backend Challenge - API REST de Transações

[![Java](https://img.shields.io/badge/Java-24-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![Tests](https://img.shields.io/badge/Tests-✅%2056%20Passed-green.svg)](#testes)

API REST desenvolvida em **Java 24** com **Spring Boot 3.5** para o desafio técnico do Itaú Unibanco. Consiste em um sistema de gerenciamento de transações financeiras com cálculo de estatísticas em tempo real.

## Descrição do Desafio

Esta API implementa um sistema de transações que:

- **Recebe transações** com validação completa
- **Calcula estatísticas** dos últimos 60 segundos em tempo real
- **Limpa dados** quando necessário
- **Armazena em memória** (sem banco de dados)
- **Thread-safe** para operações concorrentes
- **Logs estruturados** com AspectJ e Spring AOP
- **Documentação** via OpenAPI/Swagger

> **Fonte do Desafio**: [feltex/desafio-itau-backend](https://github.com/feltex/desafio-itau-backend)

## Endpoints da API

### **POST** `/transacao` - Criar Transação

Cria uma nova transação no sistema.

**Payload:**
```json
{
  "valor": 123.45,
  "dataHora": "2025-05-27T10:30:00.000-03:00"
}
```

**Respostas:**
- `201 Created` - Transação criada com sucesso
- `422 Unprocessable Entity` - Dados inválidos (futuro, valor negativo, etc.)
- `400 Bad Request` - JSON malformado

**Regras de Validação:**
- ✅ `valor` deve ser ≥ 0
- ✅ `dataHora` não pode ser no futuro
- ✅ Ambos os campos são obrigatórios

### **DELETE** `/transacao` - Limpar Transações

Remove todas as transações do sistema.

**Resposta:**
- `200 OK` - Todas as transações foram removidas

### **GET** `/estatistica` - Obter Estatísticas

Retorna estatísticas das transações dos últimos 60 segundos.

**Resposta:**
```json
{
  "count": 10,
  "sum": 1234.56,
  "avg": 123.456,
  "min": 12.34,
  "max": 123.56
}
```

- `200 OK` - Sempre retorna estatísticas
- Quando não há transações nos últimos 60s, todos os valores são `0.0`

## Como Executar

### Pré-requisitos

- **Java 24+** ([Amazon Corretto](https://aws.amazon.com/corretto/) recomendado)
- **Maven 3.9+**
- **Docker** (opcional)

### Execução Local

```bash
# Clonar o repositório
git clone <repository-url>
cd itau-backend

# Compilar o projeto
mvn clean compile

# Executar a aplicação
mvn spring-boot:run
```

A API estará disponível em: **http://localhost:8080**

### Execução com Docker

```bash
# Build e execução com docker-compose
docker-compose up -d

# Verificar logs
docker-compose logs -f
```

## Testes

### Execução dos Testes

```bash
# Executar todos os testes
mvn test

# Executar com relatório detalhado
mvn test -Dmaven.test.failure.ignore=true
```

### Cobertura de Testes

- **📊 56 testes** implementados
- **✅ 100% de cobertura** dos endpoints
- **🔄 Testes de concorrência** incluídos
- **⚡ Testes de performance** básicos

**Tipos de Teste:**
- **Unitários**: Controllers, Services, DTOs
- **Integração**: End-to-end workflows
- **Concorrência**: Thread-safety validation
- **Casos limite**: Validações e edge cases

### Load Testing

```bash
# Executar load test (1000 req/s por 1 minuto)
go run load-test.go
```

## Estrutura do Projeto

```
├── src/main/java/
│   ├── controller/          # REST Controllers
│   ├── service/            # Business Logic
│   ├── model/              # Domain Models
│   ├── dto/                # Data Transfer Objects
│   ├── config/             # Configuration Classes
│   ├── aspect/             # Logging Aspects
│   └── docs/               # API Documentation
├── src/test/java/          # Unit & Integration Tests
├── docker/                 # Docker Configuration
├── logs/                   # Application Logs
├── load-test.go           # Go Load Testing Tool
└── README.md              # This file
```

## Tecnologias Utilizadas

### Core Stack
- **Java 24**
- **Spring Boot 3.5.0**
- **Spring Validation**
- **Spring AOP**
- **Maven**

### Testing & Quality
- **JUnit 5**
- **Mockito**
- **MockMvc**
- **Go**

### DevOps & Observability
- **Docker**
- **Docker Compose**
- **Logback**
- **OpenAPI 3**

### Features Avançadas
- **Thread-safe operations**
- **Time-based filtering**
- **Aspect-oriented logging**
- **Real-time statistics**

## Documentação da API

### Swagger/OpenAPI

Acesse a documentação interativa em: **http://localhost:8080/swagger-ui.html**

### Exemplos de Uso

#### Criar uma transação
```bash
curl -X POST http://localhost:8080/transacao \
  -H "Content-Type: application/json" \
  -d '{
    "valor": 150.75,
    "dataHora": "2025-05-27T10:30:00.000-03:00"
  }'
```

#### Obter estatísticas
```bash
curl http://localhost:8080/estatistica
```

#### Limpar todas as transações
```bash
curl -X DELETE http://localhost:8080/transacao
```

## Configurações

### Profiles do Spring

- **`default`** - Desenvolvimento local
- **`docker`** - Execução em container

### Logs

- **Console**: Logs coloridos para desenvolvimento
- **Arquivo**: `logs/itau-backend.log` com rotação automática
- **Níveis**: INFO (produção), DEBUG (desenvolvimento)

### Performance

- **Memory**: Armazenamento em memória (ConcurrentLinkedQueue)
- **Concurrency**: Thread-safe para alta concorrência
- **GC**: Otimizado para baixa latência

## Funcionalidades Extras

### Implementadas

1. **Testes Automatizados** - 56 testes unitários e integração
2. **Containerização** - Docker + Docker Compose
3. **Logs Estruturados** - Aspectos com Logback
4. **Observabilidade** - Health checks básicos
5. **Performance Testing** - Load test em Go
6. **Tratamento de Erros** - Exception handlers customizados
7. **Documentação da API** - OpenAPI/Swagger
8. **Documentação do Sistema** - README completo

### Concorrência e Thread Safety

- [x] `ConcurrentLinkedQueue` para armazenamento thread-safe
- [x] Operações atômicas para estatísticas
- [x] Testes de concorrência validados
- [x] Sem race conditions identificadas

### Métricas e Observabilidade

- **Logs estruturados** com tempo de execução
- **Rastreamento** de todas as operações
- **Estatísticas** calculadas em tempo real
- **Performance** monitorada via aspectos

## Arquitetura

### Princípios SOLID
- **Single Responsibility** - Cada classe tem uma responsabilidade
- **Open/Closed** - Extensível via interfaces
- **Liskov Substitution** - Implementações substituíveis
- **Interface Segregation** - Interfaces específicas
- **Dependency Inversion** - Dependências abstratas

---

Este projeto foi desenvolvido como parte de um desafio técnico para o Itaú Unibanco.
