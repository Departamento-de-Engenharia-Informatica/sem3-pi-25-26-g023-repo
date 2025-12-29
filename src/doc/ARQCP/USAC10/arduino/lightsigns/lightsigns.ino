// arduino/lightsigns/lightsigns.ino

// Definição dos pinos dos LEDs
#define RED_PIN 3
#define YELLOW_PIN 4
#define GREEN_PIN 5

// Estado atual
int currentState = 0; // 0=off, 1=red, 2=yellow, 3=green, 4=red blink
unsigned long lastBlinkTime = 0;
bool blinkState = false;

void setup() {
    Serial.begin(9600);

    // Configurar pinos como output
    pinMode(RED_PIN, OUTPUT);
    pinMode(YELLOW_PIN, OUTPUT);
    pinMode(GREEN_PIN, OUTPUT);

    // Inicializar todos desligados
    digitalWrite(RED_PIN, LOW);
    digitalWrite(YELLOW_PIN, LOW);
    digitalWrite(GREEN_PIN, LOW);

    Serial.println("LightSigns Ready");
}

// Desligar todos os LEDs
void turnOffAllLEDs() {
    digitalWrite(RED_PIN, LOW);
    digitalWrite(YELLOW_PIN, LOW);
    digitalWrite(GREEN_PIN, LOW);
}

// Processar comando
void processCommand(String cmd) {
    cmd.trim();

    // Formato esperado: "CMD,NN" ou "GTH"
    if (cmd.length() < 2) return;

    // Extrair comando e número da via
    String command = cmd.substring(0, 2);
    int trackNumber = 0;

    if (cmd.length() > 3 && cmd.charAt(2) == ',') {
        trackNumber = cmd.substring(3).toInt();
    }

    // Desligar todos os LEDs primeiro
    turnOffAllLEDs();

    // Executar comando
    if (command == "RE") {
        digitalWrite(RED_PIN, HIGH);
        currentState = 1;
        Serial.print("Track ");
        Serial.print(trackNumber);
        Serial.println(": RED ON");
    }
    else if (command == "YE") {
        digitalWrite(YELLOW_PIN, HIGH);
        currentState = 2;
        Serial.print("Track ");
        Serial.print(trackNumber);
        Serial.println(": YELLOW ON");
    }
    else if (command == "GE") {
        digitalWrite(GREEN_PIN, HIGH);
        currentState = 3;
        Serial.print("Track ");
        Serial.print(trackNumber);
        Serial.println(": GREEN ON");
    }
    else if (command == "RB") {
        currentState = 4; // Modo piscante
        Serial.print("Track ");
        Serial.print(trackNumber);
        Serial.println(": RED BLINK");
    }
    else if (command == "GTH") {
        // Comando especial - manter LEDs como estão
        Serial.println("GTH command received");
    }
}

void loop() {
    // Processar piscar do LED vermelho se necessário
    if (currentState == 4) {
        unsigned long currentTime = millis();
        if (currentTime - lastBlinkTime > 500) { // 500ms interval
            blinkState = !blinkState;
            digitalWrite(RED_PIN, blinkState ? HIGH : LOW);
            lastBlinkTime = currentTime;
        }
    }

    // Verificar se há comandos na serial
    if (Serial.available() > 0) {
        String command = Serial.readStringUntil('\n');
        processCommand(command);
    }

    delay(10); // Pequeno delay para não sobrecarregar
}