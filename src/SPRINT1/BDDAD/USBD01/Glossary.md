# 📖 Dicionário de Dados (USBD01)

Este dicionário de dados descreve as entidades e atributos principais do sistema ferroviário, incluindo tabelas, tipos de dados, restrições e descrições.

---


## 🗂️ Tabela: **Operador**
| Atributo      | Tipo de Dados  | Restrição   | Descrição |
|---------------|----------------|-------------|-----------|
| idOperador    | INT            | PK          | Identificador único do operador ferroviário |
| nome          | VARCHAR(100)   | NOT NULL    | Nome do operador |
|---------------|----------------|-------------|-----------|
| idLinha       | INT            | PK          | Identificador único da linha ferroviária |
| nome          | VARCHAR(100)   | NOT NULL    | Nome da linha |
| idOperador    | INT            | FK → Operador | Operador responsável pela linha |
|-----------------|----------------|-------------|-----------|
| idSegmento      | INT            | PK          | Identificador único do segmento de linha |
| idLinha         | INT            | FK → Linha  | Linha a que pertence o segmento |
| idEstacaoInicio | INT            | FK → Estacao | Estação de início do segmento |
| idEstacaoFim    | INT            | FK → Estacao | Estação de fim do segmento |
| comprimento     | DECIMAL(10,2)  |             | Comprimento do segmento (km) |
| tipo            | VARCHAR(10)    |             | Tipo de via: simples/dupla |
| eletrificado    | CHAR(1)        | CHECK(S/N)  | Se o segmento é eletrificado |
| bitola          | DECIMAL(5,2)   |             | Largura da via (mm) |
| pesoMaximo      | DECIMAL(10,2)  |             | Peso máximo suportado (kg/m) |
| velocidadeMaxima| DECIMAL(5,2)   |             | Velocidade máxima (km/h) |
|---------------|----------------|-------------|-----------|
| idEstacao     | INT            | PK          | Identificador único da estação |
| nome          | VARCHAR(100)   | NOT NULL    | Nome da estação |
| localizacao   | VARCHAR(100)   |             | Localização (cidade, coordenadas, etc.) |
|----------------------|----------------|-------------|-----------|
| idLocomotiva         | INT            | PK          | Identificador único da locomotiva |
| idOperador           | INT            | FK → Operador | Operador dono da locomotiva |
| tipo                 | VARCHAR(20)    |             | Tipo: diesel/elétrica |
| modelo               | VARCHAR(50)    |             | Modelo da locomotiva |
| potencia             | INT            |             | Potência em kW |
| anoEntrada           | INT            |             | Ano de entrada em serviço |
| peso                 | DECIMAL(10,2)  |             | Peso total |
| capacidadeCombustivel| DECIMAL(10,2)  |             | Capacidade do tanque (se diesel) |
| bitola               | DECIMAL(5,2)   |             | Bitola compatível |
|---------------|----------------|-------------|-----------|
| idVagao       | INT            | PK          | Identificador único do vagão |
| idOperador    | INT            | FK → Operador | Operador dono do vagão |
| tipo          | VARCHAR(20)    |             | Tipo: boxcar, tanque, flatcar, etc. |
| cargaMaxima   | DECIMAL(10,2)  |             | Peso máximo da carga |
| volume        | DECIMAL(10,2)  |             | Capacidade volumétrica |
| tara          | DECIMAL(10,2)  |             | Peso do vagão vazio |
| bitola        | DECIMAL(5,2)   |             | Bitola compatível |
|---------------|----------------|-------------|-----------|
| idLinha       | INT            | FK → Linha  | Linha ferroviária |
| idEstacao     | INT            | FK → Estacao | Estação associada |
| ordemPassagem | INT            | PK composto | Ordem da estação na linha |

---