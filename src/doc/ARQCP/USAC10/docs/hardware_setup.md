# docs/hardware_setup.md

## USAC10 - Hardware Setup

### Componentes Necessários:
1. Arduino Uno (ou similar)
2. Sensor DHT22 (temperatura e humidade)
3. LEDs: Vermelho, Amarelo, Verde
4. Resistores 220Ω (3x)
5. Protoboard e jumpers

### Conexões:

#### Sensor DHT22:
- VCC → 5V
- GND → GND
- DATA → Pino 2

#### LEDs:
- LED Vermelho: Ânodo → Pino 3 → Resistor 220Ω → GND
- LED Amarelo: Ânodo → Pino 4 → Resistor 220Ω → GND
- LED Verde: Ânodo → Pino 5 → Resistor 220Ω → GND

### Teste:
1. Carregar `sensors.ino` no Arduino
2. Abrir Serial Monitor (9600 baud)
3. Verificar saída: "TEMP&unit:celsius&value:XX#HUM&unit:percentage&value:XX"
4. Carregar `lightsigns.ino` no Arduino
5. Enviar comandos via Serial:
    - "RE,01" → LED vermelho aceso
    - "YE,02" → LED amarelo aceso
    - "GE,03" → LED verde aceso
    - "RB,04" → LED vermelho piscante
    - "GTH" → Comando especial