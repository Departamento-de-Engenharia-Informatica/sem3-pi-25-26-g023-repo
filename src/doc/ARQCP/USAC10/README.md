# USAC10 - Sensors and LightSigns Devices

## Objetivo
Implementar os dispositivos físicos (hardware + software) para:
1. **Sensors**: Medir temperatura e humidade com filtro de mediana móvel
2. **LightSigns**: Controlar LEDs que representam o estado das vias

## Especificações Técnicas

### Sensors Device
- **Sensor**: DHT22 (temperatura e humidade)
- **Pino**: Digital 2
- **Filtro**: Mediana móvel com janela de 5 valores
- **Buffer circular**: 10 posições
- **Formato de saída**: `TEMP&unit:celsius&value:XX#HUM&unit:percentage&value:XX`
- **Intervalo de leitura**: 5 segundos

### LightSigns Device
- **LEDs**:
    - Vermelho: Pino 3
    - Amarelo: Pino 4
    - Verde: Pino 5
- **Comandos suportados**:
    - `RE,NN` - Vermelho aceso (via ocupada)
    - `YE,NN` - Amarelo aceso (via atribuída)
    - `GE,NN` - Verde aceso (via livre)
    - `RB,NN` - Vermelho piscante (via inoperacional)
    - `GTH` - Comando especial (Get Temperature/Humidity)
- **Formato**: `CMD,NN` onde NN é número da via (01-99)

## Estrutura de Pastas

usac10/
├── arduino/
│ ├── sensors/
│ │ ├── sensors.ino # Código principal do sensor
│ │ └── moving_median.h # Biblioteca de mediana móvel
│ └── lightsigns/
│ └── lightsigns.ino # Código de controlo dos LEDs
├── docs/
│ └── hardware_setup.md # Instruções de montagem
└── README.md # Este ficheiro