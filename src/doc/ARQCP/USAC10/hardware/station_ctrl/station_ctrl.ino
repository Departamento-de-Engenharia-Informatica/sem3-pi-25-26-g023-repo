#include <DHT.h>

// --- CONFIGURAÇÃO DE HARDWARE ---
#define DHT_PIN 15       // Pino analógico A1 (Digital 15)
#define DHT_TYPE DHT11

// Pinos dos LEDs [Trilho 0..3][Vermelho, Amarelo, Verde]
// Ajusta estes números se ligares noutros pinos
const int LED_PINS[4][3] = {
    {2, 3, 4},   // Trilho 1
    {5, 6, 7},   // Trilho 2
    {8, 9, 10},  // Trilho 3
    {11, 12, 13} // Trilho 4
};

DHT dht(DHT_PIN, DHT_TYPE);

// Variáveis de Estado
enum LedState { OFF, ON, BLINKING };
LedState track_states[4] = {OFF, OFF, OFF, OFF};
bool blink_state = false;
unsigned long last_blink = 0;

void setup() {
    Serial.begin(9600); // Velocidade de comunicação
    dht.begin();

    // Configurar todos os pinos dos LEDs como SAÍDA
    for (int track = 0; track < 4; track++) {
        for (int color = 0; color < 3; color++) {
            pinMode(LED_PINS[track][color], OUTPUT);
            digitalWrite(LED_PINS[track][color], LOW); // Começar desligado
        }
    }

    pinMode(LED_BUILTIN, OUTPUT);
}

void loop() {
    // 1. Ler comandos do PC
    if (Serial.available() > 0) {
        String cmd = Serial.readStringUntil('\n');
        cmd.trim(); // Limpar espaços
        process_command(cmd);
    }

    // 2. Controlar o piscar (se houver luzes a piscar)
    unsigned long now = millis();
    if (now - last_blink >= 500) {
        last_blink = now;
        blink_state = !blink_state;
        update_blinking_leds();
    }

    delay(50);
}

void process_command(String cmd) {
    // COMANDO DE SENSORES: "GTH"
    if (cmd == "GTH") {
        float t = dht.readTemperature();
        float h = dht.readHumidity();

        if (isnan(t)) t = 0;
        if (isnan(h)) h = 0;

        // Formato: TEMP&unit:celsius&value:XX#HUM&unit:percent&value:YY
        String response = "TEMP&unit:celsius&value:";
        response += String((int)t);
        response += "#HUM&unit:percent&value:";
        response += String((int)h);

        Serial.println(response);
    }
    // COMANDO DE LUZES: "RE,01", "YE,02", etc.
    else if (cmd.length() >= 5 && cmd.indexOf(',') != -1) {
        String type = cmd.substring(0, 2);
        int track = cmd.substring(3).toInt();

        if (track >= 1 && track <= 4) {
            set_track_light(track - 1, type);
        }
    }
}

void set_track_light(int track_idx, String type) {
    // 1. Desligar tudo nesse trilho
    for(int i=0; i<3; i++) digitalWrite(LED_PINS[track_idx][i], LOW);

    // 2. Ligar a certa
    if (type == "RE") { // Red
        digitalWrite(LED_PINS[track_idx][0], HIGH);
        track_states[track_idx] = ON;
    } else if (type == "YE") { // Yellow
        digitalWrite(LED_PINS[track_idx][1], HIGH);
        track_states[track_idx] = ON;
    } else if (type == "GE") { // Green
        digitalWrite(LED_PINS[track_idx][2], HIGH);
        track_states[track_idx] = ON;
    } else if (type == "RB") { // Red Blinking
        track_states[track_idx] = BLINKING;
    }
}

void update_blinking_leds() {
    for (int i = 0; i < 4; i++) {
        if (track_states[i] == BLINKING) {
            // Piscar o vermelho (índice 0)
            digitalWrite(LED_PINS[i][0], blink_state ? HIGH : LOW);
        }
    }
}