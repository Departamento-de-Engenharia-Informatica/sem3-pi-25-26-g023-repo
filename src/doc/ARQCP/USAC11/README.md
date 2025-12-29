# USAC11 - Setup Inicial via Ficheiro de Texto

## Objetivo
Como Administrador, quero fazer o setup inicial das estruturas de dados usando um ficheiro de texto.

## Descrição
Esta US implementa o carregamento inicial do sistema de gestão da estação ferroviária a partir de ficheiros de configuração em formato texto. Inclui:
- Configuração da estação
- Utilizadores (com encriptação de passwords)
- Vias (tracks) com estados iniciais
- Buffers dos sensores

## Ficheiros de Entrada

### 1. config.txt
Ficheiro de configuração geral no formato `CHAVE=VALOR`.

### 2. users.txt
Ficheiro de utilizadores no formato: `nome;username;password;chave_cifra;cargo`

### 3. tracks.txt
Ficheiro de vias no formato: `id;estado;id_comboio`

## Como Usar

### Compilação:
```bash
make